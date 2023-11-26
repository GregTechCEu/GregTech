package gregtech.api.unification.material.registry;

import gregtech.api.unification.material.MarkerMaterial;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class MarkerMaterialRegistry {

    private static MarkerMaterialRegistry INSTANCE;

    private final Map<String, MarkerMaterial> map = new Object2ObjectOpenHashMap<>();

    private MarkerMaterialRegistry() {}

    public static MarkerMaterialRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MarkerMaterialRegistry();
        }
        return INSTANCE;
    }

    /**
     * @param markerMaterial the MarkerMaterial to register
     * @return the registered MarkerMaterial
     */
    public @NotNull MarkerMaterial registerMarkerMaterial(@NotNull MarkerMaterial markerMaterial) {
        MarkerMaterial existing = map.get(markerMaterial.getName());
        if (existing != null) return existing;

        map.put(markerMaterial.getName(), markerMaterial);
        return markerMaterial;
    }

    /**
     * @param name the name of the MarkerMaterial
     * @return the MarkerMaterial associated with the name
     */
    public @Nullable MarkerMaterial getMarkerMaterial(@NotNull String name) {
        return map.get(name);
    }

    /**
     * @return all registered marker materials
     */
    public @NotNull @UnmodifiableView Collection<@NotNull MarkerMaterial> getAll() {
        return Collections.unmodifiableCollection(map.values());
    }
}
