package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;

import gregtech.api.pipenet.tile.IPipeTile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

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
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.util.ConcurrencyUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class WorldPipeNetG<NodeDataType, PipeType extends Enum<PipeType> & IPipeType<NodeDataType>> extends WorldSavedData {

    // note - should this be shut down at some point during server shutdown?
    public static ThreadPoolExecutor EXECUTOR =
            ConcurrencyUtil.createThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

    private WeakReference<World> worldRef = new WeakReference<>(null);
    private final Graph<NodeG<PipeType, NodeDataType>, DefaultWeightedEdge> pipeGraph;
    private final Map<BlockPos, NodeG<PipeType, NodeDataType>> pipeMap = new Object2ObjectOpenHashMap<>();

    private ShortestPathsAlgorithm<NodeG<PipeType, NodeDataType>, DefaultWeightedEdge> shortestPaths;
    // this is a monstrosity
    private final Map<NodeG<PipeType, NodeDataType>, Map<NodeG<PipeType, NodeDataType>, GraphPath<NodeG<PipeType, NodeDataType>, DefaultWeightedEdge>>> shortestPathsCache = new Object2ObjectOpenHashMap<>();
    private boolean validPathsCache = true;

    public WorldPipeNetG(String name) {
        super(name);
        this.pipeGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
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
        for (NodeG<PipeType, NodeDataType> node : pipeGraph.vertexSet()) {
            // do initial edge additions & fully initialize nodes
            IPipeTile<PipeType, NodeDataType> pipe = castTE(world.getTileEntity(node.getNodePos()));
            if (pipe != null) {
                node.heldMTE = pipe;
                node.data = pipe.getNodeData();
                addNodeSilent(node);
                addEdges(node);
            }

        }
        rebuildShortestPaths();
    }

    /**
     * Preferred override
     * @param tile The {@link IPipeTile} that paths are being requested for
     * @return the collection of paths associated with the {@link IPipeTile}
     */
    public Map<NodeG<PipeType, NodeDataType>, GraphPath<NodeG<PipeType, NodeDataType>, DefaultWeightedEdge>> getPaths(IPipeTile<PipeType, NodeDataType> tile) {
        return getPaths(new NodeG<>(tile.getPipePos()), tile);
    }

    /**
     * Special-case override
     * @param pos The {@link BlockPos} that paths are being requested for
     * @return the collection of paths associated with the {@link BlockPos}
     */
    public Map<NodeG<PipeType, NodeDataType>, GraphPath<NodeG<PipeType, NodeDataType>, DefaultWeightedEdge>> getPaths(BlockPos pos) {
        return getPaths(new NodeG<>(pos), null);
    }

    public Map<NodeG<PipeType, NodeDataType>, GraphPath<NodeG<PipeType, NodeDataType>, DefaultWeightedEdge>> getPaths(
            NodeG<PipeType, NodeDataType> node, @Nullable IPipeTile<PipeType, NodeDataType> tile) {
        World world = this.worldRef.get();
        if (world == null)
            throw new RuntimeException("The world of a PipeNet should be set before getting paths from it!");

        if (!this.validPathsCache) {
            this.rebuildShortestPaths();
            this.shortestPathsCache.clear();
        }

        if (!node.heldMTE.isValidTile()) {
            if (tile != null) {
                node.heldMTE = tile;
            } else {
                IPipeTile<PipeType, NodeDataType> pipe = castTE(world.getTileEntity(node.getNodePos()));
                if (pipe != null)
                    node.heldMTE = pipe;
            }
        }

        Map<NodeG<PipeType, NodeDataType>, GraphPath<NodeG<PipeType, NodeDataType>, DefaultWeightedEdge>> cache =
                this.shortestPathsCache.get(node);
        if (cache != null) return cache;

        return this.shortestPaths.getPathsMap(node);
    }

    protected IPipeTile<PipeType, NodeDataType> castTE(TileEntity te) {
        if (te instanceof IPipeTile<?, ?> pipe) {
            if (!getBasePipeClass().isAssignableFrom(pipe.getClass())) {
                return null;
            }
            return (IPipeTile<PipeType, NodeDataType>) pipe;
        }
        return null;
    }

    protected abstract Class<IPipeTile<PipeType, NodeDataType>> getBasePipeClass();

    public void addNode(BlockPos nodePos, NodeDataType nodeData, int mark, int openConnections, boolean isActive, IPipeTile<PipeType, NodeDataType> heldMTE) {
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

    public void addEdge(NodeG<PipeType, NodeDataType> node1, NodeG<PipeType, NodeDataType> node2, int weight) {
        pipeGraph.addEdge(node1, node2);
        pipeGraph.setEdgeWeight(node1, node2, weight);
        this.validPathsCache = false;
    }

    public void removeEdge(NodeG<PipeType, NodeDataType> node1, NodeG<PipeType, NodeDataType> node2) {
        if (pipeGraph.removeEdge(node1, node2) != null)
            this.validPathsCache = false;
    }

    public void removeNode(NodeG<PipeType, NodeDataType> node) {
        if (pipeGraph.removeVertex(node)) {
            this.validPathsCache = false;
            this.markDirty();
        }
    }

    protected void rebuildShortestPaths() {
        this.shortestPaths = new ShortestPathsAlgorithm<>(pipeGraph, EXECUTOR);
        this.validPathsCache = true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList allPipeNodes = nbt.getTagList("PipeNodes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < allPipeNodes.tagCount(); i++) {
            NBTTagCompound pNodeTag = allPipeNodes.getCompoundTagAt(i);
            this.addNodeSilent(new NodeG<>(pNodeTag));
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        NBTTagList allPipeNodes = new NBTTagList();
        for (NodeG<PipeType, NodeDataType> node : pipeGraph.vertexSet()) {
            NBTTagCompound nodeTag = node.serializeNBT();
            allPipeNodes.appendTag(nodeTag);
        }
        compound.setTag("PipeNodes", allPipeNodes);
        return compound;
    }

    // CHManyToManyShortestPaths is a very good algorithm because our graph will be extremely sparse.
    protected static class ShortestPathsAlgorithm<V, E> extends CHManyToManyShortestPaths<V, E> {

        public ShortestPathsAlgorithm(Graph<V, E> graph, ThreadPoolExecutor executor) {
            super(graph, executor);
        }

        public Map<V, GraphPath<V, E>> getPathsMap(V source) {
            if (!graph.containsVertex(source)) {
                throw new IllegalArgumentException("graph must contain the source vertex");
            }

            Map<V, GraphPath<V, E>> paths = new HashMap<>();
            for (V v : graph.vertexSet()) {
                paths.put(v, getPath(source, v));
            }
            return paths;
        }
    }
}
