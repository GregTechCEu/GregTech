package gregtech.common.pipelike.fluidpipe.net;

import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
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

    public PipeTankList(TileEntityFluidPipe pipe, FluidTank... fluidTanks) {
        this.tanks = fluidTanks;
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
        int channel;
        if (resource == null || resource.amount <= 0 || (channel = findChannel(resource)) < 0)
            return 0;
        FluidTank tank = tanks[channel];
        int space = tank.getCapacity() - (tank.getFluid() == null ? 0 : tank.getFluid().amount);
        FluidStack copy = resource.copy();
        if (resource.amount <= space) {
            copy.amount = resource.amount;
        } else if (space < tank.getCapacity() / 2) {
            space = (int) FluidNetWalker.getSpaceFor(pipe.getWorld(), pipe.getPos(), resource, resource.amount);
            if (space <= 0)
                return 0;
            copy.amount = space;
        } else {
            copy.amount = space;
        }
        return pipe.getFluidPipeNet().fill(copy, pipe.getPos(), doFill);
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
}
