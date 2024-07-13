package gregtech.api.graphnet;

import gregtech.api.graphnet.alg.INetAlgorithm;
import gregtech.api.graphnet.alg.NetAlgorithmWrapper;
import gregtech.api.graphnet.alg.NetPathMapper;
import gregtech.api.graphnet.alg.iter.ICacheableIterator;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.INetGraph;

import gregtech.api.graphnet.graph.GraphVertex;

import gregtech.api.graphnet.path.INetPath;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

/**
 * The bridge between JGraphT graphs and graphnet abstractions.
 * Doesn't do any automatic linking, weighting, predicating, etc. Simply handles storing the JGraphT graph to disk,
 * interfacing with graph algorithms, and interacting with the JGraphT graph.
 */
public final class GraphNetBacker {

    private final INetGraph pipeGraph;
    private final Object2ObjectOpenHashMap<Object, GraphVertex> vertexMap;
    private final NetAlgorithmWrapper netAlgorithm;

    public GraphNetBacker(IGraphNet backedNet, Function<IGraphNet, @NotNull INetAlgorithm> algorithmBuilder,
                          INetGraph graph) {
        graph.setOwningNet(this);
        this.pipeGraph = graph;
        this.netAlgorithm = new NetAlgorithmWrapper(backedNet, algorithmBuilder);
        this.vertexMap = new Object2ObjectOpenHashMap<>();
    }

    public void addNode(NetNode node) {
        GraphVertex vertex = new GraphVertex(node);
        getGraph().addVertex(vertex);
        this.vertexMap.put(node.getEquivalencyData(), vertex);
    }

    @Nullable
    public NetNode getNode(Object equivalencyData) {
        GraphVertex vertex = this.vertexMap.get(equivalencyData);
        return vertex == null ? null : vertex.wrapped;
    }

    public boolean removeNode(@Nullable NetNode node) {
        if (node != null) {
            if (this.getGraph().edgesOf(node.wrapper).size() != 0) this.invalidateAlg();
            if (node.getGroupUnsafe() != null) {
                node.getGroupUnsafe().splitNode(node);
            } else this.removeVertex(node.wrapper);
            return true;
        } else return false;
    }

    @ApiStatus.Internal
    public void removeVertex(GraphVertex vertex) {
        this.getGraph().removeVertex(vertex);
        this.vertexMap.remove(vertex.wrapped.getEquivalencyData());
    }

    @Nullable
    public NetEdge addEdge(NetNode source, NetNode target, double weight) {
        GraphEdge graphEdge = getGraph().addEdge(source.wrapper, target.wrapper);
        if (graphEdge != null) getGraph().setEdgeWeight(graphEdge, weight);
        return graphEdge == null ? null : graphEdge.wrapped;
    }

    @Nullable
    public NetEdge getEdge(NetNode source, NetNode target) {
        GraphEdge graphEdge = getGraph().getEdge(source.wrapper, target.wrapper);
        return graphEdge == null ? null : graphEdge.wrapped;
    }

    public boolean removeEdge(NetNode source, NetNode target) {
        if (source.getGroupUnsafe() == null) {
            return removeEdge(source.wrapper, target.wrapper) != null;
        } else return source.getGroupUnsafe().splitEdge(source, target);
    }

    @ApiStatus.Internal
    public GraphEdge removeEdge(GraphVertex source, GraphVertex target) {
        return this.getGraph().removeEdge(source, target);
    }

    @ApiStatus.Internal
    public void removeEdge(GraphEdge edge) {
        this.getGraph().removeEdge(edge);
    }

    public boolean dynamicWeights() {
        return netAlgorithm.getNet().usesDynamicWeights() &&  netAlgorithm.supportsDynamicWeights();
    }

    /**
     * Note - if an error is thrown with a stacktrace descending from this method,
     * most likely a bad remapper was passed in. <br>
     * This method should never be exposed outside the net this backer is backing due to this fragility.
     */
    public <Path extends INetPath<?, ?>> Iterator<Path> getPaths(@Nullable NetNode node, @NotNull NetPathMapper<Path> remapper, IPredicateTestObject testObject, @Nullable SimulatorKey simulator, long queryTick) {
        if (node == null) return Collections.emptyIterator();

        Iterator<? extends INetPath<?, ?>> cache = node.getPathCache();
        if (cache != null) return (Iterator<Path>) cache;

        Iterator<Path> iter = this.netAlgorithm.getPathsIterator(node.wrapper, remapper, testObject, simulator, queryTick);
        if (iter instanceof ICacheableIterator) {
            return (Iterator<Path>) node.setPathCache((ICacheableIterator<Path>) iter);
        } else return iter;
    }

    public void invalidateAlg() {
        this.netAlgorithm.invalidate();
    }

    public INetGraph getGraph() {
        return pipeGraph;
    }

    // PROPOSAL FOR ALTERNATIVE STORAGE MECHANISM TO REDUCE MEMORY COSTS
    // > Always loaded & nbt stored data:
    // map & weak map of group ids to groups. No references to group objects exist outside of this, only references to grou ids.
    // (for pipenet) pipes store a reference to their group id
    // > Disk-stored data:
    // contents of groups, specifically their nodes and edges.
    // > Impl (for pipenet)
    // When a pipe is loaded, it goes fetch its group and tells it the pipe's chunk. This chunk is added to a *set* of chunks that are 'loading' this group.
    // When a chunk is unloaded, it is removed from the set of 'loading' chunks for all groups.
    // When the set of 'loading' chunks for a group is empty, the group writes its data to disk and removes itself from the map and the graph but not the weak map.
    // (proposal - create a graph impl that allows for weak references to vertices and edges, in order to remove need for explicit removal of group from graph?)
    // When a pipe fetches its group, if the group is not found in the map, it then checks the weak map.
    // If found in the weak map, the pipe's chunk is added to the 'loading' chunks and a reference to the group is added to the map and the contents are added to the graph.
    // If not found in the weak map, the group is instead read from disk and initialized.
    // > Benefits of this Impl
    // By only loading the (potentially) large number of edges and nodes into the graph that a group contains when that group is needed,
    // the number of unnecessary references in the graphnet on, say, a large server is drastically reduced.
    // however, since there are necessarily more read/write actions to disk, the cpu load would increase in turn.

    public void readFromNBT(NBTTagCompound nbt) {
        // construct map of node ids -> nodes, while building nodes to groups
        // construct edges using map
        Int2ObjectOpenHashMap<NetGroup> groupMap = new Int2ObjectOpenHashMap<>();
        NBTTagCompound vertices = nbt.getCompoundTag("Vertices");
        int vertexCount = vertices.getInteger("Count");
        Int2ObjectOpenHashMap<GraphVertex> vertexMap = new Int2ObjectOpenHashMap<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            NBTTagCompound tag = vertices.getCompoundTag(String.valueOf(i));
            NetNode node = this.netAlgorithm.getNet().getNewNode();
            node.deserializeNBT(tag);
            if (tag.hasKey("GroupID")) {
                int id = tag.getInteger("GroupID");
                NetGroup group = groupMap.get(id);
                if (group == null) {
                    group = new NetGroup(this.netAlgorithm.getNet());
                    groupMap.put(id, group);
                }
                group.addNode(node);
            }
            GraphVertex vertex = new GraphVertex(node);
            this.getGraph().addVertex(vertex);
            vertexMap.put(i, vertex);
            this.vertexMap.put(node.getEquivalencyData(), vertex);
        }

        NBTTagCompound edges = nbt.getCompoundTag("Edges");
        int edgeCount = edges.getInteger("Count");
        for (int i = 0; i < edgeCount; i++) {
            NBTTagCompound tag = edges.getCompoundTag(String.valueOf(i));
            GraphEdge graphEdge = this.getGraph().addEdge(vertexMap.get(tag.getInteger("SourceID")), vertexMap.get(tag.getInteger("TargetID")));
            this.getGraph().setEdgeWeight(graphEdge, tag.getDouble("Weight"));
            graphEdge.wrapped.deserializeNBT(tag);
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        // map of net groups -> autogenerated ids;
        // tag of autogenerated vertex ids -> node nbt & group id
        // tag of autogenerated edge ids -> edge nbt & source/target ids
        Object2IntOpenHashMap<NetGroup> groupMap = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<GraphVertex> vertexMap = new Object2IntOpenHashMap<>();
        int i = -1;
        int g = 0;
        NBTTagCompound vertices = new NBTTagCompound();
        for (GraphVertex graphVertex : this.getGraph().vertexSet()) {
            vertexMap.put(graphVertex, i++);
            NetGroup group = graphVertex.wrapped.getGroupUnsafe();
            NBTTagCompound tag = graphVertex.wrapped.serializeNBT();
            if (group != null) {
                int groupID;
                if (!groupMap.containsKey(group)) {
                    groupMap.put(group, g++);
                    groupID = g;
                } else groupID = groupMap.getInt(group);
                tag.setInteger("GroupID", groupID);
            }
            vertices.setTag(String.valueOf(i), tag);
        }
        vertices.setInteger("Count", i + 1);
        compound.setTag("Vertices", vertices);

        i = -1;
        NBTTagCompound edges = new NBTTagCompound();
        for (GraphEdge graphEdge : this.getGraph().edgeSet()) {
            NBTTagCompound tag = graphEdge.wrapped.serializeNBT();
            tag.setInteger("SourceID", vertexMap.getInt(getSource(graphEdge)));
            tag.setInteger("TargetID", vertexMap.getInt(getTarget(graphEdge)));
            tag.setDouble("Weight", getWeight(graphEdge));
            edges.setTag(String.valueOf(i++), tag);
        }
        edges.setInteger("Count", i + 1);
        compound.setTag("Edges", edges);

        return compound;
    }

    private GraphVertex getSource(GraphEdge graphEdge) {
        return this.getGraph().getEdgeSource(graphEdge);
    }

    private GraphVertex getTarget(GraphEdge graphEdge) {
        return this.getGraph().getEdgeTarget(graphEdge);
    }

    private double getWeight(GraphEdge graphEdge) {
        return this.getGraph().getEdgeWeight(graphEdge);
    }
}
