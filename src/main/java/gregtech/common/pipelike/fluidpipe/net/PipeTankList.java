package gregtech.common.pipelike.fluidpipe.net;

import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
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

    private final TileEntityFluidPipeTickable pipe;
    private final FluidTank[] tanks;
    private IFluidTankProperties[] properties;
    private final EnumFacing facing;

    public PipeTankList(TileEntityFluidPipe pipe, EnumFacing facing, FluidTank... fluidTanks) {
        this.tanks = fluidTanks;
        this.pipe = (TileEntityFluidPipeTickable) pipe;
        this.facing = facing;
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
        int channel;
        if (pipe.isFaceBlocked(facing) || resource == null || resource.amount <= 0 || (channel = findChannel(resource)) < 0)
            return 0;

        return fill(resource, doFill, channel);
    }

    private int fullCapacity() {
        return tanks.length * pipe.getCapacityPerTank();
    }

    private int fill(FluidStack resource, boolean doFill, int channel) {
        if (channel >= tanks.length) return 0;
        FluidTank tank = tanks[channel];
        FluidStack currentFluid = tank.getFluid();

        if (currentFluid == null || currentFluid.amount <= 0) {
            FluidStack newFluid = resource.copy();
            newFluid.amount = Math.min(pipe.getCapacityPerTank(), newFluid.amount);
            if (doFill) {
                tank.setFluid(newFluid);
                pipe.receivedFrom(facing);
                pipe.checkAndDestroy(newFluid);
            }
            return newFluid.amount;
        }
        if (currentFluid.isFluidEqual(resource)) {
            int toAdd = Math.min(tank.getCapacity() - currentFluid.amount, resource.amount);
            if (toAdd > 0) {
                if (doFill) {
                    currentFluid.amount += toAdd;
                    pipe.receivedFrom(facing);
                    pipe.checkAndDestroy(currentFluid);
                }
                return toAdd;
            }
        }

        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) return null;
        for (FluidTank tank : tanks) {
            FluidStack drained = tank.drain(maxDrain, doDrain);
            if (drained != null) return drained;
        }
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluidStack, boolean doDrain) {
        if (fluidStack == null || fluidStack.amount <= 0) return null;
        fluidStack = fluidStack.copy();
        for (FluidTank tank : tanks) {
            FluidStack drained = tank.drain(fluidStack, doDrain);
            if (drained != null) return drained;
        }
        return null;
    }

    @Override
    @Nonnull
    public Iterator<FluidTank> iterator() {
        return Arrays.stream(tanks).iterator();
    }
}
