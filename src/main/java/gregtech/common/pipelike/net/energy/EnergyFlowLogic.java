package gregtech.common.pipelike.net.energy;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.AbstractTransientLogicData;
import gregtech.api.graphnet.logic.NetLogicType;

import net.minecraftforge.fml.common.FMLCommonHandler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EnergyFlowLogic extends AbstractTransientLogicData<EnergyFlowLogic> {

    public static final NetLogicType<EnergyFlowLogic> TYPE = new NetLogicType<>(GTValues.MODID, "EnergyFlow",
            EnergyFlowLogic::new, new EnergyFlowLogic());

    public static final int MEMORY_TICKS = 10;

    private final Long2ObjectOpenHashMap<List<EnergyFlowData>> memory = new Long2ObjectOpenHashMap<>();

    @Override
    public @NotNull NetLogicType<EnergyFlowLogic> getType() {
        return TYPE;
    }

    public @NotNull Long2ObjectOpenHashMap<List<EnergyFlowData>> getMemory() {
        updateMemory(FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter());
        return memory;
    }

    public @NotNull List<EnergyFlowData> getFlow(long tick) {
        updateMemory(tick);
        return memory.getOrDefault(tick, Collections.emptyList());
    }

    public void recordFlow(long tick, EnergyFlowData flow) {
        updateMemory(tick);
        memory.computeIfAbsent(tick, k -> new ObjectArrayList<>()).add(flow);
    }

    private void updateMemory(long tick) {
        var iter = memory.long2ObjectEntrySet().fastIterator();
        while (iter.hasNext()) {
            Long2ObjectMap.Entry<List<EnergyFlowData>> entry = iter.next();
            if (entry.getLongKey() + MEMORY_TICKS < tick) {
                iter.remove();
            }
        }
    }
}
