package gregtech.api.unification.material.properties;

import gregtech.api.fluids.store.FluidStorage;
import org.jetbrains.annotations.NotNull;

public class FluidProperty implements IMaterialProperty {

    private final FluidStorage storage = new FluidStorage();

    public FluidProperty() {}

    //TODO merge with FluidProp directly
    public @NotNull FluidStorage getStorage() {
        return this.storage;
    }

    @Deprecated
    @Override
    public void verifyProperty(MaterialProperties properties) {}
}
