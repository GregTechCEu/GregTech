package gregtech.api.recipes.lookup.flag;

import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.Hash;

public enum FluidStackMatchingContext implements Hash.Strategy<FluidStack> {

    FLUID,
    FLUID_NBT;

    public static final FluidStackMatchingContext[] VALUES = values();

    public boolean matchesNBT() {
        return this == FLUID_NBT;
    }

    @Override
    public boolean equals(FluidStack a, FluidStack b) {
        return switch (this) {
            case FLUID -> FluidStackApplicatorMap.FLUID.equals(a, b);
            case FLUID_NBT -> FluidStackApplicatorMap.FLUID_NBT.equals(a, b);
        };
    }

    @Override
    public int hashCode(FluidStack o) {
        return switch (this) {
            case FLUID -> FluidStackApplicatorMap.FLUID.hashCode(o);
            case FLUID_NBT -> FluidStackApplicatorMap.FLUID_NBT.hashCode(o);
        };
    }
}
