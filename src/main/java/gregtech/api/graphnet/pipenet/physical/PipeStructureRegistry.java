package gregtech.api.graphnet.pipenet.physical;

import gregtech.api.util.GTUtility;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public final class PipeStructureRegistry {

    private static final Map<Class<? extends IPipeStructure>, ObjectAVLTreeSet<? extends IPipeStructure>> REGISTRY = new Object2ObjectOpenHashMap<>();

    private static final Comparator<IPipeStructure> comparator = Comparator.comparing(IPipeStructure::getName);

    public static <T extends IPipeStructure> void register(@NotNull T structure) {
        ObjectAVLTreeSet<T> structures = (ObjectAVLTreeSet<T>) REGISTRY.get(structure.getClass());
        if (structures == null) {
            structures = new ObjectAVLTreeSet<>(comparator);
            REGISTRY.put(structure.getClass(), structures);
        }
        structures.add(structure);
    }

    /**
     * Do not modify the returned set.
     */
    public static <T extends IPipeStructure> @NotNull Set<T> getStructures(Class<T> structureClass) {
        GTUtility.forceInitialization(structureClass);
        ObjectAVLTreeSet<T> structures = (ObjectAVLTreeSet<T>) REGISTRY.get(structureClass);
        if (structures == null) return Collections.emptySet();
        return structures;
    }
}
