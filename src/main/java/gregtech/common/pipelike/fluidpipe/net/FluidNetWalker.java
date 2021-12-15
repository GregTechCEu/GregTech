package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FluidNetWalker extends PipeNetWalker {

    public static Set<FluidStack> countFluid(World world, BlockPos pos) {
        FluidNetWalker walker = new FluidNetWalker(Mode.COUNT, world, pos, 1, null);
        walker.ignoreFilter = true;
        walker.fluids = new HashSet<>();
        walker.traversePipeNet();
        return walker.fluids;
    }

    public static FluidNetWalker getPipesForFluid(World world, BlockPos pos, FluidStack fluid, boolean countFluid) {
        FluidNetWalker walker = new FluidNetWalker(countFluid ? Mode.GET_PIPES_AND_COUNT : Mode.GET_PIPES, world, pos, 1, fluid);
        walker.traversePipeNet();
        return walker;
    }

    public static long getTotalCapacity(World world, BlockPos pos) {
        FluidNetWalker walker = new FluidNetWalker(Mode.COUNT_CAPACITY, world, pos, 1, null);
        walker.ignoreFilter = true;
        walker.traversePipeNet();
        return walker.count;
    }

    public static long getSpaceFor(World world, BlockPos pos, FluidStack stack, int min) {
        FluidNetWalker walker = new FluidNetWalker(Mode.SPACE, world, pos, 1, stack);
        walker.min = min;
        walker.traversePipeNet();
        return Math.min(walker.count, min);
    }

    private final Mode mode;
    private List<TileEntityFluidPipe> pipes = new ArrayList<>();
    private final FluidStack fluid;
    private Set<FluidStack> fluids;
    private long count;
    private int min;
    private boolean ignoreFilter = false;

    protected FluidNetWalker(Mode mode, World world, BlockPos sourcePipe, int walkedBlocks, FluidStack fluid) {
        super(world, sourcePipe, walkedBlocks);
        this.fluid = fluid;
        this.mode = mode;
    }

    @Override
    protected PipeNetWalker createSubWalker(World world, BlockPos nextPos, int walkedBlocks) {
        FluidNetWalker walker = new FluidNetWalker(mode, world, nextPos, walkedBlocks, fluid);
        walker.pipes = pipes;
        walker.ignoreFilter = ignoreFilter;
        walker.min = min;
        walker.fluids = fluids;
        return walker;
    }

    @Override
    protected void onRemoveSubWalker(PipeNetWalker subWalker) {
        count += ((FluidNetWalker) subWalker).count;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        TileEntityFluidPipe pipe = (TileEntityFluidPipe) pipeTile;
        switch (mode) {
            case SPACE: {
                FluidStack stack = pipe.getContainedFluid(pipe.findChannel(fluid));
                count += pipe.getCapacityPerTank();
                if (stack != null)
                    count -= stack.amount;
                if (count >= min)
                    stop();
                break;
            }
            case COUNT_CAPACITY:
                count += pipe.getCapacityPerTank();
                break;
            case GET_PIPES:
                pipes.add(pipe);
                break;
            case GET_PIPES_AND_COUNT:
                pipes.add(pipe);
                FluidStack stack = pipe.getContainedFluid(pipe.findChannel(fluid));
                if (stack != null)
                    count += stack.amount;
                break;
            default: { // Mode.COUNT
                countFluids(pipe);
                break;
            }
        }
    }

    private void countFluids(TileEntityFluidPipe pipe) {
        main:
        for (FluidTank tank : pipe.getFluidTanks()) {
            FluidStack stack = tank.getFluid();
            if (stack != null && stack.amount > 0) {
                for (FluidStack stack1 : fluids) {
                    if (stack1.isFluidEqual(stack)) {
                        stack1.amount += stack.amount;
                        continue main;
                    }
                }
                fluids.add(stack.copy());
            }
        }
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        if (!(neighbourPipe instanceof TileEntityFluidPipe) || ((TileEntityFluidPipe) neighbourPipe).isInvalid())
            return false;
        if (ignoreFilter)
            return true;
        ICoverable coverable = currentPipe.getCoverableImplementation();
        CoverBehavior cover = coverable.getCoverAtSide(faceToNeighbour);
        if (cover instanceof CoverFluidFilter) {
            if (!((CoverFluidFilter) cover).testFluidStack(fluid))
                return false;
        }
        coverable = neighbourPipe.getCoverableImplementation();
        cover = coverable.getCoverAtSide(faceToNeighbour.getOpposite());
        if (cover instanceof CoverFluidFilter) {
            return ((CoverFluidFilter) cover).testFluidStack(fluid);
        }
        return true;
    }

    public long getCount() {
        return count;
    }

    public List<TileEntityFluidPipe> getPipes() {
        return pipes;
    }

    public FluidStack getFluid() {
        return fluid;
    }

    enum Mode {
        COUNT, GET_PIPES, COUNT_CAPACITY, SPACE, GET_PIPES_AND_COUNT
    }
}
