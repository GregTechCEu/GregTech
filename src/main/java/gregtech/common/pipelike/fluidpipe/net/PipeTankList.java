package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;

public class PipeTankList extends FluidTankList {

    private final EnumFacing facing;
    private final TileEntityFluidPipe pipe;

    public PipeTankList(TileEntityFluidPipe pipe, EnumFacing facing, IFluidTank... fluidTanks) {
        super(false, fluidTanks);
        this.facing = facing;
        this.pipe = pipe;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        boolean wasEmpty = pipe.getContainedFluid(pipe.findChannel(resource)) == null;
        int filled = super.fill(resource, doFill);
        if(filled > 0 && doFill) {
            FluidStack stack = resource.copy();
            stack.amount = filled;
            pipe.didInsertFrom(facing);
            pipe.getFluidPipeNet().fill(stack, pipe.getPos());
            if(wasEmpty) {
                pipe.checkAndDestroy(resource);
            }
        }
        return filled;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack drained = super.drain(maxDrain, doDrain);
        if(drained != null) {
            pipe.getFluidPipeNet().drain(drained, pipe.getPos());
        }
        return drained;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        FluidStack drained = super.drain(resource, doDrain);
        if(drained != null) {
            pipe.getFluidPipeNet().drain(drained, pipe.getPos());
        }
        return drained;
    }
}
