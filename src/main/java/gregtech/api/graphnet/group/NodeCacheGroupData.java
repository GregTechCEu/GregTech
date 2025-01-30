package gregtech.api.graphnet.group;

import gregtech.api.graphnet.net.NetNode;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class NodeCacheGroupData<T> extends GroupData {

    protected final @NotNull Reference2ReferenceOpenHashMap<NetNode, T> cache;

    public NodeCacheGroupData() {
        this(new Reference2ReferenceOpenHashMap<>());
    }

    public NodeCacheGroupData(@NotNull Reference2ReferenceOpenHashMap<NetNode, T> cache) {
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
