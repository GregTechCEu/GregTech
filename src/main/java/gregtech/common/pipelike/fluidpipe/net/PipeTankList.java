package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.util.GTLog;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.FluidTankPropertiesWrapper;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class PipeTankList implements IFluidHandler {

    private final EnumFacing facing;
    private final TileEntityFluidPipe pipe;
    private final FluidTank[] tanks;
    private IFluidTankProperties[] properties;

    public PipeTankList(TileEntityFluidPipe pipe, EnumFacing facing, FluidTank... fluidTanks) {
        this.tanks = fluidTanks;
        this.facing = facing;
        this.pipe = pipe;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (properties == null) {
            properties = new IFluidTankProperties[tanks.length];
            for (int i = 0; i < tanks.length; i++) {
                properties[i] = new FluidTankPropertiesWrapper(tanks[i]);
            }
        }
        return properties;
    }

    private int findChannel(FluidStack stack) {
        if (stack == null)
            return -1;
        int empty = -1;
        for (int i = tanks.length - 1; i >= 0; i--) {
            FluidStack f = tanks[i].getFluid();
            if (f == null)
                empty = i;
            else if (f.isFluidEqual(stack))
                return i;
        }
        return empty;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0)
            return 0;
        boolean wasEmpty = doFill && pipe.getContainedFluid(pipe.findChannel(resource)) == null;
        int filled = fillInternal(resource, doFill);
        if (filled > 0)
            log("Filled {} * {} into multipipe, sim {}", resource.getLocalizedName(), filled, !doFill);
        if (filled > 0 && doFill) {
            FluidStack stack = resource.copy();
            stack.amount = filled;
            pipe.didInsertFrom(facing);
            pipe.getFluidPipeNet().fill(stack, pipe.getPos());
            if (wasEmpty) {
                pipe.checkAndDestroy(resource);
            }
        }
        return filled;
    }

    private int fillInternal(FluidStack stack, boolean doFill) {
        int channel = findChannel(stack);
        if (channel < 0)
            return 0;
        FluidTank tank = tanks[channel];
        FluidStack current = tank.getFluid();
        FluidStack copy = stack.copy();
        if (current == null) {
            copy.amount = Math.min(stack.amount, tank.getCapacity());
            if (doFill)
                tank.setFluid(copy);
            return copy.amount;
        }
        int filled = Math.min(stack.amount, tank.getCapacity() - current.amount);
        if (doFill)
            current.amount += filled;
        return filled;
    }

    private void log(String s, Object... o) {
        if (getTankProperties().length > 1)
            GTLog.logger.info(s, o);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        GTLog.logger.warn("Don't use drain(int, bool) on Fluid pipes");
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0)
            return null;
        int channel = findChannel(resource);
        if (channel < 0)
            return null;
        FluidStack current = tanks[channel].getFluid();
        if (current == null)
            return null;
        FluidStack drained = current.copy();
        drained.amount = Math.min(current.amount, resource.amount);
        if (doDrain) {
            current.amount -= drained.amount;
            if (current.amount <= 0)
                tanks[channel].setFluid(null);
            pipe.getFluidPipeNet().drain(drained, pipe.getPos(), false);
        }
        return drained;
    }
}
