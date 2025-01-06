package gregtech.api.graphnet.group;

import gregtech.api.graphnet.net.NetNode;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class NodeCacheGroupData<T> extends GroupData {

    protected final @NotNull Object2ObjectOpenHashMap<NetNode, T> cache;

    public NodeCacheGroupData() {
        this(new Object2ObjectOpenHashMap<>());
    }

    public NodeCacheGroupData(@NotNull Object2ObjectOpenHashMap<NetNode, T> cache) {
        this.cache = cache;
    }

    @NotNull
    public T getOrCreate(@NotNull NetNode node) {
        return cache.computeIfAbsent(node, this::getNew);
    }

    protected abstract T getNew(@NotNull NetNode node);

    public void invalidateAll() {
        cache.clear();
        cache.trim(16);
    }

    @Override
    public @NotNull Pair<GroupData, GroupData> splitAcross(@NotNull Set<NetNode> sourceNodes,
                                                           @NotNull Set<NetNode> targetNodes) {
        return ImmutablePair.of(buildFilteredCache(sourceNodes), buildFilteredCache(targetNodes));
    }

    protected abstract @NotNull NodeCacheGroupData<T> buildFilteredCache(@NotNull Set<NetNode> filterNodes);
}
