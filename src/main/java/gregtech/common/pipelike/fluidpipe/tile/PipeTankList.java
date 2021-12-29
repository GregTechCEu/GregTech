package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.util.GTLog;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.FluidTankPropertiesWrapper;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;

public class PipeTankList implements IFluidHandler, Iterable<FluidTank> {

    private final TileEntityFluidPipe pipe;
    private final FluidTank[] tanks;
    private IFluidTankProperties[] properties;
    private EnumFacing facing;

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

    protected FluidTank getFluidTank(int channel) {
        return tanks[channel];
    }

    protected FluidStack getFluidStack(int channel) {
        return tanks[channel].getFluid();
    }

    protected int findChannel(FluidStack stack) {
        if (stack == null || tanks == null)
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
        int channel = findChannel(resource);
        if (channel < 0)
            return 0;
        FluidStack stack = getFluidStack(channel);
        int ins = fillChannel(resource, doFill, channel);
        if (stack == null || stack.amount <= 0 && ins > 0 && pipe instanceof TileEntityFluidPipeTickable) {
            ((TileEntityFluidPipeTickable) pipe).notifyNewWave(facing, channel);
        }
        return ins;
    }

    protected int fillChannel(FluidStack resource, boolean doFill, int channel) {
        if (resource == null || resource.amount <= 0 || channel < 0 || channel >= tanks.length)
            return 0;
        FluidTank tank = tanks[channel];
        FluidStack stack = tank.getFluid();
        if (stack == null || stack.amount <= 0) {
            int max = Math.min(resource.amount, tank.getCapacity());
            resource.amount = max;
            if (doFill) {
                tank.setFluid(resource);
                // check and destroy pipe
                boolean isBurning = pipe.getNodeData().getMaxFluidTemperature() < resource.getFluid().getTemperature(resource);
                boolean isLeaking = !pipe.getNodeData().isGasProof() && resource.getFluid().isGaseous(resource);
                if (isBurning || isLeaking) {
                    pipe.destroyPipe(isBurning, isLeaking);
                }
            }
            return max;
        }
        int max = Math.min(resource.amount, tank.getCapacity() - stack.amount);
        if (doFill) {
            stack.amount += max;
        }
        return max;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    @Nullable
    public FluidStack drainChannel(int amount, boolean doDrain, int channel) {
        if (channel < 0 || channel >= tanks.length)
            return null;
        FluidStack current = getFluidStack(channel);
        if (current == null || current.amount <= 0)
            return null;
        if (amount >= current.amount) {
            if (doDrain)
                tanks[channel].setFluid(null);
            return current;
        }
        FluidStack drained = current.copy();
        drained.amount = amount;
        if (doDrain)
            current.amount -= amount;
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
}
