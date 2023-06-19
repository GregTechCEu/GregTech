package gregtech.common.pipelike.optical.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OpticalNetWalker extends PipeNetWalker {

    @Nullable
    public static List<OpticalPipeNet.OpticalInventory> createNetData(World world, BlockPos sourcePipe, EnumFacing faceToSourceHandler) {
        OpticalNetWalker walker = new OpticalNetWalker(world, sourcePipe, 1, new ArrayList<>(), null);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.traversePipeNet();
        return walker.isFailed() ? null : walker.inventories;
    }

    private OpticalPipeProperties minProperties;
    private final List<OpticalPipeNet.OpticalInventory> inventories;
    private BlockPos sourcePipe;
    private EnumFacing facingToHandler;

    protected OpticalNetWalker(World world, BlockPos sourcePipe, int distance, List<OpticalPipeNet.OpticalInventory> inventories, OpticalPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.inventories = inventories;
        this.minProperties = properties;
    }

    @Override
    protected PipeNetWalker createSubWalker(World world, EnumFacing facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        OpticalNetWalker walker = new OpticalNetWalker(world, nextPos, walkedBlocks, inventories, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        return walker;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        OpticalPipeProperties pipeProperties = ((TileEntityOpticalPipe) pipeTile).getNodeData();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new OpticalPipeProperties(pipeProperties);
        }
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null || (GTUtility.arePosEqual(pipePos, sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }
        IDataAccessHatch handler = neighbourTile.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, faceToNeighbour.getOpposite());
        if (handler != null) {
            inventories.add(new OpticalPipeNet.OpticalInventory(new BlockPos(pipePos), faceToNeighbour, getWalkedBlocks(), minProperties));
        }
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        return neighbourPipe instanceof TileEntityOpticalPipe;
    }
}
