package gregtech.api.pipenet.predicate;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public final class FluidTestObject implements IPredicateTestObject {

    public final Fluid fluid;
    public final NBTTagCompound tag;

    public FluidTestObject(FluidStack stack) {
        this.fluid = stack.getFluid();
        this.tag = stack.tag;
    }

    public FluidStack recombine() {
        return new FluidStack(fluid, 1, tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FluidTestObject that = (FluidTestObject) o;
        return Objects.equals(fluid, that.fluid) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, tag);
    }
}
