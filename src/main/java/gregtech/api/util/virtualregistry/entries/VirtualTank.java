package gregtech.api.util.virtualregistry.entries;

import gregtech.api.util.virtualregistry.EntryType;
import gregtech.api.util.virtualregistry.VirtualEntry;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.Nullable;

public class VirtualTank extends VirtualEntry implements IFluidTank, IFluidHandler {

    protected static final String CAPACITY_KEY = "capacity";

    @Override
    public EntryType getType() {
        return EntryType.ENDER_FLUID;
    }

    public void setCapacity(int capacity) {
        getData().setInteger(CAPACITY_KEY, capacity);
    }

    @Override
    public FluidStack getFluid() {
        return null;
    }

    @Override
    public int getFluidAmount() {
        return 0;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public FluidTankInfo getInfo() {
        return null;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[0];
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    private static class VirtualTankProperties implements IFluidTankProperties {

        protected final VirtualTank tank;

        private VirtualTankProperties(VirtualTank tank) {
            this.tank = tank;
        }

        @Nullable
        @Override
        public FluidStack getContents() {
            FluidStack contents = tank.getFluid();
            return contents == null ? null : contents.copy();
        }

        @Override
        public int getCapacity() {
            return tank.getCapacity();
        }

        @Override
        public boolean canFill() {
            return true;
        }

        @Override
        public boolean canDrain() {
            return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return true;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return true;
        }
    }
}
