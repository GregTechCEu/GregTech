package gregtech.common.pipelike.net.energy;

import gregtech.api.GTValues;
import gregtech.api.graphnet.GraphNetUtility;
import gregtech.api.graphnet.logic.AbstractTransientLogicData;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.util.TickUtil;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EnergyFlowLogic extends AbstractTransientLogicData<EnergyFlowLogic> {

    public static final NetLogicType<EnergyFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "EnergyFlow",
            EnergyFlowLogic::new, new EnergyFlowLogic());

    public static final int MEMORY_TICKS = 10;

    private final Int2ObjectArrayMap<List<EnergyFlowData>> memory = new Int2ObjectArrayMap<>(MEMORY_TICKS);

    @Override
    public @NotNull NetLogicType<EnergyFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Int2ObjectMap<List<EnergyFlowData>> getMemory() {
        updateMemory(TickUtil.getTick());
        return memory;
    }

    public @NotNull List<EnergyFlowData> getFlow(int tick) {
        updateMemory(tick);
        List<EnergyFlowData> fetch = memory.get(tick);
        return fetch != null ? fetch : Collections.emptyList();
    }

    public void recordFlow(int tick, EnergyFlowData flow) {
        updateMemory(tick);
        GraphNetUtility.computeIfAbsent(memory, tick, k -> new ObjectArrayList<>(4)).add(flow);
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
