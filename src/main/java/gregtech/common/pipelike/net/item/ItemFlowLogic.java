package gregtech.common.pipelike.net.item;

import gregtech.api.GTValues;
import gregtech.api.graphnet.GraphNetUtility;
import gregtech.api.graphnet.logic.AbstractTransientLogicData;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.util.TickUtil;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import org.jetbrains.annotations.NotNull;

public class ItemFlowLogic extends AbstractTransientLogicData<ItemFlowLogic> {

    public static final NetLogicType<ItemFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "ItemFlow",
            ItemFlowLogic::new, new ItemFlowLogic());

    public static final int MEMORY_TICKS = WorldItemNet.getBufferTicks();
    public static final int BUFFER_MULT = MEMORY_TICKS / WorldItemNet.getBufferRegenerationFactor();

    private final Int2ObjectArrayMap<Object2LongMap<ItemTestObject>> memory = new Int2ObjectArrayMap<>(MEMORY_TICKS);
    private ItemTestObject last;

    @Override
    public @NotNull NetLogicType<ItemFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Int2ObjectMap<Object2LongMap<ItemTestObject>> getMemory() {
        updateMemory(TickUtil.getTick());
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

    public @NotNull Object2LongMap<ItemTestObject> getFlow(int tick) {
        updateMemory(tick);
        Object2LongMap<ItemTestObject> fetch = memory.get(tick);
        return fetch != null ? fetch : Object2LongMaps.emptyMap();
    }

    public void recordFlow(int tick, @NotNull ItemStack flow) {
        recordFlow(tick, new ItemTestObject(flow), flow.getCount());
    }

    public void recordFlow(int tick, @NotNull ItemTestObject testObject, int amount) {
        updateMemory(tick);
        Object2LongMap<ItemTestObject> map = GraphNetUtility.computeIfAbsent(memory, tick,
                k -> new Object2LongArrayMap<>(4));
        map.put(testObject, map.getLong(testObject) + amount);
        last = testObject;
    }

    public ItemTestObject getLast() {
        return last;
    }

    private void updateMemory(int tick) {
        var iter = memory.int2ObjectEntrySet().fastIterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            if (entry.getIntKey() + MEMORY_TICKS < tick) {
                iter.remove();
            }
        }
    }
}
