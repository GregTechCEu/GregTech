package gregtech.api.graphnet.traverse.iter;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.Set;

public class NetBreadthIterator implements NetIterator {

    protected BreadthFirstIterator<GraphVertex, GraphEdge> backer;

    /**
     * Creates a breadth-first iterator that traverses a connected component, starting at the given node.
     * 
     * @param origin the node to start at
     */
    public NetBreadthIterator(@NotNull NetNode origin, @NotNull EdgeDirection direction) {
        this.backer = new BreadthFirstIterator<>(origin.getNet().getGraph(), origin.wrapper) {

            @Override
            protected Set<GraphEdge> selectOutgoingEdges(GraphVertex vertex) {
                return direction.selectEdges(graph, vertex);
            }
        };
    }

    /**
     * Creates a breadth-first iterator that traverses the entire graph, starting at an arbitrary point.
     * 
     * @param graphNet the graph to traverse.
     */
    public NetBreadthIterator(@NotNull IGraphNet graphNet) {
        this.backer = new BreadthFirstIterator<>(graphNet.getGraph(), (GraphVertex) null) {

            @Override
            protected Set<GraphEdge> selectOutgoingEdges(GraphVertex vertex) {
                return graph.edgesOf(vertex);
            }
        };
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

    public @Nullable NetEdge getSpanningTreeEdge(@NotNull NetNode node) {
        return backer.getSpanningTreeEdge(node.wrapper).getWrapped();
    }

    public int getDepth(@NotNull NetNode node) {
        return backer.getDepth(node.wrapper);
    }
}
