package gregtech.api.graphnet.gather;

import gregtech.api.graphnet.logic.INetLogicEntry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;
import java.util.function.Supplier;

public class GatherLogicsEvent extends Event {

    final Map<String, Supplier<INetLogicEntry<?, ?>>> gathered = new Object2ObjectOpenHashMap<>();

    GatherLogicsEvent() {}

    public void registerLogic(INetLogicEntry<?, ?> logic) {
        gathered.put(logic.getName(), logic::getNew);
    }
}
