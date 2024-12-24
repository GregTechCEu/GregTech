package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.traverse.ClosestFirstIterator;

import java.util.Set;

public class NetClosestIterator implements NetIterator {

    protected final Iter backer;

    /**
     * Creates a closest-first iterator that traverses a connected component, starting at the given node.
     * 
     * @param origin the node to start at
     */
    public NetClosestIterator(@NotNull NetNode origin, EdgeSelector selector) {
        this.backer = new Iter(origin.getNet().getGraph(), origin.wrapper, selector);
    }

    public ClosestFirstIterator<GraphVertex, GraphEdge> getBacker() {
        return backer;
    }

    @Override
    public boolean hasNext() {
        return backer.hasNext();
    }

    @Override
    public NetNode next() {
        return backer.next().getWrapped();
    }

    public double getShortestPathLength(@NotNull NetNode node) {
        return backer.getShortestPathLength(node.wrapper);
    }

    public boolean hasSeen(@NotNull NetNode node) {
        return backer.hasSeen(node.wrapper);
    }

    public @Nullable NetEdge getSpanningTreeEdge(@NotNull NetNode node) {
        if (!backer.hasSeen(node.wrapper)) return null;
        return NetEdge.unwrap(backer.getSpanningTreeEdge(node.wrapper));
    }

    protected static final class Iter extends ClosestFirstIterator<GraphVertex, GraphEdge> {

        private final EdgeSelector selector;

        public Iter(Graph<GraphVertex, GraphEdge> g, GraphVertex startVertex, EdgeSelector selector) {
            super(g, startVertex);
            this.selector = selector;
        }

        @Override
        protected Set<GraphEdge> selectOutgoingEdges(GraphVertex vertex) {
            return selector.selectEdges(graph, vertex);
        }

        public boolean hasSeen(GraphVertex vertex) {
            return getSeenData(vertex) != null;
        }
    }
}
