package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.iter.IteratorFactory;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.path.INetPath;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.shortestpath.DefaultManyToManyShortestPaths;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DynamicWeightsShortestPathsAlgorithm extends DefaultManyToManyShortestPaths<GraphVertex, GraphEdge>
                                                  implements INetAlgorithm {

    private final boolean recomputeEveryCall;

    public DynamicWeightsShortestPathsAlgorithm(IGraphNet net, boolean recomputeEveryCall) {
        super(net.getGraph());
        this.recomputeEveryCall = recomputeEveryCall;
    }

    @Override
    public <Path extends INetPath<?, ?>> IteratorFactory<Path> getPathsIteratorFactory(GraphVertex source,
                                                                                       NetPathMapper<Path> remapper) {
        Set<GraphVertex> searchSpace = source.wrapped.getGroupSafe().getNodes().stream().filter(NetNode::isActive)
                .map(n -> n.wrapper).filter(node -> !source.equals(node)).collect(Collectors.toSet());
        return (graph, testObject, simulator, queryTick) -> {
            if (recomputeEveryCall) graph.prepareForAlgorithmRun(testObject, simulator, queryTick);
            return new LimitedIterator<>(source, searchSpace, remapper);
        };
    }

    protected class LimitedIterator<Path extends INetPath<?, ?>> implements Iterator<Path> {

        private static final int MAX_ITERATIONS = 100;

        private final GraphVertex source;
        private final Set<GraphVertex> searchSpace;
        private final NetPathMapper<Path> remapper;

        private int iterationCount = 0;
        private final ObjectArrayList<Path> visited = new ObjectArrayList<>();
        private @Nullable Path next;

        public LimitedIterator(GraphVertex source, Set<GraphVertex> searchSpace, NetPathMapper<Path> remapper) {
            this.source = source;
            this.searchSpace = searchSpace;
            this.remapper = remapper;
        }

        @Override
        public boolean hasNext() {
            if (next == null && iterationCount < MAX_ITERATIONS) calculateNext();
            return next != null;
        }

        @Override
        public Path next() {
            if (!hasNext()) throw new NoSuchElementException();
            Path temp = next;
            next = null;
            return temp;
        }

        private void calculateNext() {
            iterationCount++;
            if (iterationCount == 1) {
                next = remapper.map(source);
                return;
            }
            ManyToManyShortestPaths<GraphVertex, GraphEdge> paths = getManyToManyPaths(Collections.singleton(source),
                    searchSpace);
            Optional<Path> next = searchSpace.stream().map(node -> paths.getPath(source, node)).filter(Objects::nonNull)
                    .map(remapper::map).filter(this::isUnique).min(Comparator.comparingDouble(INetPath::getWeight));
            this.next = next.orElse(null);
            next.ifPresent(this.visited::add);
        }

        private boolean isUnique(Path path) {
            for (Path other : visited) {
                if (path.matches(other)) return false;
            }
            return true;
        }
    }
}
