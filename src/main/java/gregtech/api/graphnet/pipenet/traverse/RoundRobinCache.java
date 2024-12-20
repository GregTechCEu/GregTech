package gregtech.api.graphnet.pipenet.traverse;

import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.traverse.iter.NetIterator;

import com.github.bsideup.jabel.Desugar;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Desugar
public record RoundRobinCache(LinkedHashSet<NetNode> sourceCache, LinkedHashSet<NetNode> destCache) {

    public static RoundRobinCache create() {
        return new RoundRobinCache(new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    public Supplier<Predicate<NetNode>> buildSupplier(Collection<NetNode> sourceCandidates,
                                                      Collection<NetNode> destCandidates) {
        return new CacheSupplier(sourceCandidates, destCandidates);
    }

    public RoundRobinCache refresh(NetIterator sources, NetIterator targets) {
        sourceCache.removeIf(n -> sources.getSpanningTreeEdge(n) == null);
        destCache.removeIf(n -> targets.getSpanningTreeEdge(n) == null);
        return this;
    }

    public void filter(Predicate<NetNode> sourceFilter, Predicate<NetNode> destFilter) {
        sourceCache.removeIf(sourceFilter);
        destCache.removeIf(destFilter);
    }

    public void clear() {
        sourceCache.clear();
        destCache.clear();
    }

    public RoundRobinCache copy() {
        return new RoundRobinCache(new LinkedHashSet<>(sourceCache), new LinkedHashSet<>(destCache));
    }

    private static final class CacheSupplier implements Supplier<Predicate<NetNode>> {

        private final Iterator<NetNode> sourceIterator;
        private final Collection<NetNode> destCandidates;
        private NetNode nextSource;
        private Iterator<NetNode> destIterator;

        public CacheSupplier(Collection<NetNode> sourceCandidates, Collection<NetNode> destCandidates) {
            this.destCandidates = destCandidates;
            this.destIterator = destCandidates.iterator();
            if (!destIterator.hasNext()) {
                sourceIterator = Collections.emptyIterator();
                nextSource = null;
            } else {
                sourceIterator = sourceCandidates.iterator();
                this.nextSource = sourceIterator.hasNext() ? sourceIterator.next() : null;
            }
        }

        @Override
        public Predicate<NetNode> get() {
            if (nextSource == null) {
                if (sourceIterator.hasNext()) nextSource = sourceIterator.next();
                if (nextSource == null) return null;
            }
            if (!destIterator.hasNext()) {
                destIterator = destCandidates.iterator();
                if (!destIterator.hasNext()) return null;
                nextSource = null;
                return get();
            }
            NetNode node = destIterator.next();
            return n -> n == nextSource || n == node;
        }
    }
}
