package gregtech.api.recipes.map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public class MapFluidIngredient extends AbstractMapIngredient {

    public final Fluid fluid;
    public final NBTTagCompound tag;

    public MapFluidIngredient(FluidStack stack, boolean insideMap) {
        super(insideMap);
        this.fluid = stack.getFluid();
        this.tag = stack.tag;
    }

    @Override
    protected int hash() {
        int hash = fluid.hashCode();
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
                "fluid=" + fluid.getName() +
                ", tag=" + tag +
                '}';
    }
}
