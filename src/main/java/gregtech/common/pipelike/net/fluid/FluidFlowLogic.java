package gregtech.common.pipelike.net.fluid;

import gregtech.api.GTValues;
import gregtech.api.graphnet.GraphNetUtility;
import gregtech.api.graphnet.logic.AbstractTransientLogicData;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.util.TickUtil;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class FluidFlowLogic extends AbstractTransientLogicData<FluidFlowLogic> {

    public static final NetLogicType<FluidFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "FluidFlow",
            FluidFlowLogic::new, new FluidFlowLogic());

    public static final int MEMORY_TICKS = WorldFluidNet.getBufferTicks();

    private final Int2ObjectArrayMap<Object2LongMap<FluidTestObject>> memory = new Int2ObjectArrayMap<>(MEMORY_TICKS);
    private FluidTestObject last;

    @Override
    public @NotNull NetLogicType<FluidFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Int2ObjectMap<Object2LongMap<FluidTestObject>> getMemory() {
        updateMemory(TickUtil.getTick());
        return memory;
    }

    public @NotNull Object2LongMap<FluidTestObject> getSum() {
        Object2LongMap<FluidTestObject> sum = new Object2LongOpenHashMap<>();
        for (Object2LongMap<FluidTestObject> list : getMemory().values()) {
            for (var entry : list.object2LongEntrySet()) {
                sum.put(entry.getKey(), sum.getLong(entry.getKey()) + entry.getLongValue());
            }
        }
        return sum;
    }

    public @NotNull Object2LongMap<FluidTestObject> getFlow(int tick) {
        updateMemory(tick);
        Object2LongMap<FluidTestObject> fetch = memory.get(tick);
        return fetch != null ? fetch : Object2LongMaps.emptyMap();
    }

    public void recordFlow(int tick, @NotNull FluidStack flow) {
        recordFlow(tick, new FluidTestObject(flow), flow.amount);
    }

    public void recordFlow(int tick, @NotNull FluidTestObject testObject, int amount) {
        updateMemory(tick);
        Object2LongMap<FluidTestObject> map = GraphNetUtility.computeIfAbsent(memory, tick,
                k -> new Object2LongArrayMap<>(4));
        map.put(testObject, map.getLong(testObject) + amount);
        last = testObject;
    }

    public FluidTestObject getLast() {
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
