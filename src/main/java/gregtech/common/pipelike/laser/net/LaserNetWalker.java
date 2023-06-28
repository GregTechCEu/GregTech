package gregtech.common.pipelike.laser.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.laser.LaserPipeProperties;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LaserNetWalker extends PipeNetWalker {

    @javax.annotation.Nullable
    public static LaserPipeNet.LaserData createNetData(World world, BlockPos sourcePipe, EnumFacing faceToSourceHandler) {
        LaserNetWalker walker = new LaserNetWalker(world, sourcePipe, 1, null, null);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.traversePipeNet();
        return walker.isFailed() ? null : walker.laserData;
    }

    private LaserPipeProperties minProperties;
    private LaserPipeNet.LaserData laserData;
    private BlockPos sourcePipe;
    private EnumFacing facingToHandler;

    protected LaserNetWalker(World world, BlockPos sourcePipe, int distance, LaserPipeNet.LaserData laserData, LaserPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.laserData = laserData;
        this.minProperties = properties;
    }
    @Override
    protected PipeNetWalker createSubWalker(World world, EnumFacing facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        LaserNetWalker walker = new LaserNetWalker(world, nextPos, walkedBlocks, laserData, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        return walker;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        LaserPipeProperties pipeProperties = ((TileEntityLaserPipe) pipeTile).getNodeData();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new LaserPipeProperties(pipeProperties);
        }
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null || (GTUtility.arePosEqual(pipePos, sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }

        if (laserData == null) {
            ILaserContainer handler = neighbourTile.getCapability(GregtechTileCapabilities.CAPABILITY_LASER, faceToNeighbour.getOpposite());
            if (handler != null) {
                laserData = new LaserPipeNet.LaserData(new BlockPos(pipePos), faceToNeighbour, getWalkedBlocks(), minProperties);
            }
        }
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        if (neighbourPipe instanceof TileEntityLaserPipe pipe) {
            // I really cannot think of what else i could do
            boolean yAxisSame = sourcePipe.getY() == pipe.getPipePos().getY() && sourcePipe.getY() == pipePos.getY();
            boolean xAxisSame = sourcePipe.getX() == pipe.getPipePos().getX() && sourcePipe.getX() == pipePos.getX();
            boolean zAxisSame = sourcePipe.getZ() == pipe.getPipePos().getZ() && sourcePipe.getZ() == pipePos.getZ();

            return yAxisSame && zAxisSame || zAxisSame && xAxisSame || xAxisSame && yAxisSame;
        }
        return false;
    }
}
