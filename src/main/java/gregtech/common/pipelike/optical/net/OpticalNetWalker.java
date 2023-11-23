package gregtech.common.pipelike.optical.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.optical.OpticalPipeProperties;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class OpticalNetWalker extends PipeNetWalker<TileEntityOpticalPipe> {

    @Nullable
    public static OpticalPipeNet.OpticalInventory createNetData(World world, BlockPos sourcePipe, EnumFacing faceToSourceHandler) {
        OpticalNetWalker walker = new OpticalNetWalker(world, sourcePipe, 1, null, null);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.traversePipeNet();
        return walker.isFailed() ? null : walker.inventory;
    }

    private OpticalPipeProperties minProperties;
    private OpticalPipeNet.OpticalInventory inventory;
    private BlockPos sourcePipe;
    private EnumFacing facingToHandler;

    protected OpticalNetWalker(World world, BlockPos sourcePipe, int distance, OpticalPipeNet.OpticalInventory inventory, OpticalPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.inventory = inventory;
        this.minProperties = properties;
    }

    @Override
    protected PipeNetWalker<TileEntityOpticalPipe> createSubWalker(World world, EnumFacing facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        OpticalNetWalker walker = new OpticalNetWalker(world, nextPos, walkedBlocks, inventory, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        return walker;
    }

    @Override
    protected void checkPipe(TileEntityOpticalPipe pipeTile, BlockPos pos) {
        OpticalPipeProperties pipeProperties = pipeTile.getNodeData();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new OpticalPipeProperties(pipeProperties);
        }
    }

    @Override
    protected void checkNeighbour(TileEntityOpticalPipe pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null || (GTUtility.arePosEqual(pipePos, sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }

        if (inventory == null) {
            if (neighbourTile.hasCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, faceToNeighbour.getOpposite()) ||
                    neighbourTile.hasCapability(GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER, faceToNeighbour.getOpposite())) {
                inventory = new OpticalPipeNet.OpticalInventory(new BlockPos(pipePos), faceToNeighbour, getWalkedBlocks(), minProperties);
            }
        }
    }

    @Override
    protected Class<TileEntityOpticalPipe> getBasePipeClass() {
        return TileEntityOpticalPipe.class;
    }
}
