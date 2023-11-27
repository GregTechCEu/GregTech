package gregtech.api.capability.impl;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class FluidHandlerProxy implements IFluidHandler {

    public IFluidHandler input;
    public IFluidHandler output;
    private IFluidTankProperties[] properties;

    public FluidHandlerProxy(IFluidHandler input, IFluidHandler output) {
        reinitializeHandler(input, output);
    }

    public void reinitializeHandler(IFluidHandler input, IFluidHandler output) {
        this.input = input;
        this.output = output;

        List<IFluidTankProperties> tanks = Lists.newArrayList();
        Collections.addAll(tanks, input.getTankProperties());
        Collections.addAll(tanks, output.getTankProperties());
        this.properties = tanks.toArray(new IFluidTankProperties[0]);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return properties;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return input.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return output.drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return output.drain(maxDrain, doDrain);
    }
}
