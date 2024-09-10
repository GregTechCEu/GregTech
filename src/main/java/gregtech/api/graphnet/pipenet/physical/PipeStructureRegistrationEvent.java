package gregtech.api.graphnet.pipenet.physical;

import net.minecraftforge.fml.common.eventhandler.Event;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public final class PipeStructureRegistrationEvent extends Event {

    private final Map<Class<? extends IPipeStructure>, Set<? extends IPipeStructure>> registry = new Object2ObjectLinkedOpenHashMap<>();

    public <T extends IPipeStructure> void register(@NotNull T structure) {
        Set<T> structures = (Set<T>) registry.computeIfAbsent(structure.getClass(),
                k -> new ObjectLinkedOpenHashSet<>());
        structures.add(structure);
    }

    Map<Class<? extends IPipeStructure>, Set<? extends IPipeStructure>> getRegistry() {
        return registry;
    }
}
