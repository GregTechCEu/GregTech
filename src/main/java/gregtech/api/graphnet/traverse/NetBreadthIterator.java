package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.Set;

public class NetBreadthIterator implements NetIterator {

    protected Iter backer;

    /**
     * Creates a breadth-first iterator that traverses a connected component, starting at the given node.
     * 
     * @param origin the node to start at
     */
    public NetBreadthIterator(@NotNull NetNode origin, @NotNull EdgeSelector selector) {
        this.backer = new Iter(origin.getNet().getGraph(), origin.wrapper, selector);
    }

    /**
     * Creates a breadth-first iterator that traverses the entire graph, starting at an arbitrary point.
     * 
     * @param graphNet the graph to traverse.
     */
    public NetBreadthIterator(@NotNull IGraphNet graphNet) {
        this.backer = new Iter(graphNet.getGraph(), (GraphVertex) null, EdgeDirection.ALL);
    }

    public BreadthFirstIterator<GraphVertex, GraphEdge> getBacker() {
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

    public @Nullable NetNode getParent(@NotNull NetNode node) {
        return backer.getParent(node.wrapper).getWrapped();
    }

    public boolean hasSeen(@NotNull NetNode node) {
        return backer.hasSeen(node.wrapper);
    }

    public @Nullable NetEdge getSpanningTreeEdge(@NotNull NetNode node) {
        if (!backer.hasSeen(node.wrapper)) return null;
        return NetEdge.unwrap(backer.getSpanningTreeEdge(node.wrapper));
    }

    public int getDepth(@NotNull NetNode node) {
        return backer.getDepth(node.wrapper);
    }

    protected static final class Iter extends BreadthFirstIterator<GraphVertex, GraphEdge> {

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
