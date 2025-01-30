package gregtech.api.graphnet.traverse;

import org.jgrapht.Graph;
import org.jgrapht.util.ArrayUnenforcedSet;

import java.util.Set;
import java.util.function.Predicate;

public interface EdgeSelector {

    <V, E> Set<E> selectEdges(Graph<V, E> graph, V vertex);

    <V, E> Set<E> selectReversedEdges(Graph<V, E> graph, V vertex);

    static EdgeSelector filtered(EdgeSelector basis, Predicate<Object> blacklist) {
        return new EdgeSelector() {

            @Override
            public <V, E> Set<E> selectEdges(Graph<V, E> graph, V vertex) {
                Set<E> select = basis.selectEdges(graph, vertex);
                Set<E> set = new ArrayUnenforcedSet<>(select.size());
                for (E e : select) {
                    if (!blacklist.test(e)) {
                        set.add(e);
                    }
                }
                return set;
            }

            @Override
            public <V, E> Set<E> selectReversedEdges(Graph<V, E> graph, V vertex) {
                Set<E> select = basis.selectReversedEdges(graph, vertex);
                Set<E> set = new ArrayUnenforcedSet<>(select.size());
                for (E e : select) {
                    if (!blacklist.test(e)) {
                        set.add(e);
                    }
                }
                return set;
            }
        };
    }
}
