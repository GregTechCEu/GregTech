package gregtech.common.pipelike.net.item;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.logic.RingBufferTransientLogicData;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.util.TickUtil;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class ItemFlowLogic extends RingBufferTransientLogicData<ItemFlowLogic, Object2LongMap<ItemTestObject>> {

    public static final NetLogicType<ItemFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "ItemFlow",
            ItemFlowLogic::new, new ItemFlowLogic());

    public static final int MEMORY_TICKS = WorldItemNet.getBufferTicks();
    public static final int BUFFER_MULT = MEMORY_TICKS / WorldItemNet.getBufferRegenerationFactor();

    private Object2LongMap<ItemTestObject> sum = new Object2LongOpenHashMap<>();
    private ItemTestObject last;

    public ItemFlowLogic() {
        super(MEMORY_TICKS);
    }

    @Override
    public @NotNull NetLogicType<ItemFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Object2LongMap<ItemTestObject> getSum(boolean reducedUpdate) {
        updateBuffer(TickUtil.getTick(), reducedUpdate);
        return sum;
    }

    public @NotNull Object2LongMap<ItemTestObject> getFlow(int tick) {
        updateBuffer(tick, false);
        return getCurrentOrDefault(Object2LongMaps.emptyMap());
    }

    public void recordFlow(int tick, @NotNull ItemStack flow) {
        recordFlow(tick, new ItemTestObject(flow), flow.getCount());
    }

    public void recordFlow(int tick, @NotNull ItemTestObject testObject, int amount) {
        updateBuffer(tick, false);
        Object2LongMap<ItemTestObject> map = computeCurrentIfAbsent(() -> new Object2LongArrayMap<>(4));
        map.put(testObject, map.getLong(testObject) + amount);
        sum.put(testObject, sum.getLong(testObject) + amount);
        last = testObject;
    }

    @Override
    protected void dropEntry(Object2LongMap<ItemTestObject> entry) {
        for (var e : entry.object2LongEntrySet()) {
            long fetch = sum.getLong(e.getKey());
            if (e.getLongValue() >= fetch) {
                sum.remove(e.getKey());
            } else {
                sum.put(e.getKey(), fetch - e.getLongValue());
            }
        }
    }

    public ItemTestObject getLast() {
        return last;
    }
}
