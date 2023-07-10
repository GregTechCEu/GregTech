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

    @Nullable
    public static LaserPipeNet.LaserData createNetData(World world, BlockPos sourcePipe, EnumFacing faceToSourceHandler) {
        LaserNetWalker walker = new LaserNetWalker(world, sourcePipe, 1, null, null);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.axis = faceToSourceHandler.getAxis();
        walker.traversePipeNet();
        return walker.isFailed() ? null : walker.laserData;
    }

    private static final EnumFacing[] X_AXIS_FACINGS = {EnumFacing.WEST, EnumFacing.EAST};
    private static final EnumFacing[] Y_AXIS_FACINGS = {EnumFacing.UP, EnumFacing.DOWN};
    private static final EnumFacing[] Z_AXIS_FACINGS = {EnumFacing.NORTH, EnumFacing.SOUTH};

    private LaserPipeProperties minProperties;
    private LaserPipeNet.LaserData laserData;
    private BlockPos sourcePipe;
    private EnumFacing facingToHandler;
    private EnumFacing.Axis axis;

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
        walker.axis = axis;
        return walker;
    }

    @Override
    protected EnumFacing[] getSurroundingPipeSides() {
        return switch (axis) {
            case X -> X_AXIS_FACINGS;
            case Y -> Y_AXIS_FACINGS;
            case Z -> Z_AXIS_FACINGS;
        };
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
        return neighbourPipe instanceof TileEntityLaserPipe;
    }
}
