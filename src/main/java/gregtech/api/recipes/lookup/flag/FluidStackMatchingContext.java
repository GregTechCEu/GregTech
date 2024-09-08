package gregtech.api.recipes.lookup.flag;

public enum FluidStackMatchingContext {
    FLUID, FLUID_NBT;

    public boolean matchesNBT() {
        return this == FLUID_NBT;
    }
}
