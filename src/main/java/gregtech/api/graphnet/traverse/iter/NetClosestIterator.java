package gregtech.api.graphnet.traverse.iter;

import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.traverse.ClosestFirstIterator;

import java.util.Set;

public class NetClosestIterator implements NetIterator {

    protected final ClosestFirstIterator<GraphVertex, GraphEdge> backer;

    protected final EdgeSelector selector;

    /**
     * Creates a closest-first iterator that traverses a connected component, starting at the given node.
     * 
     * @param origin the node to start at
     */
    public NetClosestIterator(@NotNull NetNode origin, EdgeSelector selector) {
        this.backer = new ClosestFirstIterator<>(origin.getNet().getGraph(), origin.wrapper) {

            @Override
            protected Set<GraphEdge> selectOutgoingEdges(GraphVertex vertex) {
                return selectEdges(graph, vertex);
            }
        };
        this.selector = selector;
    }

    public ClosestFirstIterator<GraphVertex, GraphEdge> getBacker() {
        return backer;
    }

    protected Set<GraphEdge> selectEdges(Graph<GraphVertex, GraphEdge> graph, GraphVertex vertex) {
        return selector.selectEdges(graph, vertex);
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

    public @Nullable NetEdge getSpanningTreeEdge(@NotNull NetNode node) {
        return backer.getSpanningTreeEdge(node.wrapper).getWrapped();
    }
}
