package gregtech.api.graphnet.group;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.net.NetNode;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

public class GroupGraphView implements Graph<GraphVertex, GraphEdge> {

    protected final @NotNull NetGroup group;

    protected final EdgeSetView edgeView = new EdgeSetView();

    protected final Set<GraphVertex> addedVertices = new ObjectOpenHashSet<>();

    public GroupGraphView(@NotNull NetGroup group) {
        this.group = group;
    }

    protected INetGraph backer() {
        return group.net.getGraph();
    }

    @Override
    public Set<GraphEdge> getAllEdges(GraphVertex sourceVertex, GraphVertex targetVertex) {
        return backer().getAllEdges(sourceVertex, targetVertex);
    }

    @Override
    public GraphEdge getEdge(GraphVertex sourceVertex, GraphVertex targetVertex) {
        return backer().getEdge(sourceVertex, targetVertex);
    }

    @Override
    public Supplier<GraphVertex> getVertexSupplier() {
        return backer().getVertexSupplier();
    }

    @Override
    public Supplier<GraphEdge> getEdgeSupplier() {
        return backer().getEdgeSupplier();
    }

    @Override
    public GraphEdge addEdge(GraphVertex sourceVertex, GraphVertex targetVertex) {
        return backer().addEdge(sourceVertex, targetVertex);
    }

    @Override
    public boolean addEdge(GraphVertex sourceVertex, GraphVertex targetVertex, GraphEdge graphEdge) {
        return backer().addEdge(sourceVertex, targetVertex, graphEdge);
    }

    @Override
    public GraphVertex addVertex() {
        GraphVertex vertex = backer().addVertex();
        addedVertices.add(vertex);
        return vertex;
    }

    @Override
    public boolean addVertex(GraphVertex vertex) {
        addedVertices.add(vertex);
        return backer().addVertex(vertex);
    }

    @Override
    public boolean containsEdge(GraphVertex sourceVertex, GraphVertex targetVertex) {
        return containsVertex(sourceVertex) && containsVertex(targetVertex) &&
                backer().containsEdge(sourceVertex, targetVertex);
    }

    @Override
    public boolean containsEdge(GraphEdge graphEdge) {
        return containsVertex(graphEdge.getSource()) && containsVertex(graphEdge.getTarget()) &&
                backer().containsEdge(graphEdge);
    }

    @Override
    public boolean containsVertex(GraphVertex vertex) {
        return addedVertices.contains(vertex) || group.getNodes().contains(NetNode.unwrap(vertex));
    }

    @Override
    public Set<GraphEdge> edgeSet() {
        return edgeView;
    }

    @Override
    public int degreeOf(GraphVertex vertex) {
        if (backer().isDirected()) return inDegreeOf(vertex) + outDegreeOf(vertex);
        int degree = 0;
        Set<GraphEdge> edges = backer().edgesOf(vertex);
        for (GraphEdge e : edges) {
            if (!containsEdge(e)) continue;
            if (backer().getEdgeSource(e).equals(backer().getEdgeTarget(e))) {
                degree += 2;
            } else {
                degree += 1;
            }
        }
        return degree;
    }

    @Override
    public Set<GraphEdge> edgesOf(GraphVertex vertex) {
        Set<GraphEdge> s = new ObjectOpenHashSet<>(backer().edgesOf(vertex));
        s.removeIf(e -> !containsEdge(e));
        return s;
    }

    @Override
    public int inDegreeOf(GraphVertex vertex) {
        if (!backer().isDirected()) return degreeOf(vertex);
        return incomingEdgesOf(vertex).size();
    }

    @Override
    public Set<GraphEdge> incomingEdgesOf(GraphVertex vertex) {
        Set<GraphEdge> s = new ObjectOpenHashSet<>(backer().incomingEdgesOf(vertex));
        s.removeIf(e -> !containsEdge(e));
        return s;
    }

    @Override
    public int outDegreeOf(GraphVertex vertex) {
        if (!backer().isDirected()) return degreeOf(vertex);
        return outgoingEdgesOf(vertex).size();
    }

    @Override
    public Set<GraphEdge> outgoingEdgesOf(GraphVertex vertex) {
        Set<GraphEdge> s = new ObjectOpenHashSet<>(backer().outgoingEdgesOf(vertex));
        s.removeIf(e -> !containsEdge(e));
        return s;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends GraphEdge> edges) {
        return backer().removeAllEdges(edges);
    }

    @Override
    public Set<GraphEdge> removeAllEdges(GraphVertex sourceVertex, GraphVertex targetVertex) {
        return backer().removeAllEdges(sourceVertex, targetVertex);
    }

    @Override
    public boolean removeAllVertices(Collection<? extends GraphVertex> vertices) {
        return backer().removeAllVertices(vertices) | addedVertices.removeAll(vertices);
    }

    @Override
    public GraphEdge removeEdge(GraphVertex sourceVertex, GraphVertex targetVertex) {
        return backer().removeEdge(sourceVertex, targetVertex);
    }

    @Override
    public boolean removeEdge(GraphEdge graphEdge) {
        return backer().removeEdge(graphEdge);
    }

    @Override
    public boolean removeVertex(GraphVertex vertex) {
        return backer().removeVertex(vertex) | addedVertices.remove(vertex);
    }

    @Override
    public Set<GraphVertex> vertexSet() {
        Set<GraphVertex> set = new ObjectOpenHashSet<>(group.getNodes().size() + addedVertices.size());
        set.addAll(addedVertices);
        for (NetNode node : group.getNodes()) {
            set.add(GraphVertex.unwrap(node));
        }
        return set;
    }

    @Override
    public GraphVertex getEdgeSource(GraphEdge graphEdge) {
        return backer().getEdgeSource(graphEdge);
    }

    @Override
    public GraphVertex getEdgeTarget(GraphEdge graphEdge) {
        return backer().getEdgeTarget(graphEdge);
    }

    @Override
    public GraphType getType() {
        return backer().getType();
    }

    @Override
    public double getEdgeWeight(GraphEdge graphEdge) {
        return backer().getEdgeWeight(graphEdge);
    }

    @Override
    public void setEdgeWeight(GraphEdge graphEdge, double weight) {
        backer().setEdgeWeight(graphEdge, weight);
    }

    @Override
    public void setEdgeWeight(GraphVertex sourceVertex, GraphVertex targetVertex, double weight) {
        backer().setEdgeWeight(sourceVertex, targetVertex, weight);
    }

    private final class EdgeSetView extends AbstractSet<GraphEdge> {

        @Override
        public @NotNull Iterator<GraphEdge> iterator() {
            return new Iterator<>() {

                final Iterator<GraphEdge> backer = group.net.getGraph().edgeSet().iterator();
                GraphEdge next;

                @Override
                public boolean hasNext() {
                    if (next != null) return true;
                    return calcNext();
                }

                @Override
                public GraphEdge next() {
                    if (next == null) {
                        if (!calcNext()) throw new NoSuchElementException();
                    }
                    GraphEdge e = next;
                    next = null;
                    return e;
                }

                private boolean calcNext() {
                    do {
                        if (!backer.hasNext()) return false;
                        next = backer.next();
                    } while (!containsEdge(next));
                    return true;
                }
            };
        }

        @Override
        public int size() {
            int size = 0;
            for (GraphEdge ignored : this) {
                size++;
            }
            return size;
        }
    }
}
