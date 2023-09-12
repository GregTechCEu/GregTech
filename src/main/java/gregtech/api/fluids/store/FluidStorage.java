package gregtech.api.fluids.store;

import gregtech.api.fluids.builder.FluidBuilder;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class FluidStorage {

    private final Map<FluidStorageKey, Fluid> map = new Object2ObjectOpenHashMap<>();
    private final Map<FluidStorageKey, FluidBuilder> toRegister = new Object2ObjectOpenHashMap<>();

    public FluidStorage() {}

    /**
     * Enqueue a fluid for registration
     *
     * @param key the key corresponding with the fluid
     * @param builder the FluidBuilder to build
     */
    @ApiStatus.Internal
    public void queue(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
        if (toRegister.containsKey(key)) {
            throw new IllegalArgumentException("FluidStorageKey " + key + " is already queued");
        }
        toRegister.put(key, builder);
    }

    /**
     * @param key the key corresponding with the FluidBuilder
     * @return the fluid builder queued to be registered
     */
    public @Nullable FluidBuilder getQueuedBuilder(@NotNull FluidStorageKey key) {
        return toRegister.get(key);
    }

    /**
     * Register the enqueued fluids
     *
     * @param material the material the fluid is based off of
     */
    @ApiStatus.Internal
    public void register(@NotNull Material material) {
        for (var entry : toRegister.entrySet()) {
            storeWithLogging(entry.getKey(), entry.getValue().build(material.getModid(), material, entry.getKey()), material);
        }
        toRegister.clear();
    }

    /**
     * @param key the key corresponding with the fluid
     * @return the fluid associated with the key
     */
    public @Nullable Fluid get(@NotNull FluidStorageKey key) {
        return map.get(key);
    }

    /**
     * @see #store(FluidStorageKey, Fluid)
     */
    private void storeWithLogging(@NotNull FluidStorageKey key, @NotNull Fluid fluid, @NotNull Material material) {
        if (map.containsKey(key)) {
            GTLog.logger.error("{} already has an associated fluid for material {}", material);
            return;
        }
        map.put(key, fluid);
    }

    /**
     * @param key the key to associate with the fluid
     * @param fluid the fluid to associate with the key
     * @throws IllegalArgumentException if a key is already associated with another fluid
     */
    public void store(@NotNull FluidStorageKey key, @NotNull Fluid fluid) {
        if (map.containsKey(key)) {
            throw new IllegalArgumentException(key + " already has an associated fluid");
        }
        map.put(key, fluid);
    }
}
