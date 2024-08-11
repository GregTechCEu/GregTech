package gregtech.api.graphnet.pipenet.physical;

import gregtech.api.util.GTUtility;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class PipeStructureRegistry {

    private static final Map<Class<? extends IPipeStructure>, Set<? extends IPipeStructure>> REGISTRY = new Object2ObjectLinkedOpenHashMap<>();

    public static <T extends IPipeStructure> void register(@NotNull T structure) {
        Set<T> structures = (Set<T>) REGISTRY.get(structure.getClass());
        if (structures == null) {
            structures = new LinkedHashSet<>();
            REGISTRY.put(structure.getClass(), structures);
        }
        structures.add(structure);
    }

    /**
     * Do not modify the returned set.
     */
    public static <T extends IPipeStructure> @NotNull Set<T> getStructures(Class<T> structureClass) {
        GTUtility.forceInitialization(structureClass);
        Set<T> structures = (Set<T>) REGISTRY.get(structureClass);
        if (structures == null) return Collections.emptySet();
        return structures;
    }
}
