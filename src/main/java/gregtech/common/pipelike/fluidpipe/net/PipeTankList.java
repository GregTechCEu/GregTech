package gregtech.common.pipelike.fluidpipe.net;

import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

public class PipeTankList implements IFluidHandler, Iterable<FluidTank> {

    private final TileEntityFluidPipe pipe;
    private final FluidTank[] tanks;
    private IFluidTankProperties[] properties;
    private final FluidStack[] queued;

    public PipeTankList(TileEntityFluidPipe pipe, FluidTank... fluidTanks) {
        this.tanks = fluidTanks;
        this.pipe = pipe;
        this.queued = new FluidStack[fluidTanks.length];
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (properties == null) {
            properties = new IFluidTankProperties[tanks.length];
            for (int i = 0; i < tanks.length; i++) {
                final int tankIndex = i;
                properties[tankIndex] = new PipeTankProperties(tanks[tankIndex], () -> queued[tankIndex]);
            }
        }
        return properties;
    }

    private int findChannel(FluidStack stack) {
        if (stack == null)
            return -1;
        int empty = -1;
        for (int i = tanks.length - 1; i >= 0; i--) {
            FluidStack f = getTankProperties()[i].getContents();
            if (f == null)
                empty = i;
            else if (f.isFluidEqual(stack))
                return i;
        }
        return empty;
    }

    public int setContainingFluid(FluidStack stack, int channel, boolean fill) {
        if (channel < 0)
            return stack == null ? 0 : stack.amount;
        if (stack == null || stack.amount <= 0) {
            tanks[channel].setFluid(null);
            return 0;
        }
        this.queued[channel] = null;
        FluidTank tank = tanks[channel];
        FluidStack currentStack = tank.getFluid();
        if (currentStack == null || currentStack.amount <= 0) {
            pipe.checkAndDestroy(stack);
        } else if (fill) {
            int toFill = stack.amount;
            if (toFill + currentStack.amount > tank.getCapacity())
                toFill = tank.getCapacity() - currentStack.amount;
            currentStack.amount += toFill;
            return toFill;
        }
        stack.amount = Math.min(stack.amount, tank.getCapacity());
        tank.setFluid(stack);
        return stack.amount;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int channel;
        if (resource == null || resource.amount <= 0 || (channel = findChannel(resource)) < 0)
            return 0;
        IFluidTankProperties properties = getTankProperties()[channel];
        FluidStack currentFluid = properties.getContents();
        int space = properties.getCapacity() - (currentFluid == null ? 0 : currentFluid.amount);
        FluidStack copy = resource.copy();
        if (resource.amount <= space) {
            copy.amount = resource.amount;
        } else if (space < properties.getCapacity() / 2) {
            space = (int) FluidNetWalker.getSpaceFor(pipe.getWorld(), pipe.getPos(), resource, resource.amount);
            if (space <= 0)
                return 0;
            copy.amount = space;
        } else {
            copy.amount = space;
        }
        int filled = pipe.getFluidPipeNet().fill(copy, pipe.getPos(), doFill);
        if (doFill) {
            FluidStack queuedFluid = queued[channel];
            if (queuedFluid == null || !queuedFluid.isFluidEqual(resource)) {
                queuedFluid = resource.copy();
                queuedFluid.amount = filled;
            } else {
                queuedFluid.amount = Math.min(properties.getCapacity(), queuedFluid.amount + filled);
            }
            queued[channel] = queuedFluid;
        }
        return filled;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    @Nullable
    public FluidStack drainInternal(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0)
            return null;
        FluidStack drained = resource.copy();
        drained.amount = pipe.getFluidPipeNet().drain(resource, pipe.getPos(), false, doDrain);
        return drained;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluidStack, boolean b) {
        return null;
    }

    @Override
    @Nonnull
    public Iterator<FluidTank> iterator() {
        return Arrays.stream(tanks).iterator();
    }

    private static class PipeTankProperties implements IFluidTankProperties {

        private final FluidTank tank;
        private final Supplier<FluidStack> queued;

        private PipeTankProperties(FluidTank tank, Supplier<FluidStack> queued) {
            this.tank = tank;
            this.queued = queued;
        }

        @Nullable
        @Override
        public FluidStack getContents() {
            FluidStack fluid = tank.getFluid();
            FluidStack queuedFluid = queued.get();
            if (fluid == null) {
                return queuedFluid;
            }
            if (queuedFluid == null || !fluid.isFluidEqual(queuedFluid)) {
                return fluid;
            }
            fluid = fluid.copy();
            fluid.amount += queuedFluid.amount;
            return fluid;
        }

        @Override
        public int getCapacity() {
            return tank.getCapacity();
        }

        @Override
        public boolean canFill() {
            return tank.canFill();
        }

        @Override
        public boolean canDrain() {
            return tank.canDrain();
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return tank.canFillFluidType(fluidStack);
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return tank.canDrainFluidType(fluidStack);
        }
    }
}
