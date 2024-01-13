package gregtech.api.unification.material.properties;

import gregtech.api.fluids.store.FluidStorage;
import gregtech.api.fluids.store.FluidStorageKey;

import gregtech.api.fluids.store.FluidStorageKeys;

import gregtech.api.unification.material.Material;

import net.minecraftforge.fluids.Fluid;

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

    public Fluid solidifiesFrom(Material material) {
        if (this.solidifyingFluid == null) {
            return material.getFluid(FluidStorageKeys.LIQUID);
        }
        return solidifyingFluid;
    }

    public void setSolidifyingFluid(@Nullable Fluid solidifyingFluid) {
        this.solidifyingFluid = solidifyingFluid;
    }
}
