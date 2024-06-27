package gregtech.api.pipenet;

import gregtech.api.cover.Cover;
import gregtech.api.pipenet.alg.INetAlgorithm;
import gregtech.api.pipenet.alg.iter.ICacheableIterator;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;
import gregtech.api.pipenet.graph.ICustomGraph;
import gregtech.api.pipenet.predicate.AbstractEdgePredicate;
import gregtech.api.pipenet.predicate.BasicEdgePredicate;
import gregtech.api.pipenet.predicate.IShutteredEdgePredicate;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class WorldPipeNetBase<NodeDataType extends INodeData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, Edge extends NetEdge> extends WorldSavedData {

    private final boolean isDirected;
    private final Function<WorldPipeNetBase<NodeDataType, PipeType, Edge>, INetAlgorithm<PipeType, NodeDataType, Edge>> algorithmBuilder;

    private Set<INBTBuilder> builders = null;

    private WeakReference<World> worldRef = new WeakReference<>(null);
    protected final ICustomGraph<PipeType, NodeDataType, Edge> pipeGraph;
    protected final Map<BlockPos, NetNode<PipeType, NodeDataType, Edge>> pipeMap = new Object2ObjectOpenHashMap<>();

    private final INetAlgorithm.NetAlgorithmWrapper<PipeType, NodeDataType, Edge> netAlgorithm;

    private boolean validAlgorithmInstance = false;

    WorldPipeNetBase(String name, boolean isDirected,
                     Function<WorldPipeNetBase<NodeDataType, PipeType, Edge>, INetAlgorithm<PipeType, NodeDataType, Edge>> algorithmBuilder,
                     ICustomGraph<PipeType, NodeDataType, Edge> graph) {
        super(name);
        graph.setOwningNet(this);
        this.pipeGraph = graph;
        this.netAlgorithm = new INetAlgorithm.NetAlgorithmWrapper<>();
        this.isDirected = isDirected;
        this.algorithmBuilder = algorithmBuilder;
    }

    public ICustomGraph<PipeType, NodeDataType, Edge> getGraph() {
        return this.pipeGraph;
    }

    public final boolean isDirected() {
        return isDirected;
    }

    protected void markAlgInvalid() {
        this.validAlgorithmInstance = false;
    }

    protected void markAlgValid() {
        this.validAlgorithmInstance = true;
    }

    protected boolean hasValidAlg() {
        return this.validAlgorithmInstance;
    }

    public World getWorld() {
        return this.worldRef.get();
    }

    public final boolean usesDynamicWeights() {
        if (!needsDynamicWeights()) return false;
        if (!this.hasValidAlg()) this.rebuildNetAlgorithm();
        return this.netAlgorithm.supportsDynamicWeights();
    }

    protected boolean needsDynamicWeights() {
        return false;
    }

    protected void setWorldAndInit(World world) {
        if (world != this.worldRef.get()) {
            this.worldRef = new WeakReference<>(world);
            onWorldSet();
            // some builders have to wait for world set, so we wait until then to build them.
            if (this.builders != null) this.builders.forEach(INBTBuilder::build);
        }
    }

    public static String getDataID(final String baseID, final World world) {
        if (world == null || world.isRemote)
            throw new RuntimeException("WorldPipeNetBase should only be created on the server!");
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
    public Iterator<NetPath<PipeType, NodeDataType, Edge>> getPaths(TileEntityPipeBase<PipeType, NodeDataType, Edge> tile) {
        return getPaths(this.pipeMap.get(tile.getPipePos()), tile);
    }

    /**
     * Special-case override. Forces the collection of a fresh TE from the server.
     * 
     * @param pos The {@link BlockPos} that paths are being requested for
     * @return the ordered list of paths associated with the {@link BlockPos}
     */
    public Iterator<NetPath<PipeType, NodeDataType, Edge>> getPaths(BlockPos pos) {
        return getPaths(this.pipeMap.get(pos), null);
    }

    public Iterator<NetPath<PipeType, NodeDataType, Edge>> getPaths(@Nullable NetNode<PipeType, NodeDataType, Edge> node,
                                                                    @Nullable TileEntityPipeBase<PipeType, NodeDataType, Edge> tile) {
        if (node == null) return Collections.emptyIterator();

        node.setHeldMTE(tile);

        if (!this.hasValidAlg()) this.rebuildNetAlgorithm();

        Iterator<NetPath<PipeType, NodeDataType, Edge>> cache = node.getPathCache();
        if (cache != null) return cache;

        Iterator<NetPath<PipeType, NodeDataType, Edge>> iter = this.netAlgorithm.getPathsIterator(node);
        if (iter instanceof ICacheableIterator) {
            return node.setPathCache((ICacheableIterator<NetPath<PipeType, NodeDataType, Edge>>) iter);
        } else return iter;
    }

    /**
     * Verification removes paths ending in unloaded TEs,
     * paths that don't connect to anything,
     * and all paths if the source TE is unloaded.
     */
    protected List<NetPath<PipeType, NodeDataType, Edge>> verifyList(List<NetPath<PipeType, NodeDataType, Edge>> list,
                                                                     NetNode<PipeType, NodeDataType, Edge> source) {
        if (!verifyNode(source)) return new ObjectArrayList<>();
        return list.stream().filter(a -> verifyNode(a.getTargetNode()) && a.getWeight() != Double.POSITIVE_INFINITY)
                .collect(Collectors.toList());
    }

    protected boolean verifyNode(NetNode<PipeType, NodeDataType, Edge> node) {
        node.getHeldMTESafe();
        return node.hasConnecteds();
    }

    @Nullable
    protected TileEntityPipeBase<PipeType, NodeDataType, Edge> castTE(TileEntity te) {
        if (te instanceof TileEntityPipeBase<?, ?, ?>pipe) {
            if (!getBasePipeClass().isAssignableFrom(pipe.getClass())) {
                return null;
            }
            // noinspection unchecked
            return (TileEntityPipeBase<PipeType, NodeDataType, Edge>) pipe;
        }
        return null;
    }

    public void markNodeAsOldData(NetNode<PipeType, NodeDataType, Edge> node) {
        updateActiveNodeStatus(node);
    }

    protected abstract Class<? extends IPipeTile<PipeType, NodeDataType, Edge>> getBasePipeClass();

    /**
     * Make sure to override this if your NetGroups use data.
     * 
     * @return The correct data variant
     */
    protected AbstractGroupData<PipeType, NodeDataType> getBlankGroupData() {
        return null;
    }

    public NetNode<PipeType, NodeDataType, Edge> getOrCreateNode(@NotNull IPipeTile<PipeType, NodeDataType, Edge> mte) {
        NetNode<PipeType, NodeDataType, Edge> node = this.pipeMap.get(mte.getPipePos());
        if (node != null) return node;
        if (!canAttachNode(mte.getNodeData())) return null;
        node = new NetNode<>(mte.getNodeData(), mte, this);
        this.addNode(node);
        return node;
    }

    protected final boolean canNodesConnect(NetNode<PipeType, NodeDataType, Edge> source, EnumFacing nodeFacing,
                                            NetNode<PipeType, NodeDataType, Edge> target) {
        return areNodeBlockedConnectionsCompatible(source, nodeFacing, target) &&
                areMarksCompatible(source.mark, target.mark) &&
                areNodesCustomContactable(source.getData(), target.getData());
    }

    private static boolean areMarksCompatible(int mark1, int mark2) {
        return mark1 == mark2 || mark1 == NetNode.DEFAULT_MARK || mark2 == NetNode.DEFAULT_MARK;
    }

    private boolean areNodeBlockedConnectionsCompatible(NetNode<PipeType, NodeDataType, Edge> source,
                                                        EnumFacing nodeFacing,
                                                        NetNode<PipeType, NodeDataType, Edge> target) {
        return !source.isConnected(nodeFacing) && !target.isConnected(nodeFacing.getOpposite());
    }

    protected boolean areNodesCustomContactable(NodeDataType source, NodeDataType target) {
        return true;
    }

    protected boolean canAttachNode(NodeDataType nodeData) {
        return true;
    }

    public void updateActiveConnections(BlockPos nodePos, EnumFacing side, boolean connect) {
        NetNode<PipeType, NodeDataType, Edge> node = pipeMap.get(nodePos);
        if (node == null || node.isConnected(side) == connect) return;

        node.setConnected(side, connect);
        updateActiveNodeStatus(node);

        NetNode<PipeType, NodeDataType, Edge> nodeOffset = pipeMap.get(nodePos.offset(side));
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
        NetNode<PipeType, NodeDataType, Edge> node = pipeMap.get(nodePos);
        if (node == null || node.isBlocked(side) == blocked) return;

        node.setBlocked(side, blocked);

        NetNode<PipeType, NodeDataType, Edge> nodeOffset = pipeMap.get(nodePos.offset(side));
        if (nodeOffset == null) return;

        if (!blocked) {
            addEdge(nodeOffset, node, null);
            this.predicateEdge(nodeOffset, node, side);
        } else {
            removeEdge(nodeOffset, node);
        }
    }

    public void updateMark(BlockPos nodePos, int newMark) {
        NetNode<PipeType, NodeDataType, Edge> node = pipeMap.get(nodePos);

        int oldMark = node.mark;
        node.mark = newMark;

        for (EnumFacing side : EnumFacing.VALUES) {
            NetNode<PipeType, NodeDataType, Edge> nodeOffset = pipeMap.get(nodePos.offset(side));
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

    protected abstract Capability<?>[] getConnectionCapabilities();

    public boolean hasNode(BlockPos pos) {
        return pipeMap.containsKey(pos);
    }

    public void addNodeSilent(NetNode<PipeType, NodeDataType, Edge> node) {
        pipeGraph.addVertex(node);
        this.pipeMap.put(node.getNodePos(), node);
        // we do not need to invalidate the cache, because just adding a node means it's not connected to anything.
    }

    public void addNode(NetNode<PipeType, NodeDataType, Edge> node) {
        addNodeSilent(node);
        this.markDirty();
    }

    @Nullable
    public NetNode<PipeType, NodeDataType, Edge> getNode(BlockPos pos) {
        return this.pipeMap.get(pos);
    }

    public void addUndirectedEdge(NetNode<PipeType, NodeDataType, Edge> source,
                                  NetNode<PipeType, NodeDataType, Edge> target) {
        this.addEdge(source, target, null);
        if (this.isDirected()) this.addEdge(target, source, null);
    }

    public void addEdge(NetNode<PipeType, NodeDataType, Edge> source, NetNode<PipeType, NodeDataType, Edge> target,
                        @Nullable AbstractEdgePredicate<?> predicate) {
        addEdge(source, target, source.getData().getWeightFactor() + target.getData().getWeightFactor(), predicate);
    }

    public void addUndirectedEdge(NetNode<PipeType, NodeDataType, Edge> source,
                                  NetNode<PipeType, NodeDataType, Edge> target,
                                  double weight) {
        this.addEdge(source, target, weight, null);
        if (this.isDirected()) this.addEdge(target, source, weight, null);
    }

    public void addEdge(NetNode<PipeType, NodeDataType, Edge> source, NetNode<PipeType, NodeDataType, Edge> target,
                        double weight,
                        @Nullable AbstractEdgePredicate<?> predicate) {
        if (pipeGraph.addEdge(source, target) != null) {
            NetGroup.mergeEdge(source, target);
            pipeGraph.setEdgeWeight(source, target, weight);
            if (predicate != null) {
                pipeGraph.getEdge(source, target).setPredicate(predicate);
            }
            this.markAlgInvalid();
            this.markDirty();
        }
    }

    public void predicateUndirectedEdge(BlockPos pos, EnumFacing faceToNeighbour) {
        NetNode<PipeType, NodeDataType, Edge> source = this.pipeMap.get(pos);
        NetNode<PipeType, NodeDataType, Edge> target = this.pipeMap.get(pos.offset(faceToNeighbour));
        if (source != null && target != null) {
            this.predicateUndirectedEdge(source, target, faceToNeighbour);
        }
    }

    public void predicateEdge(BlockPos pos, EnumFacing faceToNeighbour) {
        NetNode<PipeType, NodeDataType, Edge> source = this.pipeMap.get(pos);
        NetNode<PipeType, NodeDataType, Edge> target = this.pipeMap.get(pos.offset(faceToNeighbour));
        if (source != null && target != null)
            this.predicateEdge(source, target, faceToNeighbour);
    }

    public void predicateUndirectedEdge(NetNode<PipeType, NodeDataType, Edge> source,
                                        NetNode<PipeType, NodeDataType, Edge> target,
                                        EnumFacing faceToNeighbour) {
        this.predicateEdge(source, target, faceToNeighbour);
        if (this.isDirected()) this.predicateEdge(target, source, faceToNeighbour.getOpposite());
    }

    public void predicateEdge(NetNode<PipeType, NodeDataType, Edge> source,
                              NetNode<PipeType, NodeDataType, Edge> target,
                              EnumFacing faceToNeighbour) {
        if (!this.pipeGraph.containsEdge(source, target)) return;
        Cover thisCover = source.getHeldMTESafe().getCoverableImplementation().getCoverAtSide(faceToNeighbour);
        Cover neighbourCover = target.getHeldMTESafe().getCoverableImplementation()
                .getCoverAtSide(faceToNeighbour.getOpposite());
        AbstractEdgePredicate<?> predicate = getPredicate(thisCover, neighbourCover);
        predicate.setPosInfo(source.getNodePos(), target.getNodePos());
        Edge edge = this.pipeGraph.getEdge(source, target);
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

    public void removeUndirectedEdge(NetNode<PipeType, NodeDataType, Edge> source,
                                     NetNode<PipeType, NodeDataType, Edge> target) {
        this.removeEdge(source, target);
        if (isDirected()) this.removeEdge(target, source);
    }

    public void removeEdge(NetNode<PipeType, NodeDataType, Edge> source, NetNode<PipeType, NodeDataType, Edge> target) {
        if (source.getGroupSafe() != null && source.getGroupSafe().splitEdge(source, target)) {
            this.markAlgInvalid();
            this.markDirty();
        }
    }

    public void removeNode(BlockPos pos) {
        this.removeNode(this.pipeMap.get(pos));
    }

    public void removeNode(@Nullable NetNode<PipeType, NodeDataType, Edge> node) {
        if (node != null) {
            if (this.pipeGraph.edgesOf(node).size() != 0) this.markAlgInvalid();
            if (node.getGroupUnsafe() != null) {
                node.getGroupUnsafe().splitNode(node);
            } else this.pipeGraph.removeVertex(node);
            this.pipeMap.remove(node.getNodePos());
            this.markDirty();
        }
    }

    public NetGroup<PipeType, NodeDataType, Edge> getGroup(BlockPos pos) {
        NetNode<PipeType, NodeDataType, Edge> node = this.getNode(pos);
        if (node == null) return null;
        if (node.getGroupSafe() != null) return node.getGroupSafe();
        return node.setGroup(new NetGroup<>(this.pipeGraph, this));
    }

    public boolean updateActiveNodeStatus(NetNode<PipeType, NodeDataType, Edge> node) {
        return markNodeAsActive(node, shouldNodeBeActive(node));
    }

    public boolean shouldNodeBeActive(NetNode<PipeType, NodeDataType, Edge> node) {
        var connecteds = node.getConnecteds();
        if (connecteds != null) return connecteds.entrySet().stream().filter(entry -> {
            if (entry.getValue() instanceof IPipeTile<?, ?, ?>) return false;
            for (Capability<?> cap : this.getConnectionCapabilities()) {
                if (entry.getValue().hasCapability(cap, entry.getKey())) return true;
            }
            return false;
        }).toArray().length > 0;
        return false;
    }

    public boolean markNodeAsActive(NetNode<PipeType, NodeDataType, Edge> node, boolean isActive) {
        if (node != null && node.isActive != isActive) {
            node.getGroupSafe().clearPathCaches();
            node.isActive = isActive;
            this.markDirty();
            return true;
        }
        return false;
    }

    protected void rebuildNetAlgorithm() {
        this.netAlgorithm.setAlg(algorithmBuilder.apply(this));
        this.markAlgValid();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (!nbt.hasKey("NetEdges")) {
            return;
        }
        this.builders = new ObjectOpenHashSet<>();
        NBTTagList allPipeNodes = nbt.getTagList("PipeNodes", Constants.NBT.TAG_COMPOUND);
        Map<Long, NetNode<PipeType, NodeDataType, Edge>> longPosMap = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < allPipeNodes.tagCount(); i++) {
            NBTTagCompound pNodeTag = allPipeNodes.getCompoundTagAt(i);
            NetNode<PipeType, NodeDataType, Edge> node = new NetNode<>(pNodeTag, this);
            longPosMap.put(node.getLongPos(), node);
            this.addNodeSilent(node);
        }
        NBTTagList allNetEdges = nbt.getTagList("NetEdges", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < allNetEdges.tagCount(); i++) {
            NBTTagCompound gEdgeTag = allNetEdges.getCompoundTagAt(i);
            this.builders.add(new NetEdge.NBTBuilder<>(longPosMap, gEdgeTag, this::addEdge));
        }
        NBTTagList allNetGroups = nbt.getTagList("NetGroups", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < allNetGroups.tagCount(); i++) {
            NBTTagCompound gTag = allNetGroups.getCompoundTagAt(i);
            this.builders.add(new NetGroup.NBTBuilder<>(longPosMap, gTag, this.pipeGraph, this));
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        NBTTagList allPipeNodes = new NBTTagList();
        Set<NetGroup<PipeType, NodeDataType, Edge>> groups = new ObjectOpenHashSet<>();
        for (NetNode<PipeType, NodeDataType, Edge> node : pipeGraph.vertexSet()) {
            if (node.getGroupUnsafe() != null) groups.add(node.getGroupUnsafe());
            NBTTagCompound nodeTag = node.serializeNBT();
            NBTTagCompound dataTag = new NBTTagCompound();
            writeNodeData(node.getData(), dataTag);
            nodeTag.setTag("Data", dataTag);
            allPipeNodes.appendTag(nodeTag);
        }
        compound.setTag("PipeNodes", allPipeNodes);

        NBTTagList allNetEdges = new NBTTagList();
        for (Edge edge : pipeGraph.edgeSet()) {
            allNetEdges.appendTag(edge.serializeNBT());
        }
        compound.setTag("NetEdges", allNetEdges);

        NBTTagList allNetGroups = new NBTTagList();
        for (NetGroup<PipeType, NodeDataType, Edge> group : groups) {
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
