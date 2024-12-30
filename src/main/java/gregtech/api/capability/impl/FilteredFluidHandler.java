package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilteredFluidHandler extends FluidTank implements IFilteredFluidContainer {

    @Nullable
    private IFilter<FluidStack> filter;

    public FilteredFluidHandler(int capacity) {
        super(capacity);
    }

    public FilteredFluidHandler(@Nullable FluidStack fluidStack, int capacity) {
        super(fluidStack, capacity);
    }

    public FilteredFluidHandler(Fluid fluid, int amount, int capacity) {
        super(fluid, amount, capacity);
    }

    @Nullable
    @Override
    public IFilter<FluidStack> getFilter() {
        return this.filter;
    }

    /**
     * Set filter instance. If {@code null} is given, then the filter is set to be
     *
     * @param filter new filter instance
     * @return this
     */
    @NotNull
    public FilteredFluidHandler setFilter(@Nullable IFilter<FluidStack> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return canFill() && (this.filter == null || this.filter.test(fluid));
    }
}
