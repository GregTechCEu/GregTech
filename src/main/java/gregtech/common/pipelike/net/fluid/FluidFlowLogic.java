package gregtech.common.pipelike.net.fluid;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.logic.RingBufferTransientLogicData;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.util.TickUtil;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class FluidFlowLogic extends RingBufferTransientLogicData<FluidFlowLogic, Object2LongMap<FluidTestObject>> {

    public static final NetLogicType<FluidFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "FluidFlow",
            FluidFlowLogic::new, new FluidFlowLogic());

    public static final int MEMORY_TICKS = WorldFluidNet.getBufferTicks();

    private Object2LongMap<FluidTestObject> sum = new Object2LongOpenHashMap<>();
    private FluidTestObject last;

    public FluidFlowLogic() {
        super(MEMORY_TICKS);
    }

    @Override
    public @NotNull NetLogicType<FluidFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Object2LongMap<FluidTestObject> getSum(boolean reducedUpdate) {
        updateBuffer(TickUtil.getTick(), reducedUpdate);
        return sum;
    }

    public @NotNull Object2LongMap<FluidTestObject> getFlow(int tick) {
        updateBuffer(tick, false);
        return getCurrentOrDefault(Object2LongMaps.emptyMap());
    }

    public void recordFlow(int tick, @NotNull FluidStack flow) {
        recordFlow(tick, new FluidTestObject(flow), flow.amount);
    }

    public void recordFlow(int tick, @NotNull FluidTestObject testObject, int amount) {
        updateBuffer(tick, false);
        Object2LongMap<FluidTestObject> map = computeCurrentIfAbsent(() -> new Object2LongArrayMap<>(4));
        map.put(testObject, map.getLong(testObject) + amount);
        sum.put(testObject, sum.getLong(testObject) + amount);
        last = testObject;
    }

    @Override
    protected void dropEntry(Object2LongMap<FluidTestObject> entry) {
        super.dropEntry(entry);
        for (var e : entry.object2LongEntrySet()) {
            long fetch = sum.getLong(e.getKey());
            if (e.getLongValue() >= fetch) {
                sum.remove(e.getKey());
            } else {
                sum.put(e.getKey(), fetch - e.getLongValue());
            }
        }
    }

    public FluidTestObject getLast() {
        return last;
    }
}
