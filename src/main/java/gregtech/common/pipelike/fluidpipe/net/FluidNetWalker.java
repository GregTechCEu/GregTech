package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
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
        FluidNetWalker walker = new FluidNetWalker(net, world, sourcePipe, 1, new ArrayList<>());
        walker.traversePipeNet();
        return walker.inventories;
    }

    private final List<FluidPipeNet.Inventory> inventories;

    protected FluidNetWalker(PipeNet<?> net, World world, BlockPos sourcePipe, int walkedBlocks, List<FluidPipeNet.Inventory> inventories) {
        super(net, world, sourcePipe, walkedBlocks);
        this.inventories = inventories;
    }

    @Override
    protected PipeNetWalker createSubWalker(PipeNet<?> net, World world, BlockPos nextPos, int walkedBlocks) {
        return new FluidNetWalker(net, world, nextPos, walkedBlocks, inventories);
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
    }

    @Override
    protected void checkNeighbour(BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null) return;
        IFluidHandler handler = neighbourTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, faceToNeighbour.getOpposite());
        if (handler != null)
            inventories.add(new FluidPipeNet.Inventory(pipePos, faceToNeighbour, getWalkedBlocks()));
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        return neighbourPipe instanceof TileEntityFluidPipe && ((TileEntityFluidPipe) currentPipe).getNodeData().tanks == ((TileEntityFluidPipe) neighbourPipe).getNodeData().tanks;
    }
}
