package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.jgrapht.traverse.CrossComponentIterator;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;

import java.util.Iterator;
import java.util.List;
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

    /**
     * Marks an edge as no longer valid for traversal. When applied to an edge with children, the entire "branch" of the
     * seen "tree" will be cut off, and previous inferior connections to the branch will be considered again, allowing
     * further iteration to potentially revisit nodes within the removed "branch".
     *
     * @param edge the edge to mark invalid
     * @return this object, for convenience.
     */
    @Contract("_->this")
    public ResilientNetClosestIterator markInvalid(@NotNull NetEdge edge) {
        if (edge.wrapper != null) internal.invalidate(edge.wrapper);
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

        public final Set<GraphVertex> invalidatedNodes = new ReferenceOpenHashSet<>();
        public final Set<GraphEdge> invalidatedEdges = new ReferenceOpenHashSet<>();

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
            this.seen = new Reference2ReferenceOpenHashMap<>(expectedSize) {

                // prevent rehashing to shrink the seen map, we are a shortlived object, this is expensive,
                // and it grows and shrinks rapidly.
                @Override
                protected void rehash(int newN) {
                    if (newN > n) super.rehash(newN);
                }
            };
            encounterVertexFirst(null, startVertex, null);
        }

        @Override
        public boolean hasNext() {
            if (heap.isEmpty()) {
                return false;
            } else if (heap.findMin().getKey() > radius) {
                heap.clear();
                return false;
            } else {
                while (!heap.isEmpty() && heap.findMin().getValue().outdated) {
                    heap.deleteMin();
                }
                return !heap.isEmpty();
            }
        }

        @Override
        public GraphVertex next() {
            if (hasNext()) {
                // hasNext() prevents the bottom-most entry on the heap from being outdated
                AddressableHeap.Handle<Double, SeenData> node = heap.deleteMin();
                node.getValue().foundMinimum = true;
                addUnseenChildrenOf(node.getValue().vertex);
                return node.getValue().vertex;
            } else {
                throw new NoSuchElementException();
            }
        }

        private void addUnseenChildrenOf(GraphVertex vertex) {
            for (GraphEdge edge : selector.selectEdges(graph, vertex)) {
                if (invalidatedEdges.contains(edge)) continue;
                GraphVertex oppositeV = edge.getOppositeVertex(vertex);
                encounterVertex(vertex, oppositeV, edge);
            }
        }

        public void invalidate(GraphVertex vertex) {
            if (!invalidatedNodes.add(vertex)) return;
            AddressableHeap.Handle<Double, SeenData> handle = seen.get(vertex);
            if (handle != null) {
                invalidate(handle.getValue());
            }
        }

        public void invalidate(GraphEdge edge) {
            if (!invalidatedEdges.add(edge)) return;
            AddressableHeap.Handle<Double, SeenData> handle = seen.get(edge.getSource());
            if (handle != null && handle.getValue().spanningTreeEdge == edge) {
                invalidate(handle.getValue());
                return;
            }
            handle = seen.get(edge.getTarget());
            if (handle != null && handle.getValue().spanningTreeEdge == edge) {
                invalidate(handle.getValue());
            }
        }

        private void invalidate(SeenData seen) {
            seen.applySelfAndChildren(c -> {
                this.seen.remove(c.vertex);
                c.outdated = true;
            });
            seen.applySelfAndChildren(c -> {
                for (int i = 0; i < c.weakParents.size(); i++) {
                    GraphVertex v = c.weakParents.get(i).vertex;
                    if (this.seen.containsKey(v)) {
                        // one of the weak parents is still a seen node, regenerate
                        GraphEdge e = graph.getEdge(v, c.vertex);
                        if (e != null) encounterVertex(v, c.vertex, e);
                    }
                }
            });
        }

        public GraphEdge getSpanningTreeEdge(GraphVertex vertex) {
            AddressableHeap.Handle<Double, SeenData> node = seen.get(vertex);
            return node == null ? null : node.getValue().spanningTreeEdge;
        }

        private void encounterVertex(GraphVertex source, GraphVertex dest, GraphEdge edge) {
            if (invalidatedNodes.contains(dest)) return;
            if (seen.containsKey(dest)) {
                encounterVertexAgain(source, dest, edge);
            } else {
                encounterVertexFirst(source, dest, edge);
            }
        }

        private void encounterVertexFirst(GraphVertex source, GraphVertex dest, GraphEdge edge) {
            double shortestPathLength;
            SeenData data;
            if (edge == null) {
                shortestPathLength = 0;
                data = new SeenData(dest, null, null);
            } else {
                AddressableHeap.Handle<Double, SeenData> otherEntry = seen.get(source);
                if (otherEntry == null) return;
                shortestPathLength = otherEntry.getKey() + edge.getWeight();

                data = new SeenData(dest, edge, otherEntry.getValue());
                otherEntry.getValue().spanningChildren.add(data);
            }
            AddressableHeap.Handle<Double, SeenData> handle = heap.insert(shortestPathLength, data);
            seen.put(dest, handle);
        }

        private void encounterVertexAgain(GraphVertex source, GraphVertex dest, GraphEdge edge) {
            AddressableHeap.Handle<Double, SeenData> node = seen.get(dest);
            AddressableHeap.Handle<Double, SeenData> otherEntry = seen.get(source);

            SeenData data = node.getValue();

            if (data.foundMinimum) {
                // no improvement for this vertex possible
                data.weakParents.add(otherEntry.getValue());
                return;
            }

            double candidatePathLength = otherEntry.getKey() + graph.getEdgeWeight(edge);
            if (candidatePathLength < node.getKey()) {
                data.weakParents.add(data.parent);

                data.parent = otherEntry.getValue();
                data.spanningTreeEdge = edge;

                node.decreaseKey(candidatePathLength);
            } else {
                data.weakParents.add(otherEntry.getValue());
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
        public final List<SeenData> spanningChildren = new ObjectArrayList<>(6);

        // weak parents are regeneration candidates
        public final List<SeenData> weakParents = new ObjectArrayList<>(6);

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
