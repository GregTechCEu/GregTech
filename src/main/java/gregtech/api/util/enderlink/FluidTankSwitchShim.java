package gregtech.api.util.enderlink;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

// probably causes problems
public class FluidTankSwitchShim extends SwitchShimBase<IFluidTank> implements IFluidTank, IFluidHandler {

    public FluidTankSwitchShim(IFluidTank tank) {
        super(tank);
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        return container.getFluid();
    }

    @Override
    public int getFluidAmount() {
        return container.getFluidAmount();
    }

    @Override
    public int getCapacity() {
        return container.getCapacity();
    }

    @Override
    public FluidTankInfo getInfo() {
        return container.getInfo();
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return ((IFluidHandler) container).getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return ((IFluidHandler) container).fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return ((IFluidHandler) container).drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return container.drain(maxDrain, doDrain);
    }
}
