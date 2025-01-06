package gregtech.api.graphnet.traverse;

import org.jgrapht.Graph;

import java.util.Set;

public enum EdgeDirection implements EdgeSelector {

    OUTGOING,
    INCOMING,
    ALL;

    @Override
    public <V, E> Set<E> selectEdges(Graph<V, E> graph, V vertex) {
        return switch (this) {
            case ALL -> graph.edgesOf(vertex);
            case INCOMING -> graph.incomingEdgesOf(vertex);
            case OUTGOING -> graph.outgoingEdgesOf(vertex);
        };
    }

    @Override
    public <V, E> Set<E> selectReversedEdges(Graph<V, E> graph, V vertex) {
        return switch (this) {
            case ALL -> graph.edgesOf(vertex);
            case OUTGOING -> graph.incomingEdgesOf(vertex);
            case INCOMING -> graph.outgoingEdgesOf(vertex);
        };
    }
}
