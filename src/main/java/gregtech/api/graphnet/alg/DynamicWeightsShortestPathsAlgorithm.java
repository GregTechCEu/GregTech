package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.iter.ICacheableIterator;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.path.INetPath;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.alg.shortestpath.DefaultManyToManyShortestPaths;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DynamicWeightsShortestPathsAlgorithm extends DefaultManyToManyShortestPaths<GraphVertex, GraphEdge>
                                                  implements INetAlgorithm {

    public DynamicWeightsShortestPathsAlgorithm(IGraphNet net) {
        super(net.getGraph());
    }

    @Override
    public <Path extends INetPath<?, ?>> Iterator<Path> getPathsIterator(GraphVertex source,
                                                                         NetPathMapper<Path> remapper) {
        Set<GraphVertex> searchSpace = source.wrapped.getGroupSafe().getNodes().stream().filter(NetNode::isActive)
                .map(n -> n.wrapper).filter(node -> !source.equals(node)).collect(Collectors.toSet());
        return new LimitedIterator<>(source, searchSpace, remapper);
    }

    protected class LimitedIterator<Path extends INetPath<?, ?>> implements ICacheableIterator<Path> {

        private static final int MAX_ITERATIONS = 100;

        private final GraphVertex source;
        private final Set<GraphVertex> searchSpace;
        private final NetPathMapper<Path> remapper;

        private int iterationCount = 0;
        private final ObjectArrayList<Path> visited = new ObjectArrayList<>();
        private Path next;

        public LimitedIterator(GraphVertex source, Set<GraphVertex> searchSpace, NetPathMapper<Path> remapper) {
            this.source = source;
            this.searchSpace = searchSpace;
            this.remapper = remapper;
        }

        @Override
        public ICacheableIterator<Path> newCacheableIterator() {
            return new LimitedIterator<>(source, searchSpace, remapper);
        }

        @Override
        public Iterator<Path> newIterator() {
            return newCacheableIterator();
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
            var iter = searchSpace.stream().map(node -> paths.getPath(source, node)).filter(Objects::nonNull)
                    .map(remapper::map).sorted(Comparator.comparingDouble(INetPath::getWeight)).iterator();
            while (iter.hasNext()) {
                var next = iter.next();
                if (isUnique(next)) {
                    this.next = next;
                    break;
                }
            }
            if (next != null) visited.add(next);
        }

        private boolean isUnique(Path path) {
            for (Path other : visited) {
                if (path.matches(other)) return false;
            }
            return true;
        }
    }
}
