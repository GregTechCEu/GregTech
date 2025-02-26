package gregtech.common.pipelike.net.energy;

import gregtech.api.graphnet.group.PathCacheGroupData;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.path.PathBuilder;
import gregtech.api.graphnet.traverse.NetIteratorSupplier;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class EnergyGroupData extends PathCacheGroupData {

    private long lastEnergyInPerSec;
    private long lastEnergyOutPerSec;
    private long energyInPerSec;
    private long energyOutPerSec;
    private long updateTime;

    public EnergyGroupData(NetIteratorSupplier iteratorSupplier) {
        super(iteratorSupplier);
    }

    public EnergyGroupData(NetIteratorSupplier iteratorSupplier,
                           @NotNull Reference2ReferenceOpenHashMap<NetNode, SecondaryCache> cache) {
        super(iteratorSupplier, cache);
    }

    public long getEnergyInPerSec(long queryTick) {
        updateCache(queryTick);
        return lastEnergyInPerSec;
    }

    public long getEnergyOutPerSec(long queryTick) {
        updateCache(queryTick);
        return lastEnergyOutPerSec;
    }

    public void addEnergyInPerSec(long energy, long queryTick) {
        updateCache(queryTick);
        energyInPerSec += energy;
    }

    public void addEnergyOutPerSec(long energy, long queryTick) {
        updateCache(queryTick);
        energyOutPerSec += energy;
    }

    private void updateCache(long queryTick) {
        if (queryTick > updateTime) {
            updateTime = updateTime + 20;
            clearCache();
        }
    }

    public void clearCache() {
        lastEnergyInPerSec = energyInPerSec;
        lastEnergyOutPerSec = energyOutPerSec;
        energyInPerSec = 0;
        energyOutPerSec = 0;
    }

    @Override
    protected PathBuilder createBuilder(@NotNull NetNode origin) {
        return new StandardEnergyPath.Builder(origin);
    }

    @Override
    protected NetPath buildSingleton(@NotNull NetNode singleton) {
        return new StandardEnergyPath.SingletonEnergyPath(singleton);
    }

    @Override
    protected @NotNull PathCacheGroupData buildFilteredCache(@NotNull Set<NetNode> filterNodes) {
        Reference2ReferenceOpenHashMap<NetNode, SecondaryCache> child = new Reference2ReferenceOpenHashMap<>(
                this.cache);
        child.entrySet().removeIf(entry -> {
            if (!filterNodes.contains(entry.getKey())) return true;
            SecondaryCache cache = entry.getValue();
            cache.keySet().retainAll(filterNodes);
            return cache.isEmpty();
        });
        return new EnergyGroupData(iteratorSupplier, child);
    }
}
