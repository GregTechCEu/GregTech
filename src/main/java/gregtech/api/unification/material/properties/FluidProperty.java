package gregtech.api.unification.material.properties;

import gregtech.api.fluids.store.FluidStorage;
import gregtech.api.fluids.store.FluidStorageKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidProperty implements IMaterialProperty {

    private final FluidStorage storage = new FluidStorage();
    private @Nullable FluidStorageKey primaryKey = null;

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
}
