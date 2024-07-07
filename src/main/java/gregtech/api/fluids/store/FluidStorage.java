package gregtech.api.fluids.store;

import gregtech.api.fluids.FluidBuilder;

import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FluidStorage {

    /**
     * Enqueue a fluid for registration
     *
     * @param key     the key corresponding with the fluid
     * @param builder the FluidBuilder to build
     */
    void enqueueRegistration(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder);

    /**
     * @param key the key corresponding with the FluidBuilder
     * @return the fluid builder queued to be registered
     */
    @Nullable
    FluidBuilder getQueuedBuilder(@NotNull FluidStorageKey key);

    /**
     * @param key the key corresponding with the fluid
     * @return the fluid associated with the key
     */
    @Nullable
    Fluid get(@NotNull FluidStorageKey key);

    /**
     * Will overwrite existing fluid associations.
     *
     * @param key   the key to associate with the fluid
     * @param fluid the fluid to associate with the key
     */
    void store(@NotNull FluidStorageKey key, @NotNull Fluid fluid);
}
