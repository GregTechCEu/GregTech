package gregtech.api.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

// probably causes problems
public class FluidTankSwitchShim implements IFluidTank, IFluidHandler {

    IFluidTank tank;

    public FluidTankSwitchShim(IFluidTank tank) {
        changeTank(tank);
    }

    public void changeTank(IFluidTank tank) {
        if (!(tank instanceof IFluidHandler)) {
            throw new IllegalArgumentException("Shim tank must be both IFluidTank and IFluidHandler!");
        }
        this.tank = tank;
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        return tank.getFluid();
    }

    @Override
    public int getFluidAmount() {
        return tank.getFluidAmount();
    }

    @Override
    public int getCapacity() {
        return tank.getCapacity();
    }

    @Override
    public FluidTankInfo getInfo() {
        return tank.getInfo();
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return ((IFluidHandler) tank).getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return ((IFluidHandler) tank).fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return ((IFluidHandler) tank).drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return tank.drain(maxDrain, doDrain);
    }
}
