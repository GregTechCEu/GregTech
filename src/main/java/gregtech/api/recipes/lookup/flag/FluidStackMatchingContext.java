package gregtech.api.recipes.lookup.flag;

import net.minecraftforge.fluids.FluidStack;

public enum FluidStackMatchingContext {

    FLUID,
    FLUID_NBT;

    public static final FluidStackMatchingContext[] VALUES = values();

    public boolean matchesNBT() {
        return this == FLUID_NBT;
    }

    public boolean matches(FluidStack a, FluidStack b) {
        return switch (this) {
            case FLUID -> FluidStackApplicatorMap.FLUID.equals(a, b);
            case FLUID_NBT -> FluidStackApplicatorMap.FLUID_NBT.equals(a, b);
        };
    }
}
