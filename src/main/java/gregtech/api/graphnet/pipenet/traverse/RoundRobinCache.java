package gregtech.api.graphnet.pipenet.traverse;

import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.traverse.iter.NetIterator;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Desugar
public record RoundRobinCache(ObjectLinkedOpenHashSet<NetNode> sourceCache,
                              ObjectLinkedOpenHashSet<NetNode> destCache) {

    public static RoundRobinCache create() {
        return new RoundRobinCache(new ObjectLinkedOpenHashSet<>(), new ObjectLinkedOpenHashSet<>());
    }

    public Supplier<Predicate<NetNode>> buildSupplier(Collection<NetNode> sourceCandidates,
                                                      Collection<NetNode> destCandidates) {
        return sourceCandidates.isEmpty() || destCandidates.isEmpty() ? () -> null :
                new CacheSupplier(sourceCandidates, destCandidates);
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
        return new RoundRobinCache(sourceCache.clone(), destCache.clone());
    }

    private final class CacheSupplier implements Supplier<Predicate<NetNode>> {

        private final ArrayDeque<NetNode> sources;
        private ArrayDeque<NetNode> dests;
        private ArrayDeque<NetNode> destBacklog;

        public CacheSupplier(Collection<NetNode> sourceCandidates, Collection<NetNode> destCandidates) {
            this.sources = new ArrayDeque<>(sourceCandidates);
            this.dests = new ArrayDeque<>(destCandidates);
            this.destBacklog = new ArrayDeque<>(destCandidates.size());
        }

        @Override
        public Predicate<NetNode> get() {
            if (dests.isEmpty()) {
                ArrayDeque<NetNode> queue = dests;
                dests = destBacklog;
                destBacklog = queue;
                sources.removeFirst();
                if (sources.isEmpty()) return null;
                int i = 0;
                while (true) {
                    NetNode s = sources.peekFirst();
                    // yeet the first if we've gone through the entire deque without a match
                    if (i >= sources.size()) {
                        sourceCache.removeFirst();
                        i = 0;
                    }
                    if (!sourceCache.contains(s) || sourceCache.first() == s) break;
                    i++;
                    sources.addLast(sources.removeFirst());
                }
            }
            NetNode s = sources.peekFirst();
            NetNode d;
            int i = 0;
            while (true) {
                d = dests.removeFirst();
                // yeet the first if we've gone through the entire deque without a match
                if (i >= dests.size()) {
                    destCache.removeFirst();
                    i = 0;
                }
                if (!destCache.contains(d) || destCache.first() == d) break;
                i++;
                dests.addLast(d);

            }
            destBacklog.addLast(d);
            sourceCache.addAndMoveToLast(s);
            destCache.addAndMoveToLast(d);
            NetNode finalD = d;
            return n -> n == s || n == finalD;
        }
    }
}
