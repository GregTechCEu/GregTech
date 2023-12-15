package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;

import gregtech.api.pipenet.tile.IPipeTile;

import gregtech.api.pipenet.tile.TileEntityPipeBase;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class WorldPipeNetG<NodeDataType, PipeType extends Enum<PipeType> & IPipeType<NodeDataType>> extends WorldSavedData {

    // create an executor for graph algorithm. Allows 2 threads per JVM processor, and keeps them alive for 5 seconds.
    // note - should this be shut down at some point during server shutdown?
    public static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() * 2, 5, TimeUnit.SECONDS, new SynchronousQueue<>());

    private WeakReference<World> worldRef = new WeakReference<>(null);
    private final Graph<NodeG<PipeType, NodeDataType>, NetEdge> backingGraph;
    private final ListenableGraph<NodeG<PipeType, NodeDataType>, NetEdge> pipeGraph;
    private final ConnectivityInspector<NodeG<PipeType, NodeDataType>, NetEdge> graphInspector;
    private final Map<BlockPos, NodeG<PipeType, NodeDataType>> pipeMap = new Object2ObjectOpenHashMap<>();

    private ShortestPathsAlgorithm<PipeType, NodeDataType> shortestPaths;
    // this is a monstrosity
    private final Map<NodeG<PipeType, NodeDataType>, List<GraphPath<NodeG<PipeType, NodeDataType>, NetEdge>>> shortestPathsCache = new Object2ObjectOpenHashMap<>();
    private boolean validPathsCache = false;

    public WorldPipeNetG(String name) {
        super(name);
        this.backingGraph = new SimpleWeightedGraph<>(NetEdge.class);
        // multithreading? in this economy?
        this.pipeGraph = new DefaultListenableGraph<>(this.backingGraph, true);
        this.graphInspector = new ConnectivityInspector<>(this.pipeGraph);
        this.pipeGraph.addGraphListener(this.graphInspector);
    }

    public World getWorld() {
        return this.worldRef.get();
    }

    protected void setWorldAndInit(World world) {
        if (world != this.worldRef.get()) {
            this.worldRef = new WeakReference<>(world);
            onWorldSet(world);
        }
    }

    public static String getDataID(final String baseID, final World world) {
        if (world == null || world.isRemote)
            throw new RuntimeException("WorldPipeNetG should only be created on the server!");
        int dimension = world.provider.getDimension();
        return dimension == 0 ? baseID : baseID + '.' + dimension;
    }

    protected void onWorldSet(World world) {
        this.rebuildShortestPaths();
    }

    /**
     * Preferred override, reduces operational cost
     * @param tile The {@link TileEntityPipeBase} that paths are being requested for
     * @return the ordered list of paths associated with the {@link TileEntityPipeBase}
     */
    public List<GraphPath<NodeG<PipeType, NodeDataType>, NetEdge>> getPaths(TileEntityPipeBase<PipeType, NodeDataType> tile) {
        return getPaths(new NodeG<>(tile.getPipePos()), tile);
    }

    /**
     * Special-case override
     * @param pos The {@link BlockPos} that paths are being requested for
     * @return the ordered list of paths associated with the {@link BlockPos}
     */
    public List<GraphPath<NodeG<PipeType, NodeDataType>, NetEdge>> getPaths(BlockPos pos) {
        return getPaths(new NodeG<>(pos), null);
    }

    public List<GraphPath<NodeG<PipeType, NodeDataType>, NetEdge>> getPaths(
            NodeG<PipeType, NodeDataType> node, @Nullable TileEntityPipeBase<PipeType, NodeDataType> tile) {
        node.heldMTE = tile;

        if (!this.validPathsCache) {
            this.rebuildShortestPaths();
            this.shortestPathsCache.clear();
        }

        List<GraphPath<NodeG<PipeType, NodeDataType>, NetEdge>> cache =
                this.shortestPathsCache.get(node);
        if (cache != null) return verifyList(cache, node);

        List<GraphPath<NodeG<PipeType, NodeDataType>, NetEdge>> list = this.shortestPaths.getPathsList(node);
        this.shortestPathsCache.put(node, list);
        return verifyList(list, node);
    }

    protected List<GraphPath<NodeG<PipeType, NodeDataType>, NetEdge>> verifyList(List<GraphPath<NodeG<PipeType, NodeDataType>, NetEdge>> list, NodeG<PipeType, NodeDataType> source) {
        if (!verifyNode(source)) return new ObjectArrayList<>();
        return list.stream().filter(a -> verifyNode(a.getEndVertex())).collect(Collectors.toList());
    }

    protected boolean verifyNode(NodeG<PipeType, NodeDataType> node) {
        if (!getWorld().loadedTileEntityList.contains(node.heldMTE)) {
            TileEntityPipeBase<PipeType, NodeDataType> pipe = castTE(getWorld().getTileEntity(node.getNodePos()));
            if (pipe == null) return false;
            node.heldMTE = pipe;
        }
        return true;
    }

    protected TileEntityPipeBase<PipeType, NodeDataType> castTE(TileEntity te) {
        if (te instanceof TileEntityPipeBase<?, ?> pipe) {
            if (!getBasePipeClass().isAssignableFrom(pipe.getClass())) {
                return null;
            }
            return (TileEntityPipeBase<PipeType, NodeDataType>) pipe;
        }
        return null;
    }

    protected abstract Class<IPipeTile<PipeType, NodeDataType>> getBasePipeClass();

    public void addNode(BlockPos nodePos, NodeDataType nodeData, int mark, int openConnections, boolean isActive, TileEntityPipeBase<PipeType, NodeDataType> heldMTE) {
        NodeG<PipeType, NodeDataType> node = new NodeG<>(nodeData, openConnections, mark, isActive, heldMTE);
        if (!canAttachNode(nodeData)) return;

        this.addNode(node);
        addEdges(node);

    }

    private void addEdges(NodeG<PipeType, NodeDataType> node) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos offsetPos = node.getNodePos().offset(facing);
            NodeG<PipeType, NodeDataType> nodeOffset = this.pipeMap.get(offsetPos);
            if (nodeOffset != null && this.canNodesConnect(node, facing, nodeOffset)) {
                this.addEdge(node, nodeOffset);
            }
        }
    }

    protected final boolean canNodesConnect(NodeG<PipeType, NodeDataType> node1, EnumFacing nodeFacing, NodeG<PipeType, NodeDataType> node2) {
        return areNodeBlockedConnectionsCompatible(node1, nodeFacing, node2) &&
                areMarksCompatible(node1.mark, node2.mark) && areNodesCustomContactable(node1.data, node2.data);
    }

    private static boolean areMarksCompatible(int mark1, int mark2) {
        return mark1 == mark2 || mark1 == Node.DEFAULT_MARK || mark2 == Node.DEFAULT_MARK;
    }

    private boolean areNodeBlockedConnectionsCompatible(NodeG<PipeType, NodeDataType> node1, EnumFacing nodeFacing, NodeG<PipeType, NodeDataType> node2) {
        return !node1.isBlocked(nodeFacing) && !node2.isBlocked(nodeFacing.getOpposite());
    }

    protected boolean areNodesCustomContactable(NodeDataType node1, NodeDataType node2) {
        return true;
    }

    protected boolean canAttachNode(NodeDataType nodeData) {
        return true;
    }

    public void updateBlockedConnections(BlockPos nodePos, EnumFacing side, boolean isBlocked) {
        NodeG<PipeType, NodeDataType> node = pipeMap.get(nodePos);
        if (node == null || node.isBlocked(side) == isBlocked) return;

        node.setBlocked(side, isBlocked);
        NodeG<PipeType, NodeDataType> nodeOffset = pipeMap.get(nodePos.offset(side));
        if (nodeOffset == null) return;

        if (!node.isBlocked(side) && !nodeOffset.isBlocked(side.getOpposite())) {
            addEdge(node, nodeOffset);
        } else {
            removeEdge(node, nodeOffset);
        }
    }

    public void updateMark(BlockPos nodePos, int newMark) {
        NodeG<PipeType, NodeDataType> node = pipeMap.get(nodePos);
        if (node == null) return;

        int oldMark = node.mark;
        node.mark = newMark;

        for (EnumFacing side : EnumFacing.VALUES) {
            NodeG<PipeType, NodeDataType> nodeOffset = pipeMap.get(nodePos.offset(side));
            if (nodeOffset == null) continue;
            if (!areNodeBlockedConnectionsCompatible(node, side, nodeOffset) ||
                    !areNodesCustomContactable(node.data, nodeOffset.data)) continue;
            if (areMarksCompatible(oldMark, nodeOffset.mark) == areMarksCompatible(newMark, nodeOffset.mark)) continue;

            if (areMarksCompatible(newMark, nodeOffset.mark)) {
                addEdge(node, nodeOffset);
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
        this.validPathsCache = false;
    }

    public void addNode(NodeG<PipeType, NodeDataType> node) {
        addNodeSilent(node);
        this.markDirty();
    }

    public void addEdge(NodeG<PipeType, NodeDataType> node1, NodeG<PipeType, NodeDataType> node2) {
        addEdge(node1, node2, 1);
        this.validPathsCache = false;
    }

    public void addEdge(NodeG<PipeType, NodeDataType> node1, NodeG<PipeType, NodeDataType> node2, double weight) {
        if (pipeGraph.addEdge(node1, node2) != null) {
            pipeGraph.setEdgeWeight(node1, node2, weight);
            this.validPathsCache = false;
            this.markDirty();
        }
    }

    public void removeEdge(NodeG<PipeType, NodeDataType> node1, NodeG<PipeType, NodeDataType> node2) {
        if (pipeGraph.removeEdge(node1, node2) != null) {
            this.validPathsCache = false;
            this.markDirty();
        }
    }

    public void removeNode(NodeG<PipeType, NodeDataType> node) {
        if (pipeGraph.removeVertex(node)) {
            this.validPathsCache = false;
            this.markDirty();
        }
    }

    protected void rebuildShortestPaths() {
        this.shortestPaths = new ShortestPathsAlgorithm<>(pipeGraph, EXECUTOR, graphInspector);
        this.validPathsCache = true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList allPipeNodes = nbt.getTagList("PipeNodes", Constants.NBT.TAG_COMPOUND);
        NBTTagList allNetEdges = nbt.getTagList("NetEdges", Constants.NBT.TAG_COMPOUND);
        Map<Long, NodeG<PipeType, NodeDataType>> longPosMap = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < allPipeNodes.tagCount(); i++) {
            NBTTagCompound pNodeTag = allPipeNodes.getCompoundTagAt(i);
            NodeG<PipeType, NodeDataType> node = new NodeG<>(pNodeTag, this);
            longPosMap.put(node.getLongPos(), node);
            this.addNodeSilent(node);
        }
        for (int i = 0; i < allNetEdges.tagCount(); i++) {
            NBTTagCompound gEdgeTag = allNetEdges.getCompoundTagAt(i);
            new NetEdge.Builder<>(longPosMap, gEdgeTag, this::addEdge).addIfBuildable();
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        NBTTagList allPipeNodes = new NBTTagList();
        for (NodeG<PipeType, NodeDataType> node : pipeGraph.vertexSet()) {
            NBTTagCompound nodeTag = node.serializeNBT();
            NBTTagCompound dataTag = new NBTTagCompound();
            writeNodeData(node.data, dataTag);
            nodeTag.setTag("Data", dataTag);
            allPipeNodes.appendTag(nodeTag);
        }
        compound.setTag("PipeNodes", allPipeNodes);
        
        NBTTagList allNetEdges = new NBTTagList();
        for (NetEdge edge : pipeGraph.edgeSet()) {
            allNetEdges.appendTag(edge.serializeNBT());
        }
        compound.setTag("NetEdges", allNetEdges);
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

    // CHManyToManyShortestPaths is a very good algorithm because our graph will be extremely sparse.
    protected static final class ShortestPathsAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT> extends CHManyToManyShortestPaths<NodeG<PT, NDT>, NetEdge> {

        private final ConnectivityInspector<NodeG<PT, NDT>, NetEdge> inspector;

        public ShortestPathsAlgorithm(Graph<NodeG<PT, NDT>, NetEdge> graph, ThreadPoolExecutor executor, ConnectivityInspector<NodeG<PT, NDT>, NetEdge> inspector) {
            super(graph, executor);
            this.inspector = inspector;
        }

        public List<GraphPath<NodeG<PT, NDT>, NetEdge>> getPathsList(NodeG<PT, NDT> source) {
            if (!graph.containsVertex(source)) {
                throw new IllegalArgumentException("graph must contain the source vertex");
            }
            ManyToManyShortestPaths<NodeG<PT, NDT>, NetEdge> manyToManyPaths = getManyToManyPaths(Collections.singleton(source), inspector.connectedSetOf(source));
            List<GraphPath<NodeG<PT, NDT>, NetEdge>> paths = new ObjectArrayList<>();
            for (NodeG<PT, NDT> v : inspector.connectedSetOf(source)) {
                // update mutably so that the returned paths are also updated
                if (v.equals(source)) {
                    source.sync(v);
                    continue;
                }

                GraphPath<NodeG<PT, NDT>, NetEdge> path = manyToManyPaths.getPath(source, v);
                if (path != null) {
                    paths.add(path);
                }
            }
            paths.sort(Comparator.comparingDouble(GraphPath::getWeight));
            return paths;
        }
    }
}
