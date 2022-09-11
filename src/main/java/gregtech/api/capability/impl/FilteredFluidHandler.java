package gregtech.api.capability.impl;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.jetbrains.annotations.Nullable;
import java.util.function.Predicate;

public class FilteredFluidHandler extends FluidTank {

    private Predicate<FluidStack> fillPredicate;

    public FilteredFluidHandler(int capacity) {
        super(capacity);
    }

    public FilteredFluidHandler(@Nullable FluidStack fluidStack, int capacity) {
        super(fluidStack, capacity);
    }

    public FilteredFluidHandler(Fluid fluid, int amount, int capacity) {
        super(fluid, amount, capacity);
    }

    public FilteredFluidHandler setFillPredicate(Predicate<FluidStack> predicate) {
        this.fillPredicate = predicate;
        return this;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return canFill() && (fillPredicate == null || fillPredicate.test(fluid));
    }
}
