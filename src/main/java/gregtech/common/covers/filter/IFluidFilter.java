package gregtech.common.covers.filter;

import net.minecraftforge.fluids.FluidStack;

public interface IFluidFilter extends IFilter {

    MatchResult<FluidStack> match(FluidStack toMatch);

    boolean test(FluidStack toTest);

    default int getTransferLimit(FluidStack stack, int transferSize) {
        return 0;
    }

    void configureFilterTanks(int amount);

    default MatchResult<FluidStack> createResult(boolean matched, FluidStack fluidStack, int index) {
        return MatchResult.create(matched, fluidStack, index);
    }

    @Override
    default FilterType getType() {
        return FilterType.FLUID;
    }
}
