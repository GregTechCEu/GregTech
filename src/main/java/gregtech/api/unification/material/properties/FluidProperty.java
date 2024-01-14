package gregtech.api.unification.material.properties;

import gregtech.api.fluids.store.FluidStorage;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidProperty implements IMaterialProperty {

    private final FluidStorage storage = new FluidStorage();
    private @Nullable FluidStorageKey primaryKey = null;
    private @Nullable Fluid solidifyingFluid = null;

    public FluidProperty() {}

    public @NotNull FluidStorage getStorage() {
        return this.storage;
    }

    /**
     * @return the FluidStorageKey fluid is stored as primarily
     */
    public @Nullable FluidStorageKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * @param primaryKey the key to use primarily
     */
    public void setPrimaryKey(@Nullable FluidStorageKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}

    /**
     * @return the Fluid which solidifies into the material.
     */

    public Fluid solidifiesFrom() {
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
     * @param solidifyingFluid The Fluid which solidifies into the material. If left null, it will be left as the default value: the material's liquid.
     */
    public void setSolidifyingFluid(@Nullable Fluid solidifyingFluid) {
        this.solidifyingFluid = solidifyingFluid;
    }
}
