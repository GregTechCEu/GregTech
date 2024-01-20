package gregtech.api.pipenet;

import gregtech.api.cover.Cover;
import gregtech.api.pipenet.alg.NetAlgorithm;
import gregtech.api.pipenet.alg.ShortestPathsAlgorithm;
import gregtech.api.pipenet.alg.SinglePathAlgorithm;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.common.covers.CoverShutter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class WorldPipeNetG<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>> extends WorldSavedData {

    private final boolean isDirected;
    private final boolean isSinglePath;
    private final boolean isFlow;

    private WeakReference<World> worldRef = new WeakReference<>(null);
    // TODO move graph & algorithm into NetGroup to reduce unnecessary algorithm cost
    Graph<NodeG<PipeType, NodeDataType>, NetEdge> pipeGraph;
    final Map<BlockPos, NodeG<PipeType, NodeDataType>> pipeMap = new Object2ObjectOpenHashMap<>();

    final NetAlgorithm.NetAlgorithmWrapper<PipeType, NodeDataType> netAlgorithm;

    boolean validAlgorithmInstance = false;

    /**
     * @param isDirected   Determines whether this net needs directed graph handling.
     *                     Used to respect filter directions in the item net and fluid net, for example.
     *                     If the graph is not directed, pipes should not support blocked connections.
     * @param isSinglePath Determines whether this net allows only one source and one destination per group.
     *                     Allows for optimizations in path lookup and cache invalidation.
     */
    public WorldPipeNetG(String name, boolean isDirected, boolean isSinglePath) {
        super(name);
        if (isDirected())
            this.pipeGraph = new SimpleDirectedWeightedGraph<>(NetEdge.class);
        else this.pipeGraph = new SimpleWeightedGraph<>(NetEdge.class);
        this.netAlgorithm = new NetAlgorithm.NetAlgorithmWrapper<>();
        this.isDirected = isDirected;
        this.isSinglePath = isSinglePath;
        this.isFlow = false;
    }

    WorldPipeNetG(String name, boolean isDirected, boolean isSinglePath, boolean isFlow) {
        super(name);
        if (isDirected())
            this.pipeGraph = new SimpleDirectedWeightedGraph<>(NetEdge.class);
        else this.pipeGraph = new SimpleWeightedGraph<>(NetEdge.class);
        this.netAlgorithm = new NetAlgorithm.NetAlgorithmWrapper<>();
        this.isDirected = isDirected;
        this.isSinglePath = isSinglePath;
        this.isFlow = isFlow;
    }

    public final boolean isDirected() {
        return isDirected;
    }
    public final boolean isSinglePath() {
        return isSinglePath;
    }
    public final boolean isFlow() {
        return isFlow;
    }

    public World getWorld() {
        return this.worldRef.get();
    }

    protected void setWorldAndInit(World world) {
        if (world != this.worldRef.get()) {
            this.worldRef = new WeakReference<>(world);
            onWorldSet();
        }
    }

    public static String getDataID(final String baseID, final World world) {
        if (world == null || world.isRemote)
            throw new RuntimeException("WorldPipeNetG should only be created on the server!");
        int dimension = world.provider.getDimension();
        return dimension == 0 ? baseID : baseID + '.' + dimension;
    }

    protected void onWorldSet() {
        this.rebuildNetAlgorithm();
    }

    /**
     * Preferred override. Only collects a fresh TE from the server if the provided TE is invalid.
     * 
     * @param tile The {@link TileEntityPipeBase} that paths are being requested for
     * @return the ordered list of paths associated with the {@link TileEntityPipeBase}
     */
    public List<NetPath<PipeType, NodeDataType>> getPaths(TileEntityPipeBase<PipeType, NodeDataType> tile) {
        return getPaths(this.pipeMap.get(tile.getPipePos()), tile);
    }

    /**
     * Special-case override. Forces the collection of a fresh TE from the server.
     * 
     * @param pos The {@link BlockPos} that paths are being requested for
     * @return the ordered list of paths associated with the {@link BlockPos}
     */
    public List<NetPath<PipeType, NodeDataType>> getPaths(BlockPos pos) {
        return getPaths(this.pipeMap.get(pos), null);
    }

    public List<NetPath<PipeType, NodeDataType>> getPaths(@Nullable NodeG<PipeType, NodeDataType> node,
                                                          @Nullable TileEntityPipeBase<PipeType, NodeDataType> tile) {
        if (node == null) return new ObjectArrayList<>();

        node.setHeldMTE(tile);

        if (!this.validAlgorithmInstance) this.rebuildNetAlgorithm();

        List<NetPath<PipeType, NodeDataType>> cache = node.getPathCache();
        if (cache != null) {
            return verifyList(cache, node);
        }

        List<NetPath<PipeType, NodeDataType>> list = this.netAlgorithm.getPathsList(node);
        return verifyList(node.setPathCache(list), node);
    }

    /**
     * Verification removes paths ending in unloaded TEs,
     * paths that don't connect to anything,
     * and all paths if the source TE is unloaded.
     */
    protected List<NetPath<PipeType, NodeDataType>> verifyList(List<NetPath<PipeType, NodeDataType>> list,
                                                               NodeG<PipeType, NodeDataType> source) {
        if (!verifyNode(source)) return new ObjectArrayList<>();
        return list.stream().filter(a -> verifyNode(a.getTargetNode())).collect(Collectors.toList());
    }

    protected boolean verifyNode(NodeG<PipeType, NodeDataType> node) {
        node.getHeldMTESafe();
        return node.hasConnecteds();
    }

    @Nullable
    protected TileEntityPipeBase<PipeType, NodeDataType> castTE(TileEntity te) {
        if (te instanceof TileEntityPipeBase<?, ?>pipe) {
            if (!getBasePipeClass().isAssignableFrom(pipe.getClass())) {
                return null;
            }
            return (TileEntityPipeBase<PipeType, NodeDataType>) pipe;
        }
        return null;
    }

    protected abstract Class<? extends IPipeTile<PipeType, NodeDataType>> getBasePipeClass();

    /**
     * Make sure to override this if your NetGroups use data.
     * 
     * @return The correct data variant
     */
    protected AbstractGroupData<PipeType, NodeDataType> getBlankGroupData() {
        return null;
    }

    public NodeG<PipeType, NodeDataType> getOrCreateNode(@NotNull IPipeTile<PipeType, NodeDataType> mte) {
        NodeG<PipeType, NodeDataType> node = this.pipeMap.get(mte.getPipePos());
        if (node != null) return node;
        if (!canAttachNode(mte.getNodeData())) return null;
        node = new NodeG<>(mte.getNodeData(), mte, this);
        this.addNode(node);
        return node;
    }

    protected final boolean canNodesConnect(NodeG<PipeType, NodeDataType> source, EnumFacing nodeFacing,
                                            NodeG<PipeType, NodeDataType> target) {
        return areNodeBlockedConnectionsCompatible(source, nodeFacing, target) &&
                areMarksCompatible(source.mark, target.mark) &&
                areNodesCustomContactable(source.getData(), target.getData());
    }

    private static boolean areMarksCompatible(int mark1, int mark2) {
        return mark1 == mark2 || mark1 == NodeG.DEFAULT_MARK || mark2 == NodeG.DEFAULT_MARK;
    }

    private boolean areNodeBlockedConnectionsCompatible(NodeG<PipeType, NodeDataType> source, EnumFacing nodeFacing,
                                                        NodeG<PipeType, NodeDataType> target) {
        return !source.isConnected(nodeFacing) && !target.isConnected(nodeFacing.getOpposite());
    }

    protected boolean areNodesCustomContactable(NodeDataType source, NodeDataType target) {
        return true;
    }

    protected boolean canAttachNode(NodeDataType nodeData) {
        return true;
    }

    public void updateActiveConnections(BlockPos nodePos, EnumFacing side, boolean connect) {
        NodeG<PipeType, NodeDataType> node = pipeMap.get(nodePos);
        if (node == null || node.isConnected(side) == connect) return;

        node.setConnected(side, connect);

        NodeG<PipeType, NodeDataType> nodeOffset = pipeMap.get(nodePos.offset(side));
        if (nodeOffset == null) return;

        nodeOffset.setConnected(side.getOpposite(), connect);

        if (connect) {
            if (!node.isBlocked(side)) {
                addEdge(nodeOffset, node, null);
                this.predicateEdge(nodeOffset, node, side.getOpposite());
                if (!this.isDirected()) return;
            }
            if (!nodeOffset.isBlocked(side.getOpposite())) {
                addEdge(node, nodeOffset, null);
                this.predicateEdge(node, nodeOffset, side);
            }
        } else {
            removeUndirectedEdge(node, nodeOffset);
        }
    }

    public void updateBlockedConnections(BlockPos nodePos, EnumFacing side, boolean blocked) {
        if (!isDirected()) return; // no such thing as blocked connections on undirected graphs.
        NodeG<PipeType, NodeDataType> node = pipeMap.get(nodePos);
        if (node == null || node.isBlocked(side) == blocked) return;

        node.setBlocked(side, blocked);

        NodeG<PipeType, NodeDataType> nodeOffset = pipeMap.get(nodePos.offset(side));
        if (nodeOffset == null) return;

        if (!blocked) {
            addEdge(nodeOffset, node, null);
            this.predicateEdge(nodeOffset, node, side);
        } else {
            removeEdge(nodeOffset, node);
        }
    }

    public void updateMark(BlockPos nodePos, int newMark) {
        NodeG<PipeType, NodeDataType> node = pipeMap.get(nodePos);

        int oldMark = node.mark;
        node.mark = newMark;

        for (EnumFacing side : EnumFacing.VALUES) {
            NodeG<PipeType, NodeDataType> nodeOffset = pipeMap.get(nodePos.offset(side));
            if (nodeOffset == null) continue;
            if (!areNodeBlockedConnectionsCompatible(node, side, nodeOffset) ||
                    !areNodesCustomContactable(node.getData(), nodeOffset.getData()))
                continue;
            if (areMarksCompatible(oldMark, nodeOffset.mark) == areMarksCompatible(newMark, nodeOffset.mark)) continue;

            if (areMarksCompatible(newMark, nodeOffset.mark)) {
                addEdge(node, nodeOffset, null);
                this.predicateEdge(node, nodeOffset, side);
            } else {
                removeEdge(node, nodeOffset);
            }
        }
    }

    public boolean hasNode(BlockPos pos) {
        return pipeMap.containsKey(pos);
    }

    public void addNodeSilent(NodeG<PipeType, NodeDataType> node) {
        pipeGraph.addVertex(node);
        this.pipeMap.put(node.getNodePos(), node);
        // we do not need to invalidate the cache, because just adding a node means it's not connected to anything.
    }

    public void addNode(NodeG<PipeType, NodeDataType> node) {
        addNodeSilent(node);
        this.markDirty();
    }

    @Nullable
    public NodeG<PipeType, NodeDataType> getNode(BlockPos pos) {
        return this.pipeMap.get(pos);
    }

    public void addUndirectedEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target) {
        this.addEdge(source, target, null);
        if (this.isDirected()) this.addEdge(target, source, null);
    }

    public void addEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target,
                        @Nullable AbstractEdgePredicate<?> predicate) {
        addEdge(source, target, source.getData().getWeightFactor() + target.getData().getWeightFactor(), predicate);
        this.validAlgorithmInstance = false;
    }

    public void addUndirectedEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target,
                                  double weight) {
        this.addEdge(source, target, weight, null);
        if (this.isDirected()) this.addEdge(target, source, weight, null);
    }

    public void addEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target, double weight,
                        @Nullable AbstractEdgePredicate<?> predicate) {
        if (pipeGraph.addEdge(source, target) != null) {
            NetGroup.mergeEdge(source, target);
            pipeGraph.setEdgeWeight(source, target, weight);
            if (predicate != null) {
                pipeGraph.getEdge(source, target).setPredicate(predicate);
            }
            this.validAlgorithmInstance = false;
            this.markDirty();
        }
    }

    public void predicateUndirectedEdge(BlockPos pos, EnumFacing faceToNeighbour) {
        NodeG<PipeType, NodeDataType> source = this.pipeMap.get(pos);
        NodeG<PipeType, NodeDataType> target = this.pipeMap.get(pos.offset(faceToNeighbour));
        if (source != null && target != null) {
            this.predicateUndirectedEdge(source, target, faceToNeighbour);
        }
    }

    public void predicateEdge(BlockPos pos, EnumFacing faceToNeighbour) {
        NodeG<PipeType, NodeDataType> source = this.pipeMap.get(pos);
        NodeG<PipeType, NodeDataType> target = this.pipeMap.get(pos.offset(faceToNeighbour));
        if (source != null && target != null)
            this.predicateEdge(source, target, faceToNeighbour);
    }

    public void predicateUndirectedEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target,
                                        EnumFacing faceToNeighbour) {
        this.predicateEdge(source, target, faceToNeighbour);
        if (this.isDirected()) this.predicateEdge(target, source, faceToNeighbour.getOpposite());
    }

    public void predicateEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target,
                              EnumFacing faceToNeighbour) {
        if (!this.pipeGraph.containsEdge(source, target)) return;
        Cover thisCover = source.getHeldMTESafe().getCoverableImplementation().getCoverAtSide(faceToNeighbour);
        Cover neighbourCover = target.getHeldMTESafe().getCoverableImplementation()
                .getCoverAtSide(faceToNeighbour.getOpposite());
        AbstractEdgePredicate<?> predicate = getPredicate(thisCover, neighbourCover);
        predicate.setPosInfo(source.getNodePos(), target.getNodePos());
        NetEdge edge = this.pipeGraph.getEdge(source, target);
        edge.setPredicate(predicate);
    }

    protected AbstractEdgePredicate<?> getPredicate(Cover thisCover, Cover neighbourCover) {
        return shutterify(new BasicEdgePredicate(), thisCover, neighbourCover);
    }

    protected final AbstractEdgePredicate<?> shutterify(AbstractEdgePredicate<?> predicate, @Nullable Cover thisCover,
                                                        @Nullable Cover neighbourCover) {
        if (predicate instanceof IShutteredEdgePredicate shutteredEdgePredicate) {
            if (thisCover instanceof CoverShutter shutter) {
                shutteredEdgePredicate.setShutteredSource(shutter.isWorkingEnabled());
            }
            if (neighbourCover instanceof CoverShutter shutter) {
                shutteredEdgePredicate.setShutteredTarget(shutter.isWorkingEnabled());
            }
        }
        return predicate;
    }

    public void removeUndirectedEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target) {
        this.removeEdge(source, target);
        if (isDirected()) this.removeEdge(target, source);
    }

    public void removeEdge(NodeG<PipeType, NodeDataType> source, NodeG<PipeType, NodeDataType> target) {
        if (source.getGroupSafe() != null && source.getGroupSafe().splitEdge(source, target)) {
            this.validAlgorithmInstance = false;
            this.markDirty();
        }
    }

    public void removeNode(BlockPos pos) {
        this.removeNode(this.pipeMap.get(pos));
    }

    public void removeNode(@Nullable NodeG<PipeType, NodeDataType> node) {
        if (node != null) {
            if (this.pipeGraph.edgesOf(node).size() != 0) this.validAlgorithmInstance = false;
            if (node.getGroupSafe() != null) {
                node.getGroupSafe().splitNode(node);
            } else this.pipeGraph.removeVertex(node);
            this.pipeMap.remove(node.getNodePos());
            this.markDirty();
        }
    }

    public NetGroup<PipeType, NodeDataType> getGroup(BlockPos pos) {
        NodeG<PipeType, NodeDataType> node = this.getNode(pos);
        if (node == null) return null;
        if (node.getGroupSafe() != null) return node.getGroupSafe();
        return node.setGroup(new NetGroup<>(this.pipeGraph, this));
    }

    public boolean markNodeAsActive(BlockPos nodePos, boolean isActive) {
        NodeG<PipeType, NodeDataType> node = this.pipeMap.get(nodePos);
        if (node != null && node.isActive != isActive) {
            node.isActive = isActive;
            this.markDirty();
            return true;
        }
        return false;
    }

    protected void rebuildNetAlgorithm() {
        if (!this.isSinglePath()) {
            this.netAlgorithm.setAlg(new ShortestPathsAlgorithm<>(pipeGraph));
        } else {
            this.netAlgorithm.setAlg(new SinglePathAlgorithm<>(pipeGraph, isDirected()));
        }
        this.validAlgorithmInstance = true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (!nbt.hasKey("NetEdges")) {
            return;
        }
        NBTTagList allPipeNodes = nbt.getTagList("PipeNodes", Constants.NBT.TAG_COMPOUND);
        Map<Long, NodeG<PipeType, NodeDataType>> longPosMap = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < allPipeNodes.tagCount(); i++) {
            NBTTagCompound pNodeTag = allPipeNodes.getCompoundTagAt(i);
            NodeG<PipeType, NodeDataType> node = new NodeG<>(pNodeTag, this);
            longPosMap.put(node.getLongPos(), node);
            this.addNodeSilent(node);
        }
        NBTTagList allNetEdges = nbt.getTagList("NetEdges", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < allNetEdges.tagCount(); i++) {
            NBTTagCompound gEdgeTag = allNetEdges.getCompoundTagAt(i);
            new NetEdge.NBTBuilder<>(longPosMap, gEdgeTag, this::addEdge).addIfBuildable();
        }
        NBTTagList allNetGroups = nbt.getTagList("NetGroups", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < allNetGroups.tagCount(); i++) {
            NBTTagCompound gTag = allNetGroups.getCompoundTagAt(i);
            new NetGroup.NBTBuilder<>(longPosMap, gTag).build(this.pipeGraph, this);
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        NBTTagList allPipeNodes = new NBTTagList();
        Set<NetGroup<PipeType, NodeDataType>> groups = new ObjectOpenHashSet<>();
        for (NodeG<PipeType, NodeDataType> node : pipeGraph.vertexSet()) {
            if (node.getGroupSafe() != null) groups.add(node.getGroupSafe());
            NBTTagCompound nodeTag = node.serializeNBT();
            NBTTagCompound dataTag = new NBTTagCompound();
            writeNodeData(node.getData(), dataTag);
            nodeTag.setTag("Data", dataTag);
            allPipeNodes.appendTag(nodeTag);
        }
        compound.setTag("PipeNodes", allPipeNodes);

        NBTTagList allNetEdges = new NBTTagList();
        for (NetEdge edge : pipeGraph.edgeSet()) {
            allNetEdges.appendTag(edge.serializeNBT());
        }
        compound.setTag("NetEdges", allNetEdges);

        NBTTagList allNetGroups = new NBTTagList();
        for (NetGroup<PipeType, NodeDataType> group : groups) {
            allNetGroups.appendTag(group.serializeNBT());
        }
        compound.setTag("NetGroups", allNetGroups);

        return compound;
    }

    /**
     * Serializes node data into specified tag compound
     * Used for writing persistent node data
     */
    protected abstract void writeNodeData(NodeDataType nodeData, NBTTagCompound tagCompound);

    /**
     * Deserializes node data from specified tag compound
     * Used for reading persistent node data
     */
    protected abstract NodeDataType readNodeData(NBTTagCompound tagCompound);

}
