package gregtech.common.pipelike.net.energy;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.logic.RingBufferTransientLogicData;
import gregtech.api.util.TickUtil;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class EnergyFlowLogic extends RingBufferTransientLogicData<EnergyFlowLogic, List<EnergyFlowData>> {

    public static final NetLogicType<EnergyFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "EnergyFlow",
            EnergyFlowLogic::new, new EnergyFlowLogic());

    public static final int MEMORY_TICKS = 10;

    private final Long2LongMap sum = new Long2LongOpenHashMap();
    private @Nullable EnergyFlowData last;

    public EnergyFlowLogic() {
        super(MEMORY_TICKS);
    }

    @Override
    public @NotNull NetLogicType<EnergyFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Long2LongMap getSum(boolean reducedUpdate) {
        updateBuffer(TickUtil.getTick(), reducedUpdate);
        return sum;
    }

    public @NotNull List<EnergyFlowData> getFlow(int tick) {
        updateBuffer(tick, false);
        return getCurrentOrDefault(Collections.emptyList());
    }

    public void recordFlow(int tick, EnergyFlowData flow) {
        updateBuffer(tick, false);
        computeCurrentIfAbsent(() -> new ObjectArrayList<>(4)).add(flow);
        sum.put(flow.voltage(), sum.get(flow.voltage()) + flow.amperage());
        last = flow;
    }

    @Override
    protected void dropEntry(List<EnergyFlowData> entry) {
        super.dropEntry(entry);
        for (var e : entry) {
            long fetch = sum.get(e.voltage());
            if (e.amperage() >= fetch) {
                sum.remove(e.voltage());
            } else {
                sum.put(e.voltage(), fetch - e.amperage());
            }
        }
    }

    public @Nullable EnergyFlowData getLast() {
        return last;
    }
}
