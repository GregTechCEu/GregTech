package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GTLog;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FluidNetWalker extends PipeNetWalker {

    public static List<TileEntityFluidPipe> getPipesForFluid(World world, BlockPos pos, FluidStack fluid) {
        FluidNetWalker walker = new FluidNetWalker(world, pos, 1, fluid);
        walker.traversePipeNet();
        return walker.pipes;
    }

    private List<TileEntityFluidPipe> pipes = new ArrayList<>();
    private final FluidStack fluid;

    protected FluidNetWalker(World world, BlockPos sourcePipe, int walkedBlocks, FluidStack fluid) {
        super(world, sourcePipe, walkedBlocks);
        this.fluid = fluid;
    }

    @Override
    protected PipeNetWalker createSubWalker(World world, BlockPos nextPos, int walkedBlocks) {
        FluidNetWalker walker = new FluidNetWalker(world, nextPos, walkedBlocks, fluid);
        walker.pipes = pipes;
        return walker;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        pipes.add((TileEntityFluidPipe) pipeTile);
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        if(!(neighbourPipe instanceof TileEntityFluidPipe))
            return false;
        GTLog.logger.info("Checking covers");
        ICoverable coverable = currentPipe.getCoverableImplementation();
        CoverBehavior cover = coverable.getCoverAtSide(faceToNeighbour);
        if(cover instanceof CoverFluidFilter) {
            GTLog.logger.info(" - filter found on self");
            if(!((CoverFluidFilter) cover).testFluidStack(fluid))
                return false;
        }
        coverable = neighbourPipe.getCoverableImplementation();;
        cover = coverable.getCoverAtSide(faceToNeighbour.getOpposite());
        if(cover instanceof CoverFluidFilter) {
            GTLog.logger.info(" - filter found on other");
            return ((CoverFluidFilter) cover).testFluidStack(fluid);
        }
        GTLog.logger.info(" - pass");
        return true;
    }
}
