package gregtech.api.graphnet.logic;

import net.minecraftforge.fml.common.eventhandler.Event;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

import java.util.Comparator;

public final class NetLogicRegistrationEvent extends Event {

    private final ObjectRBTreeSet<NetLogicType<?>> gather = new ObjectRBTreeSet<>(
            Comparator.comparing(NetLogicType::getName));

    public void accept(NetLogicType<?> type) {
        if (!gather.add(type))
            throw new IllegalStateException("Detected a name collision during Net Logic registration!");
    }

    ObjectRBTreeSet<NetLogicType<?>> getGather() {
        return gather;
    }
}
