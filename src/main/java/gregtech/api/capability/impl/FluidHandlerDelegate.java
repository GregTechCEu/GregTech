package gregtech.api.capability.impl;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.Nullable;

public class FluidHandlerDelegate implements IFluidHandler {

    public final IFluidHandler delegate;

    public FluidHandlerDelegate(IFluidHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return delegate.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return delegate.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return delegate.drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return delegate.drain(maxDrain, doDrain);
    }
}
