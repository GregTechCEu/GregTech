package gregtech.api.graphnet.graph;

import gregtech.api.util.collection.PairedBiMap;

import com.google.common.collect.BiMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.GraphSpecificsStrategy;
import org.jgrapht.graph.IntrusiveEdgesSpecifics;
import org.jgrapht.graph.specifics.Specifics;
import org.jgrapht.util.ArrayUnenforcedSet;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BaseNetGraph extends AbstractBaseGraph<GraphVertex, GraphEdge> implements INetGraph {

    private static final DefaultGraphType DIRECTED = new DefaultGraphType.Builder()
            .directed().allowMultipleEdges(false).allowSelfLoops(false).weighted(true)
            .build();

    private static final DefaultGraphType UNDIRECTED = new DefaultGraphType.Builder()
            .undirected().allowMultipleEdges(false).allowSelfLoops(false).weighted(true)
            .build();

    protected BaseNetGraph(Supplier<GraphVertex> vertexSupplier, Supplier<GraphEdge> edgeSupplier, boolean directed) {
        super(vertexSupplier, edgeSupplier, directed ? DIRECTED : UNDIRECTED, SpecificsStrategy.INSTANCE);
    }

    private static final class SpecificsStrategy implements GraphSpecificsStrategy<GraphVertex, GraphEdge> {

        public static final SpecificsStrategy INSTANCE = new SpecificsStrategy();

        @Override
        public Function<GraphType, IntrusiveEdgesSpecifics<GraphVertex, GraphEdge>> getIntrusiveEdgesSpecificsFactory() {
            return t -> new IntrusiveEdgesSpecifics<>() {

                final ReferenceOpenHashSet<GraphEdge> edges = new ReferenceOpenHashSet<>();

                @Override
                public GraphVertex getEdgeSource(GraphEdge edge) {
                    return edge.getSource();
                }

                @Override
                public GraphVertex getEdgeTarget(GraphEdge edge) {
                    return edge.getTarget();
                }

                @Override
                public boolean add(GraphEdge edge, GraphVertex sourceVertex, GraphVertex targetVertex) {
                    edge.setSource(sourceVertex);
                    edge.setTarget(targetVertex);
                    return edges.add(edge);
                }

                @Override
                public boolean containsEdge(GraphEdge edge) {
                    return edges.contains(edge);
                }

                @Override
                public Set<GraphEdge> getEdgeSet() {
                    return edges;
                }

                @Override
                public void remove(GraphEdge edge) {
                    edges.remove(edge);
                }

                @Override
                public double getEdgeWeight(GraphEdge edge) {
                    return edge.getWeight();
                }

                @Override
                public void setEdgeWeight(GraphEdge edge, double weight) {
                    edge.setWeight(weight);
                }
            };
        }

        @Override
        public BiFunction<Graph<GraphVertex, GraphEdge>, GraphType, Specifics<GraphVertex, GraphEdge>> getSpecificsFactory() {
            return (g, t) -> t.isDirected() ? new DirectedSpecifics() : new UndirectedSpecifics();
        }

        private static BiMap<GraphVertex, GraphEdge> standardBiMap() {
            return new PairedBiMap<>(Reference2ReferenceOpenHashMap::new, 1);
        }

        private static final class DirectedSpecifics implements Specifics<GraphVertex, GraphEdge> {

            // vertex -> (source -> incoming edge), (target -> outgoing edge)
            final Map<GraphVertex, Pair<BiMap<GraphVertex, GraphEdge>, BiMap<GraphVertex, GraphEdge>>> map = new Reference2ReferenceOpenHashMap<>();

            static final Pair<Map<GraphVertex, GraphEdge>, Map<GraphVertex, GraphEdge>> defaultable = new ImmutablePair<>(
                    Collections.emptyMap(), Collections.emptyMap());

            @Override
            public boolean addVertex(GraphVertex vertex) {
                return map.putIfAbsent(vertex, new ImmutablePair<>(standardBiMap(), standardBiMap())) == null;
            }

            @Override
            public Set<GraphVertex> getVertexSet() {
                return map.keySet();
            }

            @Override
            public Set<GraphEdge> getAllEdges(GraphVertex sourceVertex, GraphVertex targetVertex) {
                GraphEdge e = getEdge(sourceVertex, targetVertex);
                if (e == null) return Collections.emptySet();
                return Collections.singleton(e);
            }

            @Override
            public GraphEdge getEdge(GraphVertex sourceVertex, GraphVertex targetVertex) {
                var fetch = map.get(sourceVertex);
                return fetch == null ? null : fetch.getRight().get(targetVertex);
            }

            @Override
            public boolean addEdgeToTouchingVertices(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                     GraphEdge edge) {
                var fetch = map.get(sourceVertex);
                if (fetch != null) fetch.getRight().put(targetVertex, edge);
                fetch = map.get(targetVertex);
                if (fetch != null) fetch.getLeft().put(sourceVertex, edge);
                return true;
            }

            @Override
            public boolean addEdgeToTouchingVerticesIfAbsent(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                             GraphEdge edge) {
                var fetch = map.get(sourceVertex);
                if (fetch != null && fetch.getRight().putIfAbsent(targetVertex, edge) == null) {
                    fetch = map.get(targetVertex);
                    if (fetch != null) fetch.getLeft().put(sourceVertex, edge);
                    return true;
                }
                return false;
            }

            @Override
            public GraphEdge createEdgeToTouchingVerticesIfAbsent(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                                  Supplier<GraphEdge> edgeSupplier) {
                var fetch = map.get(sourceVertex);
                if (fetch == null || fetch.getRight().containsKey(targetVertex)) return null;
                GraphEdge edge = edgeSupplier.get();
                fetch.getRight().put(targetVertex, edge);
                fetch = map.get(targetVertex);
                if (fetch != null) fetch.getLeft().put(sourceVertex, edge);
                return edge;
            }

            @Override
            public int degreeOf(GraphVertex vertex) {
                var fetch = map.get(vertex);
                return fetch == null ? 0 : fetch.getLeft().size() + fetch.getRight().size();
            }

            @Override
            public Set<GraphEdge> edgesOf(GraphVertex vertex) {
                var fetch = map.get(vertex);
                if (fetch == null) return Collections.emptySet();
                Set<GraphEdge> set = new ArrayUnenforcedSet<>(fetch.getLeft().size() + fetch.getRight().size());
                set.addAll(fetch.getLeft().values());
                set.addAll(fetch.getRight().values());
                return set;
            }

            @Override
            public int inDegreeOf(GraphVertex vertex) {
                var fetch = map.get(vertex);
                return fetch == null ? 0 : fetch.getLeft().size();
            }

            @Override
            public Set<GraphEdge> incomingEdgesOf(GraphVertex vertex) {
                var fetch = map.get(vertex);
                return fetch == null ? Collections.emptySet() : fetch.getLeft().values();
            }

            @Override
            public int outDegreeOf(GraphVertex vertex) {
                var fetch = map.get(vertex);
                return fetch == null ? 0 : fetch.getRight().size();
            }

            @Override
            public Set<GraphEdge> outgoingEdgesOf(GraphVertex vertex) {
                var fetch = map.get(vertex);
                return fetch == null ? Collections.emptySet() : fetch.getRight().values();
            }

            @Override
            public void removeEdgeFromTouchingVertices(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                       GraphEdge edge) {
                var fetch = map.get(sourceVertex);
                if (fetch != null && fetch.getRight().remove(targetVertex) != null) {
                    fetch = map.get(targetVertex);
                    if (fetch != null) fetch.getLeft().remove(sourceVertex);
                }
            }
        }

        private static final class UndirectedSpecifics implements Specifics<GraphVertex, GraphEdge> {

            // vertex -> vertex -> edge
            Map<GraphVertex, BiMap<GraphVertex, GraphEdge>> map = new Reference2ReferenceOpenHashMap<>();

            @Override
            public boolean addVertex(GraphVertex vertex) {
                return map.putIfAbsent(vertex, standardBiMap()) == null;
            }

            @Override
            public Set<GraphVertex> getVertexSet() {
                return map.keySet();
            }

            @Override
            public Set<GraphEdge> getAllEdges(GraphVertex sourceVertex, GraphVertex targetVertex) {
                GraphEdge e = getEdge(sourceVertex, targetVertex);
                if (e == null) return Collections.emptySet();
                return Collections.singleton(e);
            }

            @Override
            public GraphEdge getEdge(GraphVertex sourceVertex, GraphVertex targetVertex) {
                var fetch = map.get(sourceVertex);
                return fetch == null ? null : fetch.get(targetVertex);
            }

            @Override
            public boolean addEdgeToTouchingVertices(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                     GraphEdge edge) {
                var fetch = map.get(sourceVertex);
                if (fetch != null) fetch.put(targetVertex, edge);
                fetch = map.get(targetVertex);
                if (fetch != null) fetch.put(sourceVertex, edge);
                return true;
            }

            @Override
            public boolean addEdgeToTouchingVerticesIfAbsent(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                             GraphEdge edge) {
                var fetch = map.get(sourceVertex);
                if (fetch != null && fetch.putIfAbsent(targetVertex, edge) == null) {
                    fetch = map.get(targetVertex);
                    if (fetch != null) fetch.put(sourceVertex, edge);
                    return true;
                }
                return false;
            }

            @Override
            public GraphEdge createEdgeToTouchingVerticesIfAbsent(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                                  Supplier<GraphEdge> edgeSupplier) {
                var fetch = map.get(sourceVertex);
                if (fetch == null || fetch.containsKey(targetVertex)) return null;
                GraphEdge edge = edgeSupplier.get();
                fetch.put(targetVertex, edge);
                fetch = map.get(targetVertex);
                if (fetch != null) fetch.put(sourceVertex, edge);
                return edge;
            }

            @Override
            public int degreeOf(GraphVertex vertex) {
                var fetch = map.get(vertex);
                return fetch == null ? 0 : fetch.size();
            }

            @Override
            public Set<GraphEdge> edgesOf(GraphVertex vertex) {
                var fetch = map.get(vertex);
                if (fetch == null) return Collections.emptySet();
                return fetch.values();
            }

            @Override
            public int inDegreeOf(GraphVertex vertex) {
                return degreeOf(vertex);
            }

            @Override
            public Set<GraphEdge> incomingEdgesOf(GraphVertex vertex) {
                return edgesOf(vertex);
            }

            @Override
            public int outDegreeOf(GraphVertex vertex) {
                return degreeOf(vertex);
            }

            @Override
            public Set<GraphEdge> outgoingEdgesOf(GraphVertex vertex) {
                return edgesOf(vertex);
            }

            @Override
            public void removeEdgeFromTouchingVertices(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                       GraphEdge edge) {
                var fetch = map.get(sourceVertex);
                if (fetch != null && fetch.remove(targetVertex) != null) {
                    fetch = map.get(targetVertex);
                    if (fetch != null) fetch.remove(sourceVertex);
                }
            }
        }
    }
}
