package gregtech.api.recipes.map;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public class MapFluidIngredient extends AbstractMapIngredient {

    public final Fluid fluid;
    public final NBTTagCompound tag;

    public MapFluidIngredient(GTRecipeInput fluidInput) {
        FluidStack fluidStack = fluidInput.getInputFluidStack();
        this.fluid = fluidStack.getFluid();
        this.tag = fluidStack.tag;
    }

    public MapFluidIngredient(FluidStack fluidStack) {
        this.fluid = fluidStack.getFluid();
        this.tag = fluidStack.tag;
    }

    @Override
    protected int hash() {
        int hash = 31 + fluid.hashCode();
        if (tag != null) {
            return 31 * hash + tag.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapFluidIngredient other = (MapFluidIngredient) o;
            return fluid == other.fluid && Objects.equals(tag, other.tag);
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapFluidIngredient{" +
                "{fluid=" + fluid.getName() + "} {tag=" + tag + "}";
    }
}
