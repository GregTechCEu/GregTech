package gregtech.api.unification.material.properties;

import gregtech.api.fluids.store.FluidStorage;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class FluidProperty implements IMaterialProperty {

    private final FluidStorage storage = new FluidStorage();
    private FluidStorageKey primaryKey = null;
    private @Nullable Fluid solidifyingFluid = null;

    public FluidProperty() {}

    public @NotNull FluidStorage getStorage() {
        return this.storage;
    }

    /**
     * This is only {@code Nullable} internally during the Material creation process.
     * All external use should assume this is {@code Nonnull}.
     *
     * @return the FluidStorageKey fluid is stored as primarily
     */
    public @UnknownNullability FluidStorageKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * @param primaryKey the key to use primarily
     */
    public void setPrimaryKey(@NotNull FluidStorageKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (this.primaryKey == null) {
            throw new IllegalStateException("PrimaryKey cannot be null after property verification");
        }
    }

    /**
     * @return the Fluid which solidifies into the material.
     */
    public @Nullable Fluid solidifiesFrom() {
        if (this.solidifyingFluid == null) {
            return getStorage().get(FluidStorageKeys.LIQUID);
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
    public void setSolidifyingFluid(@Nullable Fluid solidifyingFluid) {
        this.solidifyingFluid = solidifyingFluid;
    }
}
