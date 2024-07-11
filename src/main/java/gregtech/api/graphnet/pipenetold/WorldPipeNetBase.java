package gregtech.api.graphnet.pipenetold;

import gregtech.api.cover.Cover;
import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetGroup;
import gregtech.api.graphnet.alg.INetAlgorithm;
import gregtech.api.graphnet.alg.iter.ICacheableIterator;
import gregtech.api.graphnet.pipenetold.block.IPipeType;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.pipenetold.predicate.ShutteredEdgePredicate;
import gregtech.api.graphnet.pipenetold.predicate.IShutteredEdgePredicate;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;
import gregtech.api.graphnet.pipenetold.tile.TileEntityPipeBase;
import gregtech.common.covers.CoverShutter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.capabilities.Capability;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public abstract class WorldPipeNetBase<NodeDataType extends IPipeNetData<NodeDataType>,
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, Edge extends NetEdge> extends WorldSavedData
        implements IGraphNet {


    private final boolean isDirected;

    private WeakReference<World> worldRef = new WeakReference<>(null);
    protected final Map<BlockPos, PipeNetNode<PipeType, NodeDataType, Edge>> pipeMap = new Object2ObjectOpenHashMap<>();

    private final GraphNetBacker<PipeNetPath<PipeType, NodeDataType, Edge>> backer;

    WorldPipeNetBase(String name, boolean isDirected,
                     Function<IGraphNet, INetAlgorithm> algorithmBuilder,
                     INetGraph graph) {
        super(name);
        this.backer = new GraphNetBacker(this, algorithmBuilder, graph, this.needsDynamicWeights());
        this.isDirected = isDirected;
    }

    @Override
    public INetGraph getGraph() {
        return this.backer.getGraph();
    }

    public final boolean isDirected() {
        return isDirected;
    }

    protected void markAlgInvalid() {
        this.backer.invalidateAlg();
    }

    public World getWorld() {
        return this.worldRef.get();
    }

    protected boolean needsDynamicWeights() {
        return false;
    }

    protected void setWorldAndInit(World world) {
        if (world != this.worldRef.get()) {
            this.worldRef = new WeakReference<>(world);
        }
    }

    public static String getDataID(final String baseID, final World world) {
        if (world == null || world.isRemote)
            throw new RuntimeException("WorldPipeNetBase should only be created on the server!");
        int dimension = world.provider.getDimension();
        return dimension == 0 ? baseID : baseID + '.' + dimension;
    }

    /**
     * Preferred override. Only collects a fresh TE from the server if the provided TE is invalid.
     * 
     * @param tile The {@link TileEntityPipeBase} that paths are being requested for
     * @return the ordered list of paths associated with the {@link TileEntityPipeBase}
     */
    public Iterator<PipeNetPath<PipeType, NodeDataType, Edge>> getPaths(TileEntityPipeBase<PipeType, NodeDataType, Edge> tile) {
        return getPaths(this.pipeMap.get(tile.getPipePos()), tile);
    }

    /**
     * Special-case override. Forces the collection of a fresh TE from the server.
     * 
     * @param pos The {@link BlockPos} that paths are being requested for
     * @return the ordered list of paths associated with the {@link BlockPos}
     */
    public Iterator<PipeNetPath<PipeType, NodeDataType, Edge>> getPaths(BlockPos pos) {
        return getPaths(this.pipeMap.get(pos), null);
    }

    public Iterator<PipeNetPath<PipeType, NodeDataType, Edge>> getPaths(@Nullable PipeNetNode<PipeType, NodeDataType, Edge> node,
                                                                        @Nullable TileEntityPipeBase<PipeType, NodeDataType, Edge> tile) {
        if (node == null) return Collections.emptyIterator();

        node.setHeldMTE(tile);

        Iterator<?> returnable = node.getPathCache();
        if (returnable == null) {
            returnable = this.backer.getPaths(node, MAPPER);
            if (returnable instanceof ICacheableIterator) {
                returnable = node.setPathCache((ICacheableIterator<?>) returnable);
            }
        }
        return (Iterator<PipeNetPath<PipeType, NodeDataType, Edge>>) returnable;
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

    public void markNodeAsOldData(PipeNetNode<PipeType, NodeDataType, Edge> node) {
        updateActiveNodeStatus(node);
    }

    protected abstract Class<? extends IPipeTile<PipeType, NodeDataType, Edge>> getBasePipeClass();

    public PipeNetNode<PipeType, NodeDataType, Edge> getOrCreateNode(@NotNull IPipeTile<PipeType, NodeDataType, Edge> mte) {
        PipeNetNode<PipeType, NodeDataType, Edge> node = this.pipeMap.get(mte.getPipePos());
        if (node != null) return node;
        if (!canAttachNode(mte.getNodeData())) return null;
        node = new PipeNetNode<>(mte.getNodeData(), mte, this);
        this.addNode(node);
        return node;
    }

    protected final boolean canNodesConnect(PipeNetNode<PipeType, NodeDataType, Edge> source, EnumFacing nodeFacing,
                                            PipeNetNode<PipeType, NodeDataType, Edge> target) {
        return areNodeBlockedConnectionsCompatible(source, nodeFacing, target) &&
                areMarksCompatible(source.mark, target.mark) &&
                areNodesCustomContactable(source.getData(), target.getData());
    }

    private static boolean areMarksCompatible(int mark1, int mark2) {
        return mark1 == mark2 || mark1 == PipeNetNode.DEFAULT_MARK || mark2 == PipeNetNode.DEFAULT_MARK;
    }

    private boolean areNodeBlockedConnectionsCompatible(PipeNetNode<PipeType, NodeDataType, Edge> source,
                                                        EnumFacing nodeFacing,
                                                        PipeNetNode<PipeType, NodeDataType, Edge> target) {
        return !source.isConnected(nodeFacing) && !target.isConnected(nodeFacing.getOpposite());
    }

    protected boolean areNodesCustomContactable(NodeDataType source, NodeDataType target) {
        return true;
    }

    protected boolean canAttachNode(NodeDataType nodeData) {
        return true;
    }

    public void updateActiveConnections(BlockPos nodePos, EnumFacing side, boolean connect) {
        PipeNetNode<PipeType, NodeDataType, Edge> node = pipeMap.get(nodePos);
        if (node == null || node.isConnected(side) == connect) return;

        node.setConnected(side, connect);
        updateActiveNodeStatus(node);

        PipeNetNode<PipeType, NodeDataType, Edge> nodeOffset = pipeMap.get(nodePos.offset(side));
        if (nodeOffset == null) return;

        nodeOffset.setConnected(side.getOpposite(), connect);

        if (connect) {
            if (!node.isBlocked(side)) {
                addEdge(nodeOffset, node, null);
                this.predicateEdge(nodeOffset, node, side.getOpposite());
                if (!this.getGraph().isDirected()) return;
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
        PipeNetNode<PipeType, NodeDataType, Edge> node = pipeMap.get(nodePos);
        if (node == null || node.isBlocked(side) == blocked) return;

        node.setBlocked(side, blocked);

        PipeNetNode<PipeType, NodeDataType, Edge> nodeOffset = pipeMap.get(nodePos.offset(side));
        if (nodeOffset == null) return;

        if (!blocked) {
            addEdge(nodeOffset, node, null);
            this.predicateEdge(nodeOffset, node, side);
        } else {
            removeEdge(nodeOffset, node);
        }
    }

    public void updateMark(BlockPos nodePos, int newMark) {
        PipeNetNode<PipeType, NodeDataType, Edge> node = pipeMap.get(nodePos);

        int oldMark = node.mark;
        node.mark = newMark;

        for (EnumFacing side : EnumFacing.VALUES) {
            PipeNetNode<PipeType, NodeDataType, Edge> nodeOffset = pipeMap.get(nodePos.offset(side));
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

    public void addNodeSilent(PipeNetNode<PipeType, NodeDataType, Edge> node) {
        backer.addNode(node);
        this.pipeMap.put(node.getNodePos(), node);
        // we do not need to invalidate the cache, because just adding a node means it's not connected to anything.
    }

    public void addNode(PipeNetNode<PipeType, NodeDataType, Edge> node) {
        addNodeSilent(node);
        this.markDirty();
    }

    @Nullable
    public PipeNetNode<PipeType, NodeDataType, Edge> getNode(BlockPos pos) {
        return this.pipeMap.get(pos);
    }

    public void addUndirectedEdge(PipeNetNode<PipeType, NodeDataType, Edge> source,
                                  PipeNetNode<PipeType, NodeDataType, Edge> target) {
        this.addEdge(source, target, null);
        if (this.isDirected()) this.addEdge(target, source, null);
    }

    public void addEdge(PipeNetNode<PipeType, NodeDataType, Edge> source, PipeNetNode<PipeType, NodeDataType, Edge> target,
                        @Nullable IEdgePredicateOld<?> predicate) {
        addEdge(source, target, source.getData().getWeightFactor() + target.getData().getWeightFactor(), predicate);
    }

    public void addUndirectedEdge(PipeNetNode<PipeType, NodeDataType, Edge> source,
                                  PipeNetNode<PipeType, NodeDataType, Edge> target,
                                  double weight) {
        this.addEdge(source, target, weight, null);
        if (this.isDirected()) this.addEdge(target, source, weight, null);
    }

    public void addEdge(PipeNetNode<PipeType, NodeDataType, Edge> source, PipeNetNode<PipeType, NodeDataType, Edge> target,
                        double weight,
                        @Nullable IEdgePredicateOld<?> predicate) {
        NetEdge edge = backer.addEdge(source, target, weight);
        if (edge != null) {
            NetGroup.mergeEdge(source, target);
            if (predicate != null) {
                edge.setPredicate(predicate);
            }
            this.markAlgInvalid();
            this.markDirty();
        }
    }

    public void predicateUndirectedEdge(BlockPos pos, EnumFacing faceToNeighbour) {
        PipeNetNode<PipeType, NodeDataType, Edge> source = this.pipeMap.get(pos);
        PipeNetNode<PipeType, NodeDataType, Edge> target = this.pipeMap.get(pos.offset(faceToNeighbour));
        if (source != null && target != null) {
            this.predicateUndirectedEdge(source, target, faceToNeighbour);
        }
    }

    public void predicateEdge(BlockPos pos, EnumFacing faceToNeighbour) {
        PipeNetNode<PipeType, NodeDataType, Edge> source = this.pipeMap.get(pos);
        PipeNetNode<PipeType, NodeDataType, Edge> target = this.pipeMap.get(pos.offset(faceToNeighbour));
        if (source != null && target != null)
            this.predicateEdge(source, target, faceToNeighbour);
    }

    public void predicateUndirectedEdge(PipeNetNode<PipeType, NodeDataType, Edge> source,
                                        PipeNetNode<PipeType, NodeDataType, Edge> target,
                                        EnumFacing faceToNeighbour) {
        this.predicateEdge(source, target, faceToNeighbour);
        if (this.isDirected()) this.predicateEdge(target, source, faceToNeighbour.getOpposite());
    }

    public void predicateEdge(PipeNetNode<PipeType, NodeDataType, Edge> source,
                              PipeNetNode<PipeType, NodeDataType, Edge> target,
                              EnumFacing faceToNeighbour) {
        NetEdge edge = this.backer.getEdge(source, target);
        if (edge == null) return;
        Cover thisCover = source.getHeldMTESafe().getCoverableImplementation().getCoverAtSide(faceToNeighbour);
        Cover neighbourCover = target.getHeldMTESafe().getCoverableImplementation()
                .getCoverAtSide(faceToNeighbour.getOpposite());
        IEdgePredicateOld<?> predicate = getPredicate(thisCover, neighbourCover);
        predicate.setPosInfo(source.getNodePos(), target.getNodePos());
        edge.setPredicate(predicate);
    }

    protected IEdgePredicateOld<?> getPredicate(Cover thisCover, Cover neighbourCover) {
        return shutterify(new ShutteredEdgePredicate(), thisCover, neighbourCover);
    }

    protected final IEdgePredicateOld<?> shutterify(IEdgePredicateOld<?> predicate, @Nullable Cover thisCover,
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

    public void removeUndirectedEdge(PipeNetNode<PipeType, NodeDataType, Edge> source,
                                     PipeNetNode<PipeType, NodeDataType, Edge> target) {
        this.removeEdge(source, target);
        if (isDirected()) this.removeEdge(target, source);
    }

    public void removeEdge(PipeNetNode<PipeType, NodeDataType, Edge> source, PipeNetNode<PipeType, NodeDataType, Edge> target) {
        if (backer.removeEdge(source, target)) {
            this.markAlgInvalid();
            this.markDirty();
        }
    }

    public void removeNode(BlockPos pos) {
        this.removeNode(this.pipeMap.get(pos));
    }

    public void removeNode(@Nullable PipeNetNode<PipeType, NodeDataType, Edge> node) {
        if (backer.removeNode(node)) {
            this.pipeMap.remove(node.getNodePos());
            this.markDirty();
        }
    }

    public NetGroup getGroup(BlockPos pos) {
        PipeNetNode<PipeType, NodeDataType, Edge> node = this.getNode(pos);
        if (node == null) return null;
        if (node.getGroupSafe() != null) return node.getGroupSafe();
        return node.setGroup(new NetGroup(this));
    }

    public boolean updateActiveNodeStatus(PipeNetNode<PipeType, NodeDataType, Edge> node) {
        return markNodeAsActive(node, shouldNodeBeActive(node));
    }

    public boolean shouldNodeBeActive(PipeNetNode<PipeType, NodeDataType, Edge> node) {
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

    public boolean markNodeAsActive(PipeNetNode<PipeType, NodeDataType, Edge> node, boolean isActive) {
        if (node != null && node.isActive() != isActive) {
            node.getGroupSafe().clearPathCaches();
            node.setActive(isActive);
            this.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        backer.readFromNBT(nbt);
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        return backer.writeToNBT(compound);
    }
}
