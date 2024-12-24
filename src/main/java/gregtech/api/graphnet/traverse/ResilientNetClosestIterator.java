package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.jgrapht.traverse.CrossComponentIterator;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A specialized net closest iterator that allows nodes to be marked invalid for iteration <b>retroactively</b>,
 * without requiring a new iterator to be declared, leading to its resilience.
 */
public class ResilientNetClosestIterator implements NetIterator {

    protected final @NotNull InternalIterator internal;

    public ResilientNetClosestIterator(@NotNull NetNode origin, @NotNull EdgeSelector selector) {
        internal = new InternalIterator(origin.getNet().getGraph(), origin.wrapper, selector,
                origin.getGroupSafe().getNodes().size());
    }

    public ResilientNetClosestIterator(@NotNull NetNode origin, @NotNull EdgeSelector selector, double radius) {
        internal = new InternalIterator(origin.getNet().getGraph(), origin.wrapper, selector,
                origin.getGroupSafe().getNodes().size(), radius);
    }

    /**
     * Marks a node as no longer valid for traversal. When applied to a node with children, the entire "branch" of the
     * seen "tree" will be cut off, and previous inferior connections to the branch will be considered again, allowing
     * further iteration to potentially revisit nodes within the removed "branch".
     * 
     * @param node the node to mark invalid
     * @return this object, for convenience.
     */
    @Contract("_->this")
    public ResilientNetClosestIterator markInvalid(@NotNull NetNode node) {
        if (node.wrapper != null) internal.invalidate(node.wrapper);
        return this;
    }

    @Override
    public boolean hasSeen(@NotNull NetNode node) {
        return internal.seen.containsKey(node.wrapper);
    }

    @Override
    public @Nullable NetEdge getSpanningTreeEdge(@NotNull NetNode node) {
        return NetEdge.unwrap(internal.getSpanningTreeEdge(node.wrapper));
    }

    @Override
    public boolean hasNext() {
        return internal.hasNext();
    }

    @Override
    public NetNode next() {
        return internal.next().getWrapped();
    }

    /**
     * See {@link ClosestFirstIterator} and {@link CrossComponentIterator}
     */
    protected static final class InternalIterator implements Iterator<GraphVertex> {

        public final Graph<GraphVertex, GraphEdge> graph;

        public final EdgeSelector selector;

        public final AddressableHeap<Double, SeenData> heap;

        public final Map<GraphVertex, AddressableHeap.Handle<Double, SeenData>> seen;

        public final double radius;

        public final Set<GraphVertex> invalidated = new ObjectOpenHashSet<>();

        public InternalIterator(Graph<GraphVertex, GraphEdge> graph, GraphVertex startVertex, EdgeSelector selector,
                                int expectedSize) {
            this(graph, startVertex, selector, expectedSize, Double.POSITIVE_INFINITY);
        }

        public InternalIterator(Graph<GraphVertex, GraphEdge> graph, GraphVertex startVertex, EdgeSelector selector,
                                int expectedSize, double radius) {
            this.graph = graph;
            this.selector = selector;
            this.radius = radius;
            this.heap = new PairingHeap<>();
            this.seen = new Object2ObjectOpenHashMap<>(expectedSize);
            encounterVertexFirst(startVertex, null);
        }

        @Override
        public boolean hasNext() {
            return !isConnectedComponentExhausted();
        }

        @Override
        public GraphVertex next() {
            if (hasNext()) {
                AddressableHeap.Handle<Double, SeenData> node;
                do {
                    node = heap.deleteMin();
                    node.getValue().foundMinimum = true;
                } while (node.getValue().outdated && hasNext());

                addUnseenChildrenOf(node.getValue().vertex);
                return node.getValue().vertex;
            } else {
                throw new NoSuchElementException();
            }
        }

        private void addUnseenChildrenOf(GraphVertex vertex) {
            for (GraphEdge edge : selector.selectEdges(graph, vertex)) {

                GraphVertex oppositeV = edge.getOppositeVertex(vertex);
                encounterVertex(oppositeV, edge);
            }
        }

        public void invalidate(GraphVertex vertex) {
            if (!invalidated.add(vertex)) return;
            AddressableHeap.Handle<Double, SeenData> handle = seen.get(vertex);
            if (handle != null) {
                Set<GraphEdge> regenerationCandidates = new ObjectOpenHashSet<>();
                handle.getValue().applySelfAndChildren(c -> {
                    seen.remove(c.vertex);
                    c.outdated = true;
                    regenerationCandidates.addAll(selector.selectReversedEdges(graph, c.vertex));
                });
                for (GraphEdge candidate : regenerationCandidates) {
                    if (seen.containsKey(candidate.getSource())) {
                        encounterVertex(candidate.getTarget(), candidate);
                    } else if (seen.containsKey(candidate.getTarget())) {
                        encounterVertex(candidate.getSource(), candidate);
                    }
                }
            }
        }

        public GraphEdge getSpanningTreeEdge(GraphVertex vertex) {
            AddressableHeap.Handle<Double, SeenData> node = seen.get(vertex);
            return node == null ? null : node.getValue().spanningTreeEdge;
        }

        private boolean isConnectedComponentExhausted() {
            if (heap.size() == 0) {
                return true;
            } else {
                if (heap.findMin().getKey() > radius) {
                    heap.clear();
                    return true;
                } else {
                    return false;
                }
            }
        }

        private void encounterVertex(GraphVertex vertex, GraphEdge edge) {
            if (invalidated.contains(vertex)) return;
            if (seen.containsKey(vertex)) {
                encounterVertexAgain(vertex, edge);
            } else {
                encounterVertexFirst(vertex, edge);
            }
        }

        private void encounterVertexFirst(GraphVertex vertex, GraphEdge edge) {
            double shortestPathLength;
            SeenData data;
            if (edge == null) {
                shortestPathLength = 0;
                data = new SeenData(vertex, null, null);
            } else {
                GraphVertex otherVertex = edge.getOppositeVertex(vertex);
                AddressableHeap.Handle<Double, SeenData> otherEntry = seen.get(otherVertex);
                if (otherEntry == null) return;
                shortestPathLength = otherEntry.getKey() + graph.getEdgeWeight(edge);

                data = new SeenData(vertex, edge, otherEntry.getValue());
                otherEntry.getValue().spanningChildren.add(data);
            }
            AddressableHeap.Handle<Double, SeenData> handle = heap.insert(shortestPathLength, data);
            seen.put(vertex, handle);
        }

        private void encounterVertexAgain(GraphVertex vertex, GraphEdge edge) {
            AddressableHeap.Handle<Double, SeenData> node = seen.get(vertex);

            if (node.getValue().foundMinimum) {
                // no improvement for this vertex possible
                return;
            }
            GraphVertex otherVertex = edge.getOppositeVertex(vertex);
            AddressableHeap.Handle<Double, SeenData> otherEntry = seen.get(otherVertex);

            double candidatePathLength = otherEntry.getKey() + graph.getEdgeWeight(edge);
            if (candidatePathLength < node.getKey()) {
                node.getValue().parent = otherEntry.getValue();
                node.getValue().spanningTreeEdge = edge;
                node.decreaseKey(candidatePathLength);
            }
        }
    }

    protected static final class SeenData {

        public final GraphVertex vertex;

        // the next edge along the lowest weight path to the origin node
        public GraphEdge spanningTreeEdge;

        public SeenData parent;

        public boolean foundMinimum = false;

        public boolean outdated = false;

        // all nodes whose spanning tree edges point at this node
        public final Set<SeenData> spanningChildren = new ObjectOpenHashSet<>(6);

        public SeenData(GraphVertex vertex, GraphEdge spanningTreeEdge, SeenData parent) {
            this.vertex = vertex;
            this.spanningTreeEdge = spanningTreeEdge;
            this.parent = parent;
        }

        public void applySelfAndChildren(Consumer<SeenData> c) {
            c.accept(this);
            for (SeenData data : spanningChildren) {
                data.applySelfAndChildren(c);
            }
        }
    }
}
