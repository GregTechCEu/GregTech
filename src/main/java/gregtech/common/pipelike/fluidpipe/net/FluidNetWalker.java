package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.cover.CoverBehavior;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FluidNetWalker extends PipeNetWalker {

    public static List<FluidPipeNet.Inventory> createNetData(FluidPipeNet net, World world, BlockPos sourcePipe) {
        FluidNetWalker walker = new FluidNetWalker(net, world, sourcePipe, 1, new ArrayList<>(), new ArrayList<>(), Integer.MAX_VALUE, new ArrayList<>());
        walker.traversePipeNet();
        return walker.inventories;
    }

    private final List<FluidPipeNet.Inventory> inventories;
    private final List<Object> pathObjects;
    private final List<TileEntityFluidPipeTickable> tickingPipes;
    private int rate;

    protected FluidNetWalker(PipeNet<?> net, World world, BlockPos sourcePipe, int walkedBlocks, List<FluidPipeNet.Inventory> inventories, List<Object> pathObjects, int rate, List<TileEntityFluidPipeTickable> tickingPipes) {
        super(net, world, sourcePipe, walkedBlocks);
        this.inventories = inventories;
        this.pathObjects = pathObjects;
        this.rate = rate;
        this.tickingPipes = tickingPipes;
    }

    @Override
    protected PipeNetWalker createSubWalker(PipeNet<?> net, World world, BlockPos nextPos, int walkedBlocks) {
        return new FluidNetWalker(net, world, nextPos, walkedBlocks, inventories, new ArrayList<>(pathObjects), rate, new ArrayList<>(tickingPipes));
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        pathObjects.add(pipeTile);
        this.rate = Math.min(this.rate, ((TileEntityFluidPipe)pipeTile).getNodeData().throughput);
        int validPipes = 0;
        for(EnumFacing facing : EnumFacing.values()) {
            TileEntity tile = pipeTile.getPipeWorld().getTileEntity(pos.offset(facing));
            if(tile instanceof IPipeTile && isValidPipe(pipeTile, (IPipeTile<?, ?>) tile, pos, facing)) {
                checkCover(pipeTile.getCoverableImplementation().getCoverAtSide(facing));
                validPipes++;
            }
        }
        if(validPipes > 2) {
            tickingPipes.add((TileEntityFluidPipeTickable) pipeTile.setSupportsTicking());
        }
    }

    protected void checkCover(CoverBehavior coverBehavior) {
        if(coverBehavior instanceof CoverFluidFilter)
            pathObjects.add(coverBehavior);
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null) return;
        IFluidHandler handler = neighbourTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, faceToNeighbour.getOpposite());
        if (handler != null) {
            inventories.add(new FluidPipeNet.Inventory(pipePos, faceToNeighbour, getWalkedBlocks(), new ArrayList<>(pathObjects), rate, new ArrayList<>(tickingPipes)));
            tickingPipes.add((TileEntityFluidPipeTickable) pipeTile.setSupportsTicking());
        }
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        return neighbourPipe instanceof TileEntityFluidPipe;
    }
}
