package gregtech.api.graphnet.predicate;

import net.minecraftforge.fml.common.eventhandler.Event;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

import java.util.Comparator;

public final class NetPredicateRegistrationEvent extends Event {

    private final ObjectRBTreeSet<NetPredicateType<?>> gather = new ObjectRBTreeSet<>(
            Comparator.comparing(NetPredicateType::getName));

    public void accept(NetPredicateType<?> type) {
        if (!gather.add(type))
            throw new IllegalStateException("Detected a name collision during Net Predicate registration!");
    }

    ObjectRBTreeSet<NetPredicateType<?>> getGather() {
        return gather;
    }
}
