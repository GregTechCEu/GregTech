package gregtech.api.graphnet.graph;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.UnorderedPair;
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

        private static final class DirectedSpecifics implements Specifics<GraphVertex, GraphEdge> {

            // vertex -> incoming edges, outgoing edges
            Map<GraphVertex, Pair<Set<GraphEdge>, Set<GraphEdge>>> map = new Reference2ReferenceOpenHashMap<>();

            // source, target -> edge
            Map<Pair<GraphVertex, GraphVertex>, GraphEdge> edges = new Object2ReferenceOpenHashMap<>();

            @Override
            public boolean addVertex(GraphVertex vertex) {
                return map.putIfAbsent(vertex, new Pair<>(new ArrayUnenforcedSet<>(1), new ArrayUnenforcedSet<>(1))) ==
                        null;
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
                return edges.get(new Pair<>(sourceVertex, targetVertex));
            }

            @Override
            public boolean addEdgeToTouchingVertices(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                     GraphEdge edge) {
                edges.put(new Pair<>(sourceVertex, targetVertex), edge);
                map.get(sourceVertex).getSecond().add(edge);
                map.get(targetVertex).getFirst().add(edge);
                return true;
            }

            @Override
            public boolean addEdgeToTouchingVerticesIfAbsent(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                             GraphEdge edge) {
                if (edges.putIfAbsent(new Pair<>(sourceVertex, targetVertex), edge) == null) {
                    map.get(sourceVertex).getSecond().add(edge);
                    map.get(targetVertex).getFirst().add(edge);
                    return true;
                }
                return false;
            }

            @Override
            public GraphEdge createEdgeToTouchingVerticesIfAbsent(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                                  Supplier<GraphEdge> edgeSupplier) {
                Pair<GraphVertex, GraphVertex> pair = new Pair<>(sourceVertex, targetVertex);
                if (edges.containsKey(pair)) return null;
                GraphEdge edge = edgeSupplier.get();
                edges.put(pair, edge);
                map.get(sourceVertex).getSecond().add(edge);
                map.get(targetVertex).getFirst().add(edge);
                return edge;
            }

            @Override
            public int degreeOf(GraphVertex vertex) {
                Pair<Set<GraphEdge>, Set<GraphEdge>> pair = map.get(vertex);
                return pair == null ? 0 : pair.getFirst().size() + pair.getSecond().size();
            }

            @Override
            public Set<GraphEdge> edgesOf(GraphVertex vertex) {
                Pair<Set<GraphEdge>, Set<GraphEdge>> get = map.get(vertex);
                if (get == null) return Collections.emptySet();
                Set<GraphEdge> set = new ArrayUnenforcedSet<>(get.getFirst().size() + get.getSecond().size());
                set.addAll(get.getFirst());
                set.addAll(get.getSecond());
                return set;
            }

            @Override
            public int inDegreeOf(GraphVertex vertex) {
                Pair<Set<GraphEdge>, Set<GraphEdge>> pair = map.get(vertex);
                return pair == null ? 0 : pair.getFirst().size();
            }

            @Override
            public Set<GraphEdge> incomingEdgesOf(GraphVertex vertex) {
                Pair<Set<GraphEdge>, Set<GraphEdge>> get = map.get(vertex);
                return get == null ? Collections.emptySet() : get.getFirst();
            }

            @Override
            public int outDegreeOf(GraphVertex vertex) {
                Pair<Set<GraphEdge>, Set<GraphEdge>> pair = map.get(vertex);
                return pair == null ? 0 : pair.getSecond().size();
            }

            @Override
            public Set<GraphEdge> outgoingEdgesOf(GraphVertex vertex) {
                Pair<Set<GraphEdge>, Set<GraphEdge>> get = map.get(vertex);
                return get == null ? Collections.emptySet() : get.getSecond();
            }

            @Override
            public void removeEdgeFromTouchingVertices(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                       GraphEdge edge) {
                if (edges.remove(new Pair<>(sourceVertex, targetVertex)) != null) {
                    map.get(sourceVertex).getSecond().remove(edge);
                    map.get(targetVertex).getFirst().remove(edge);
                }
            }
        }

        private static final class UndirectedSpecifics implements Specifics<GraphVertex, GraphEdge> {

            // vertex -> edges
            Map<GraphVertex, Set<GraphEdge>> map = new Reference2ReferenceOpenHashMap<>();

            // vertices -> edge
            Map<UnorderedPair<GraphVertex, GraphVertex>, GraphEdge> edges = new Object2ReferenceOpenHashMap<>();

            @Override
            public boolean addVertex(GraphVertex vertex) {
                return map.putIfAbsent(vertex, new ArrayUnenforcedSet<>(1)) == null;
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
                return edges.get(new UnorderedPair<>(sourceVertex, targetVertex));
            }

            @Override
            public boolean addEdgeToTouchingVertices(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                     GraphEdge edge) {
                edges.put(new UnorderedPair<>(sourceVertex, targetVertex), edge);
                map.get(sourceVertex).add(edge);
                map.get(targetVertex).add(edge);
                return true;
            }

            @Override
            public boolean addEdgeToTouchingVerticesIfAbsent(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                             GraphEdge edge) {
                if (edges.putIfAbsent(new UnorderedPair<>(sourceVertex, targetVertex), edge) == null) {
                    map.get(sourceVertex).add(edge);
                    map.get(targetVertex).add(edge);
                    return true;
                }
                return false;
            }

            @Override
            public GraphEdge createEdgeToTouchingVerticesIfAbsent(GraphVertex sourceVertex, GraphVertex targetVertex,
                                                                  Supplier<GraphEdge> edgeSupplier) {
                UnorderedPair<GraphVertex, GraphVertex> pair = new UnorderedPair<>(sourceVertex, targetVertex);
                if (edges.containsKey(pair)) return null;
                GraphEdge edge = edgeSupplier.get();
                edges.put(pair, edge);
                map.get(sourceVertex).add(edge);
                map.get(targetVertex).add(edge);
                return edge;
            }

            @Override
            public int degreeOf(GraphVertex vertex) {
                Set<GraphEdge> set = map.get(vertex);
                return set == null ? 0 : set.size();
            }

            @Override
            public Set<GraphEdge> edgesOf(GraphVertex vertex) {
                Set<GraphEdge> get = map.get(vertex);
                return get == null ? Collections.emptySet() : get;
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
                if (edges.remove(new UnorderedPair<>(sourceVertex, targetVertex)) != null) {
                    map.get(sourceVertex).remove(edge);
                    map.get(targetVertex).remove(edge);
                }
            }
        }
    }
}
