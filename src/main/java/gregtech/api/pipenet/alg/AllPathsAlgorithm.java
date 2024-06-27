package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.WorldPipeNetBase;
import gregtech.api.pipenet.alg.iter.ICacheableIterator;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import org.jgrapht.alg.shortestpath.DijkstraManyToManyShortestPaths;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AllPathsAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>, E extends NetEdge>
                              extends DijkstraManyToManyShortestPaths<NetNode<PT, NDT, E>, E>
                              implements INetAlgorithm<PT, NDT, E> {

    public AllPathsAlgorithm(WorldPipeNetBase<NDT, PT, E> pipenet) {
        super(pipenet.getGraph());
        if (!pipenet.isDirected()) throw new IllegalArgumentException("Cannot build all paths on an undirected graph!");
    }

    @Override
    public Iterator<NetPath<PT, NDT, E>> getPathsIterator(NetNode<PT, NDT, E> source) {
        Set<NetNode<PT, NDT, E>> searchSpace = source.getGroupSafe().getNodes().stream()
                .filter(NetNode::validTarget).filter(node -> !source.equals(node)).collect(Collectors.toSet());
        return new LimitedIterator(source, searchSpace);
    }

    @Override
    public boolean supportsDynamicWeights() {
        return true;
    }

    protected class LimitedIterator implements ICacheableIterator<NetPath<PT, NDT, E>> {

        private static final int MAX_ITERATIONS = 100;

        private final NetNode<PT, NDT, E> source;
        private final Set<NetNode<PT, NDT, E>> searchSpace;

        private int iterationCount = 0;
        private NetPath<PT, NDT, E> cachedNext;

        public LimitedIterator(NetNode<PT, NDT, E> source, Set<NetNode<PT, NDT, E>> searchSpace) {
            this.source = source;
            this.searchSpace = searchSpace;
        }

        @Override
        public ICacheableIterator<NetPath<PT, NDT, E>> newCacheableIterator() {
            return new LimitedIterator(source, searchSpace);
        }

        @Override
        public Iterator<NetPath<PT, NDT, E>> newIterator() {
            return new LimitedIterator(source, searchSpace);
        }

        @Override
        public boolean hasNext() {
            if (cachedNext == null && iterationCount < MAX_ITERATIONS) calculateNext();
            return cachedNext != null;
        }

        @Override
        public NetPath<PT, NDT, E> next() {
            if (!hasNext()) throw new NoSuchElementException();
            var temp = cachedNext;
            cachedNext = null;
            return temp;
        }

        private void calculateNext() {
            iterationCount++;
            if (iterationCount == 1) {
                cachedNext = new NetPath<>(source);
                return;
            }
            var paths = getManyToManyPaths(Collections.singleton(source), searchSpace);
            cachedNext = searchSpace.stream().map(node -> paths.getPath(source, node)).filter(Objects::nonNull)
                    .map(NetPath::new).min(Comparator.comparingDouble(NetPath::getWeight)).orElse(null);
        }
    }
}
