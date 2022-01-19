package gregtech.api.fluids.fluidType;

import gregtech.api.unification.material.Material;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class FluidType {

    private static final Map<String, FluidType> FLUID_TYPES = new HashMap<>();

    private final String name;
    private final String prefix;
    private final String suffix;
    private final String localization;

    public FluidType(@Nonnull String name, @Nonnull String prefix, @Nonnull String suffix, @Nonnull String localization) {
        if (FLUID_TYPES.get(name) != null)
            throw new IllegalArgumentException("Cannot register FluidType with duplicate name: " + name);

        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.localization = localization;
        FLUID_TYPES.put(name, this);
    }

    public String getNameForMaterial(@Nonnull Material material) {
        return this.prefix + "." + material.toString() + "." + this.suffix;
    }

    public static void setFluidProperties(@Nonnull FluidType fluidType, @Nonnull Fluid fluid) {
        fluidType.setFluidProperties(fluid);
    }

    protected abstract void setFluidProperties(@Nonnull Fluid fluid);

    public String getLocalization() {
        return this.localization;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public static FluidType getByName(@Nonnull String name) {
        return FLUID_TYPES.get(name);
    }
}
