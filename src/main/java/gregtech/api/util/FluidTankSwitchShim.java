package gregtech.api.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.Nullable;

// probably causes problems
public class FluidTankSwitchShim implements IFluidTank, IFluidHandler {

    @Nullable
    private IFluidTank tank;
    private static final FluidTankInfo NO_INFO = new FluidTankInfo(null, 0);
    private static final IFluidTankProperties[] NO_PROPS = new IFluidTankProperties[0];

    public FluidTankSwitchShim(IFluidTank tank) {
        changeTank(tank);
    }

    public void changeTank(IFluidTank tank) {
        if (tank != null && !(tank instanceof IFluidHandler)) {
            throw new IllegalArgumentException("Shim tank must be both IFluidTank and IFluidHandler!");
        }
        this.tank = tank;
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        return tank == null ? null : tank.getFluid();
    }

    @Override
    public int getFluidAmount() {
        return tank == null ? 0 : tank.getFluidAmount();
    }

    @Override
    public int getCapacity() {
        return tank == null ? 0 : tank.getCapacity();
    }

    @Override
    public FluidTankInfo getInfo() {
        return tank == null ? NO_INFO : tank.getInfo();
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (tank == null)
            return NO_PROPS;

        return ((IFluidHandler) tank).getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (tank == null) return 0;
        return ((IFluidHandler) tank).fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (tank == null) return null;
        return ((IFluidHandler) tank).drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (tank == null) return null;
        return tank.drain(maxDrain, doDrain);
    }
}
