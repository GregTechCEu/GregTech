package gregtech.common.covers.filter.fluid;

import gregtech.api.cover.filter.Filter;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.BooleanSupplier;

public abstract class FluidFilter extends Filter<FluidStack> {

    private BooleanSupplier shouldShowTip = () -> false;
    private int maxSize = 1000;

    public abstract void configureFilterTanks(int amount);

    public abstract void setMaxConfigurableFluidSize(int maxSize);

    public abstract int getFluidTransferLimit(FluidStack fluidStack);

    @Override
    public int getTransferLimit(Object object, int globalTransferLimit) {
        return getFluidTransferLimit((FluidStack) object);
    }

    public void setShouldShowTip(BooleanSupplier shouldShowTip) {
        this.shouldShowTip = shouldShowTip != null ? shouldShowTip : () -> false;
    }

    public boolean shouldShowTip() {
        return this.shouldShowTip.getAsBoolean();
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
