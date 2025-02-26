package gregtech.api.graphnet;

import net.minecraftforge.fml.common.eventhandler.Event;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

import java.util.Comparator;

public final class GraphClassRegistrationEvent extends Event {

    private final ObjectRBTreeSet<GraphClassType<?>> gather = new ObjectRBTreeSet<>(
            Comparator.comparing(GraphClassType::getName));

    public void accept(GraphClassType<?> type) {
        if (!gather.add(type))
            throw new IllegalStateException(
                    "Detected a name collision during Graph Class registration! Collision on name: " + type.getName());
    }

    ObjectRBTreeSet<GraphClassType<?>> getGather() {
        return gather;
    }
}
