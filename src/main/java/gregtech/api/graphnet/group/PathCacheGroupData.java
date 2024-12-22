package gregtech.api.graphnet.group;

import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.path.PathBuilder;
import gregtech.api.graphnet.path.SingletonNetPath;
import gregtech.api.graphnet.path.StandardNetPath;
import gregtech.api.graphnet.traverse.iter.EdgeDirection;
import gregtech.api.graphnet.traverse.iter.NetIterator;
import gregtech.api.graphnet.traverse.iter.NetIteratorSupplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PathCacheGroupData extends GroupData {

    protected final NetIteratorSupplier iteratorSupplier;

    protected final @NotNull Object2ObjectOpenHashMap<NetNode, SecondaryCache> cache;

    public PathCacheGroupData(NetIteratorSupplier iteratorSupplier) {
        this(iteratorSupplier, new Object2ObjectOpenHashMap<>());
    }

    public PathCacheGroupData(NetIteratorSupplier iteratorSupplier,
                              @NotNull Object2ObjectOpenHashMap<NetNode, SecondaryCache> cache) {
        this.cache = cache;
        this.iteratorSupplier = iteratorSupplier;
    }

    @NotNull
    public SecondaryCache getOrCreate(@NotNull NetNode source) {
        return cache.computeIfAbsent(source, SecondaryCache::new);
    }

    public void invalidateAll() {
        cache.clear();
        cache.trim(16);
    }

    public void notifyTopologicalChange() {
        cache.forEach((key, value) -> value.notifyTopologicalChange());
    }

    public class SecondaryCache extends Object2ObjectOpenHashMap<NetNode, NetPath> {

        protected final @NotNull NetNode source;
        protected @Nullable NetIterator searchFrontier;

        protected NetPath singleton;

        protected int frontierPosition;

        public SecondaryCache(@NotNull NetNode source) {
            this.source = source;
        }

        @Nullable
        public NetPath getOrCompute(@NotNull NetNode target) {
            if (target == source) {
                if (singleton == null) singleton = buildSingleton(source);
                return singleton;
            }

            if (searchFrontier == null) searchFrontier = iteratorSupplier.create(source, EdgeDirection.OUTGOING);

            NetPath existing = this.get(target);
            if (existing != null) return existing;
            NetIterator targetFrontier = iteratorSupplier.create(target, EdgeDirection.INCOMING);
            int frontierPosition = 0;
            // first, attempt to bring the target frontier up to date with the search frontier.
            while (frontierPosition < this.frontierPosition && targetFrontier.hasNext()) {
                NetNode node = targetFrontier.next();
                frontierPosition++;
                if (searchFrontier.hasSeen(node)) {
                    NetPath built = buildPath(node, targetFrontier, searchFrontier);
                    this.put(target, built);
                    return built;
                }
            }
            // second, move both frontiers forward until intersect or exhaustion of iterators.
            while (searchFrontier.hasNext() && targetFrontier.hasNext()) {
                searchFrontier.next();
                NetNode node = targetFrontier.next();
                this.frontierPosition++;
                if (searchFrontier.hasSeen(node)) {
                    NetPath built = buildPath(node, targetFrontier, searchFrontier);
                    this.put(target, built);
                    return built;
                }
            }
            return null;
        }

        public void notifyTopologicalChange() {
            this.searchFrontier = null;
            this.frontierPosition = 0;
        }
    }

    protected PathBuilder createBuilder(@NotNull NetNode origin) {
        return new StandardNetPath.Builder(origin);
    }

    protected NetPath buildSingleton(@NotNull NetNode singleton) {
        return new SingletonNetPath(singleton);
    }

    protected NetPath buildPath(@NotNull NetNode intersect, @NotNull NetIterator targetFrontier,
                                @NotNull NetIterator searchFrontier) {
        PathBuilder builder = createBuilder(intersect);
        // first, assemble the path leading to the target frontier origin
        NetNode link = intersect;
        while (true) {
            NetEdge span = targetFrontier.getSpanningTreeEdge(link);
            if (span == null) break;
            link = span.getOppositeNode(link);
            if (link == null) return null;
            builder.addToEnd(link, span);
        }
        // second, assemble the path leading to the search frontier origin
        link = intersect;
        while (true) {
            NetEdge span = searchFrontier.getSpanningTreeEdge(link);
            if (span == null) break;
            link = span.getOppositeNode(link);
            if (link == null) return null;
            builder.addToStart(link, span);
        }
        return builder.build();
    }

    @Override
    public void notifyOfBridgingEdge(@NotNull NetEdge edge) {
        notifyTopologicalChange();
        invalidateAll();
    }

    @Override
    public void notifyOfRemovedEdge(@NotNull NetEdge edge) {
        notifyTopologicalChange();
        this.cache.values().removeIf(c -> {
            c.values().removeIf(p -> p.getOrderedEdges().contains(edge));
            return c.isEmpty();
        });
    }

    @Override
    protected @Nullable GroupData mergeAcross(@Nullable GroupData other, @NotNull NetEdge edge) {
        if (other instanceof PathCacheGroupData data) {
            this.cache.putAll(data.cache);
        }
        notifyTopologicalChange();
        return this;
    }

    @Override
    public @NotNull Pair<GroupData, GroupData> splitAcross(@NotNull Set<NetNode> sourceNodes,
                                                           @NotNull Set<NetNode> targetNodes) {
        notifyTopologicalChange();
        return ImmutablePair.of(buildFilteredCache(sourceNodes), buildFilteredCache(targetNodes));
    }

    protected @NotNull PathCacheGroupData buildFilteredCache(@NotNull Set<NetNode> filterNodes) {
        Object2ObjectOpenHashMap<NetNode, SecondaryCache> child = new Object2ObjectOpenHashMap<>(this.cache);
        child.entrySet().removeIf(entry -> {
            if (!filterNodes.contains(entry.getKey())) return true;
            SecondaryCache cache = entry.getValue();
            cache.keySet().retainAll(filterNodes);
            return cache.isEmpty();
        });
        return new PathCacheGroupData(iteratorSupplier, child);
    }
}
