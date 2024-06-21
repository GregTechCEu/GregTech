package gregtech.api.util.virtualregistry.entries;

import gregtech.api.util.virtualregistry.EntryTypes;
import gregtech.api.util.virtualregistry.VirtualEntry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.Nullable;

public class VirtualTank extends VirtualEntry implements IFluidTank, IFluidHandler {

    protected static final String CAPACITY_KEY = "capacity";
    protected static final String FLUID_KEY = "fluid";
    private final IFluidTankProperties[] props = new IFluidTankProperties[] {
            new VirtualTankProperties(this)
    };

    @Override
    public EntryTypes<VirtualTank> getType() {
        return EntryTypes.ENDER_FLUID;
    }

    public void setCapacity(int capacity) {
        getData().setInteger(CAPACITY_KEY, capacity);
    }

    @Override
    public FluidStack getFluid() {
        return !getData().hasKey(FLUID_KEY) ? null :
                FluidStack.loadFluidStackFromNBT(getData().getCompoundTag(FLUID_KEY));
    }

    public void setFluid(FluidStack fluid) {
        if (fluid == null)
            getData().removeTag(FLUID_KEY);
        else
            getData().setTag(FLUID_KEY, fluid.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public int getFluidAmount() {
        return !getData().hasKey(FLUID_KEY) ? 0 : getFluid().amount;
    }

    @Override
    public int getCapacity() {
        return getData().getInteger(CAPACITY_KEY);
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return this.props;
    }

    @Override
    public int fill(FluidStack fluidStack, boolean doFill) {
        var fluid = getFluid();
        if (fluidStack == null || fluidStack.amount <= 0 ||
                (fluid != null && !fluidStack.isFluidEqual(fluid)))
            return 0;

        int fillAmt = Math.min(fluidStack.amount, getCapacity() - this.getFluidAmount());
        if (doFill) {
            if (fluid == null) {
                fluid = new FluidStack(fluidStack, fillAmt);
            } else {
                fluid.amount += fillAmt;
            }
            setFluid(fluid);
        }
        return fillAmt;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        var fluid = getFluid();
        return resource == null || !resource.isFluidEqual(fluid) ? null : drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int amount, boolean doDrain) {
        var fluid = getFluid();
        if (fluid == null || amount <= 0)
            return null;

        int drainAmt = Math.min(this.getFluidAmount(), amount);
        FluidStack drainedFluid = new FluidStack(fluid, drainAmt);
        if (doDrain) {
            fluid.amount -= drainAmt;
            if (fluid.amount <= 0) {
                fluid = null;
            }
            setFluid(fluid);
        }
        return drainedFluid;
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
