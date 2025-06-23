package gregtech.api.unification.material.properties;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorage;
import gregtech.api.fluids.store.FluidStorageImpl;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidProperty implements IMaterialProperty, FluidStorage {

    private final FluidStorageImpl storage = new FluidStorageImpl();
    private FluidStorageKey primaryKey = null;
    private @Nullable Fluid solidifyingFluid = null;

    public FluidProperty() {}

    /**
     * Helper constructor which automatically calls {@link #enqueueRegistration(FluidStorageKey, FluidBuilder)} for a
     * builder.
     * <p>
     * This is primarily useful for adding FluidProperties to materials after they are registered with a single fluid
     * stored.
     *
     * @param key     the fluid storage key to store the builder with
     * @param builder the builder to enqueue
     */
    public FluidProperty(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
        enqueueRegistration(key, builder);
    }

    /**
     * @see FluidStorageImpl#registerFluids(Material)
     */
    @ApiStatus.Internal
    public void registerFluids(@NotNull Material material) {
        this.storage.registerFluids(material);
    }

    @Override
    public void enqueueRegistration(@NotNull FluidStorageKey key, @NotNull FluidBuilder builder) {
        storage.enqueueRegistration(key, builder);
        if (primaryKey == null) {
            primaryKey = key;
        }
    }

    @Override
    public void store(@NotNull FluidStorageKey key, @NotNull Fluid fluid) {
        storage.store(key, fluid);
        if (primaryKey == null) {
            primaryKey = key;
        }
    }

    @Override
    public @Nullable Fluid get(@NotNull FluidStorageKey key) {
        return storage.get(key);
    }

    @Override
    public @Nullable FluidBuilder getQueuedBuilder(@NotNull FluidStorageKey key) {
        return storage.getQueuedBuilder(key);
    }

    /**
     *
     * @return the key the fluid is stored with primarily
     */
    public @NotNull FluidStorageKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * @param primaryKey the key to use primarily
     */
    public FluidProperty setPrimaryKey(@NotNull FluidStorageKey primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (this.primaryKey == null) {
            throw new IllegalStateException("FluidProperty cannot be empty");
        }
    }

    /**
     * @return the Fluid which solidifies into the material.
     */
    public @Nullable Fluid solidifiesFrom() {
        if (this.solidifyingFluid == null) {
            return storage.get(FluidStorageKeys.LIQUID);
        }
        return solidifyingFluid;
    }

    /**
     * @param amount the size of the returned FluidStack.
     * @return a FluidStack of the Fluid which solidifies into the material.
     */
    public FluidStack solidifiesFrom(int amount) {
        return new FluidStack(solidifiesFrom(), amount);
    }

    /**
     * Sets the fluid that solidifies into the material.
     *
     * @param solidifyingFluid The Fluid which solidifies into the material. If left null, it will be left as the
     *                         default value: the material's liquid.
     */
    public FluidProperty setSolidifyingFluid(@Nullable Fluid solidifyingFluid) {
        this.solidifyingFluid = solidifyingFluid;
        return this;
    }
}
