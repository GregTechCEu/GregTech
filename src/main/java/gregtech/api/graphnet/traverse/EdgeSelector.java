package gregtech.api.graphnet.traverse;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jgrapht.Graph;

import java.util.Set;
import java.util.function.Predicate;

public interface EdgeSelector {

    <V, E> Set<E> selectEdges(Graph<V, E> graph, V vertex);

    <V, E> Set<E> selectReversedEdges(Graph<V, E> graph, V vertex);

    static EdgeSelector filtered(EdgeSelector basis, Predicate<Object> blacklist) {
        return new EdgeSelector() {

            @Override
            public <V, E> Set<E> selectEdges(Graph<V, E> graph, V vertex) {
                Set<E> set = new ObjectOpenHashSet<>(basis.selectEdges(graph, vertex));
                set.removeIf(blacklist);
                return set;
            }

            @Override
            public <V, E> Set<E> selectReversedEdges(Graph<V, E> graph, V vertex) {
                Set<E> set = new ObjectOpenHashSet<>(basis.selectReversedEdges(graph, vertex));
                set.removeIf(blacklist);
                return set;
            }
        };
    }
}
