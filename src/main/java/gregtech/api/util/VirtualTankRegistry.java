package gregtech.api.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

//todo save tanks to world data
public class VirtualTankRegistry {

    private static final int DEFAULT_CAPACITY = Integer.MAX_VALUE;

    protected static Map<String, IFluidTank> tankMap = new HashMap<>();

    public static IFluidTank getTank(String key) {
        return tankMap.get(key);
    }

    public static IFluidTank getTankCreate(String key, int capacity) {
        if (!tankMap.containsKey(key)) {
            addTank(key, capacity);
        }
        return getTank(key);
    }

    public static IFluidTank getTankCreate(String key) {
        return getTankCreate(key, DEFAULT_CAPACITY);
    }

    public static void addTank(String key, int capacity) {
        if(tankMap.containsKey(key)) {
            GTLog.logger.warn("Overwriting virtual tank " + key + ", this might cause fluid loss!");
        }
        tankMap.put(key, new VirtualTank(capacity));
    }

    public static void addTank(String key) {
        addTank(key, DEFAULT_CAPACITY);
    }

    private static class VirtualTank implements IFluidTank, IFluidHandler {

        @Nullable
        protected FluidStack fluid;
        protected int capacity;
        protected IFluidTankProperties[] tankProperties;

        public VirtualTank(int capacity) {
            this.capacity = capacity;
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            return this.fluid;
        }

        @Override
        public int getFluidAmount() {
            return this.fluid == null ? 0 : this.fluid.amount;
        }

        @Override
        public int getCapacity() {
            return this.capacity;
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(this);
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            if (this.tankProperties == null) {
                this.tankProperties = new IFluidTankProperties[]{ new VirtualTankProperties(this) };
            }
            return this.tankProperties;
        }

        @Override
        public int fill(FluidStack fluidStack, boolean doFill) {
            //GTLog.logger.info("fluid fill " + fluidStack.getLocalizedName() + "/" + fluidStack.amount);
            if (fluidStack == null || fluidStack.amount <= 0 || (this.fluid != null && !fluidStack.isFluidEqual(this.fluid)))
                return 0;

            int fillAmt = Math.min(fluidStack.amount, this.capacity - this.getFluidAmount());
            if (doFill) {
                if (this.fluid == null) {
                    this.fluid = new FluidStack(fluidStack, fillAmt);
                } else {
                    this.fluid.amount += fillAmt;
                }
            }
            return fillAmt;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return resource == null || !resource.isFluidEqual(this.fluid) ? null : drain(resource.amount, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int amount, boolean doDrain) {
            if (this.fluid == null || amount <= 0)
                return null;

            int drainAmt = Math.min(this.getFluidAmount(), amount);
            FluidStack drainedFluid = new FluidStack(fluid, drainAmt);
            if (doDrain) {
                this.fluid.amount -= drainAmt;
                if (this.fluid.amount <= 0) {
                    this.fluid = null;
                }
            }
            return drainedFluid;
        }

        private class VirtualTankProperties implements IFluidTankProperties {

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
}
