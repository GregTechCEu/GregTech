package gregtech.common.pipelike.net.energy;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.logic.RingBufferTransientLogicData;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EnergyFlowLogic extends RingBufferTransientLogicData<EnergyFlowLogic, List<EnergyFlowData>> {

    public static final NetLogicType<EnergyFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "EnergyFlow",
            EnergyFlowLogic::new, new EnergyFlowLogic());

    public static final int MEMORY_TICKS = 10;

    public EnergyFlowLogic() {
        super(MEMORY_TICKS);
    }

    @Override
    public @NotNull NetLogicType<EnergyFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull List<EnergyFlowData> getFlow(int tick) {
        updateBuffer(tick, false);
        return getCurrentOrDefault(Collections.emptyList());
    }

    public void recordFlow(int tick, EnergyFlowData flow) {
        updateBuffer(tick, false);
        computeCurrentIfAbsent(() -> new ObjectArrayList<>(4)).add(flow);
    }
}
