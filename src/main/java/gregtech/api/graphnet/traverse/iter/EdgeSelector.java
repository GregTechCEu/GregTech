package gregtech.api.graphnet.traverse.iter;

import org.jgrapht.Graph;

import java.util.Set;

public interface EdgeSelector {

    <V, E> Set<E> selectEdges(Graph<V, E> graph, V vertex);
}
