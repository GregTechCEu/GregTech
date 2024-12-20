package gregtech.api.graphnet.traverse.iter;

import org.jgrapht.Graph;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface EdgeSelector {

    <V, E> Set<E> selectEdges(Graph<V, E> graph, V vertex);

    static EdgeSelector filtered(EdgeSelector prototype, Predicate<Object> edgeFilter) {
        // can't create via lambda due to generics
        return new EdgeSelector() {

            @Override
            public <V, E> Set<E> selectEdges(Graph<V, E> graph, V vertex) {
                return prototype.selectEdges(graph, vertex).stream().filter(edgeFilter).collect(Collectors.toSet());
            }
        };
    }
}
