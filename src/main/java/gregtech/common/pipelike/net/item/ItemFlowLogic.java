package gregtech.common.pipelike.net.item;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.AbstractTransientLogicData;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import org.jetbrains.annotations.NotNull;

public class ItemFlowLogic extends AbstractTransientLogicData<ItemFlowLogic> {

    public static final NetLogicType<ItemFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "ItemFlow",
            ItemFlowLogic::new, new ItemFlowLogic());

    public static final int MEMORY_TICKS = WorldItemNet.getBufferTicks();
    public static final int BUFFER_MULT = MEMORY_TICKS / WorldItemNet.getBufferRegenerationFactor();

    private final Long2ObjectOpenHashMap<Object2LongMap<ItemTestObject>> memory = new Long2ObjectOpenHashMap<>();
    private ItemStack last;

    @Override
    public @NotNull NetLogicType<ItemFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Long2ObjectOpenHashMap<Object2LongMap<ItemTestObject>> getMemory() {
        updateMemory(GTUtility.getTick());
        return memory;
    }

    public @NotNull Object2LongMap<ItemTestObject> getSum() {
        Object2LongMap<ItemTestObject> sum = new Object2LongArrayMap<>();
        for (Object2LongMap<ItemTestObject> list : getMemory().values()) {
            for (var entry : list.object2LongEntrySet()) {
                sum.put(entry.getKey(), sum.getLong(entry.getKey()) + entry.getLongValue());
            }
        }
        return sum;
    }

    public @NotNull Object2LongMap<ItemTestObject> getFlow(long tick) {
        updateMemory(tick);
        return memory.getOrDefault(tick, Object2LongMaps.emptyMap());
    }

    public void recordFlow(long tick, @NotNull ItemStack flow) {
        recordFlow(tick, new ItemTestObject(flow), flow.getCount());
    }

    public void recordFlow(long tick, @NotNull ItemTestObject testObject, int amount) {
        updateMemory(tick);
        Object2LongMap<ItemTestObject> map = memory.computeIfAbsent(tick, k -> new Object2LongArrayMap<>());
        map.put(testObject, map.getLong(testObject) + amount);
        last = testObject.recombine(amount);
    }

    public ItemStack getLast() {
        return last;
    }

    private void updateMemory(long tick) {
        var iter = memory.long2ObjectEntrySet().fastIterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            if (entry.getLongKey() + MEMORY_TICKS < tick) {
                iter.remove();
            }
        }
    }
}
