package gregtech.api.recipes.lookup.flag;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        public int hashCode(@Nullable FluidStack o) {
            if (o == null) return 0;
            return o.getFluid().hashCode();
        }

        @Override
        public boolean equals(@Nullable FluidStack a, @Nullable FluidStack b) {
            if (a == b) return true;
            if (a == null ^ b == null) return false;
            return a.getFluid().equals(b.getFluid());
        }
    };

    public static final Strategy<FluidStack> FLUID_NBT = new Strategy<>() {

        @Override
        public int hashCode(@Nullable FluidStack o) {
            if (o == null) return 0;
            return 97 * o.getFluid().hashCode() + ItemStackApplicatorMap.hashNBT(o.tag);
        }

        @Override
        public boolean equals(@Nullable FluidStack a, @Nullable FluidStack b) {
            if (a == b) return true;
            if (a == null ^ b == null) return false;
            return a.getFluid().equals(b.getFluid()) && Objects.equals(a.tag, b.tag);
        }
    };
}
