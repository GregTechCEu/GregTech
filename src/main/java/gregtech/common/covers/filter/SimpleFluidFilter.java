package gregtech.common.covers.filter;

import gregtech.common.covers.filter.readers.SimpleFluidFilterReader;

import net.minecraftforge.fluids.FluidStack;

public class SimpleFluidFilter extends BaseFilter {

    private static final int MAX_FLUID_SLOTS = 9;

    private final SimpleFluidFilterReader filterReader = new SimpleFluidFilterReader(MAX_FLUID_SLOTS);

    @Override
    public SimpleFluidFilterReader getFilterReader() {
        return filterReader;
    }

    public void configureFilterTanks(int amount) {
        this.filterReader.setFluidAmounts(amount);
    }

    @Override
    public MatchResult matchFluid(FluidStack fluidStack) {
        int index = -1;
        FluidStack returnable = null;
        for (int i = 0; i < filterReader.getSize(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                index = i;
                returnable = fluid.copy();
                break;
            }
        }
        return MatchResult.create(index != -1, returnable, index);
    }

    @Override
    public boolean testFluid(FluidStack toTest) {
        for (int i = 0; i < filterReader.getSize(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(toTest)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getTransferLimit(FluidStack fluidStack, int transferSize) {
        int limit = 0;

        for (int i = 0; i < this.filterReader.getSize(); i++) {
            var fluid = this.filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                limit = fluid.amount;
            }
        }
        return isBlacklistFilter() ? transferSize : limit;
    }

    @Override
    public FilterType getType() {
        return FilterType.FLUID;
    }
}
