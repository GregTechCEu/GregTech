package gregtech.api.recipes.map;

import net.minecraftforge.fluids.FluidStack;

public class MapFluidIngredient extends AbstractMapIngredient {

    public FluidStack stack;

    public MapFluidIngredient(FluidStack stack, boolean insideMap) {
        super(insideMap);
        this.stack = stack;
    }

    @Override
    protected int hash() {
        return stack.getFluid().getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (o instanceof MapFluidIngredient) {
            MapFluidIngredient fluid = (MapFluidIngredient) o;
            return this.hashCode() == fluid.hashCode() && stack.getFluid() == fluid.stack.getFluid(); //&& stack.isFluidEqual(fluid.stack);
        }
        /*if (o instanceof MapTagIngredient) {
            MapTagIngredient tag = (MapTagIngredient) o;
            return stack.getFluid().getTags().contains(tag.loc);
        }*/
        return false;
    }

    @Override
    public String toString() {
        return stack.getFluid().getName();
    }
}
