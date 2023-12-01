package gregtech.api.fluids.store;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTLog;

import net.minecraftforge.fluids.Fluid;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class FluidStorage {

    private final Map<FluidStorageKey, Fluid> map = new Object2ObjectOpenHashMap<>();
    private Map<FluidStorageKey, FluidBuilder> toRegister = new Object2ObjectOpenHashMap<>();

    private boolean registered = false;

    public FluidStorage() {}

    /**
     * Enqueue a fluid for registration
     *
     * @param key     the key corresponding with the fluid
     * @param builder the FluidBuilder to build
     */
    public void enqueueRegistration(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
        if (registered) {
            throw new IllegalStateException("Cannot enqueue a builder after registration");
        }

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
        if (registered) {
            throw new IllegalArgumentException("FluidStorage has already been registered");
        }
        return toRegister.get(key);
    }

    /**
     * Register the enqueued fluids
     *
     * @param material the material the fluid is based off of
     */
    @ApiStatus.Internal
    public void registerFluids(@NotNull Material material) {
        if (registered) {
            throw new IllegalStateException("FluidStorage has already been registered");
        }

        // If nothing is queued for registration and nothing is manually stored,
        // we need something for the registry to handle this will prevent cases
        // of a material having a fluid property but no fluids actually created
        // for the material.
        if (toRegister.isEmpty() && map.isEmpty()) {
            enqueueRegistration(FluidStorageKeys.LIQUID, new FluidBuilder());
        }

        for (var entry : toRegister.entrySet()) {
            Fluid fluid = entry.getValue().build(material.getModid(), material, entry.getKey());
            if (!storeNoOverwrites(entry.getKey(), fluid)) {
                GTLog.logger.error("{} already has an associated fluid for material {}", material);
            }
        }
        toRegister = null;
        registered = true;
    }

    /**
     * @param key the key corresponding with the fluid
     * @return the fluid associated with the key
     */
    public @Nullable Fluid get(@NotNull FluidStorageKey key) {
        return map.get(key);
    }

    /**
     * Will do nothing if an existing fluid association would be overwritten.
     *
     * @param key   the key to associate with the fluid
     * @param fluid the fluid to associate with the key
     * @return if the associations were successfully updated
     */
    public boolean storeNoOverwrites(@NotNull FluidStorageKey key, @NotNull Fluid fluid) {
        if (map.containsKey(key)) {
            return false;
        }
        store(key, fluid);
        return true;
    }

    /**
     * Will overwrite existing fluid associations.
     *
     * @param key   the key to associate with the fluid
     * @param fluid the fluid to associate with the key
     */
    public void store(@NotNull FluidStorageKey key, @NotNull Fluid fluid) {
        map.put(key, fluid);
    }
}
