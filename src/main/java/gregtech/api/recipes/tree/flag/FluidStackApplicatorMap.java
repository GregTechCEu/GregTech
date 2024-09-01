package gregtech.api.recipes.tree.flag;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FluidStackApplicatorMap extends Object2ApplicatorMapMap<FluidStack> {

    @Contract(" -> new")
    public static @NotNull FluidStackApplicatorMap fluid() {
        return new FluidStackApplicatorMap(FLUID);
    }

    @Contract(" -> new")
    public static @NotNull FluidStackApplicatorMap fluidNBT() {
        return new FluidStackApplicatorMap(FLUID_NBT);
    }

    public FluidStackApplicatorMap(Strategy<FluidStack> strategy) {
        super(strategy);
    }

    public static final Strategy<FluidStack> FLUID = new Strategy<>() {

        @Override
        public int hashCode(FluidStack o) {
            return o.getFluid().hashCode();
        }

        @Override
        public boolean equals(FluidStack a, FluidStack b) {
            if (a == null || b == null) return false;
            return a.getFluid().equals(b.getFluid());
        }
    };

    public static final Strategy<FluidStack> FLUID_NBT = new Strategy<>() {

        @Override
        public int hashCode(FluidStack o) {
            return 97 * o.getFluid().hashCode() + ItemStackApplicatorMap.hashNBT(o.tag);
        }

        @Override
        public boolean equals(FluidStack a, FluidStack b) {
            if (a == null || b == null) return false;
            return a.getFluid().equals(b.getFluid()) && Objects.equals(a.tag, b.tag);
        }
    };
}
