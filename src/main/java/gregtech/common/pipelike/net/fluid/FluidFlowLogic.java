package gregtech.common.pipelike.net.fluid;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.AbstractTransientLogicData;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.util.GTUtility;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import org.jetbrains.annotations.NotNull;

public class FluidFlowLogic extends AbstractTransientLogicData<FluidFlowLogic> {

    public static final NetLogicType<FluidFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "FluidFlow",
            FluidFlowLogic::new, new FluidFlowLogic());

    public static final int MEMORY_TICKS = WorldFluidNet.getBufferTicks();

    private final Long2ObjectOpenHashMap<Object2LongMap<FluidTestObject>> memory = new Long2ObjectOpenHashMap<>();
    private FluidStack last;

    @Override
    public @NotNull NetLogicType<FluidFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Long2ObjectOpenHashMap<Object2LongMap<FluidTestObject>> getMemory() {
        updateMemory(GTUtility.getTick());
        return memory;
    }

    public @NotNull Object2LongMap<FluidTestObject> getSum() {
        Object2LongMap<FluidTestObject> sum = new Object2LongArrayMap<>();
        for (Object2LongMap<FluidTestObject> list : getMemory().values()) {
            for (var entry : list.object2LongEntrySet()) {
                sum.put(entry.getKey(), sum.getLong(entry.getKey()) + entry.getLongValue());
            }
        }
        return sum;
    }

    public @NotNull Object2LongMap<FluidTestObject> getFlow(long tick) {
        updateMemory(tick);
        return memory.getOrDefault(tick, Object2LongMaps.emptyMap());
    }

    public void recordFlow(long tick, @NotNull FluidStack flow) {
        recordFlow(tick, new FluidTestObject(flow), flow.amount);
    }

    public void recordFlow(long tick, @NotNull FluidTestObject testObject, int amount) {
        updateMemory(tick);
        Object2LongMap<FluidTestObject> map = memory.computeIfAbsent(tick, k -> new Object2LongArrayMap<>());
        map.put(testObject, map.getLong(testObject) + amount);
        last = testObject.recombine(amount);
    }

    public FluidStack getLast() {
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
