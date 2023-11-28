package gregtech.common.pipelike.laser.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class LaserNetWalker extends PipeNetWalker<TileEntityLaserPipe> {

    public static final LaserRoutePath FAILED_MARKER = new LaserRoutePath(null, null, 0);

    @Nullable
    public static LaserRoutePath createNetData(World world, BlockPos sourcePipe, EnumFacing faceToSourceHandler) {
        LaserNetWalker walker = new LaserNetWalker(world, sourcePipe, 1);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.axis = faceToSourceHandler.getAxis();
        walker.traversePipeNet();
        return walker.isFailed() ? FAILED_MARKER : walker.routePath;
    }

    private static final EnumFacing[] X_AXIS_FACINGS = { EnumFacing.WEST, EnumFacing.EAST };
    private static final EnumFacing[] Y_AXIS_FACINGS = { EnumFacing.UP, EnumFacing.DOWN };
    private static final EnumFacing[] Z_AXIS_FACINGS = { EnumFacing.NORTH, EnumFacing.SOUTH };

    private LaserRoutePath routePath;
    private BlockPos sourcePipe;
    private EnumFacing facingToHandler;
    private EnumFacing.Axis axis;

    protected LaserNetWalker(World world, BlockPos sourcePipe, int distance) {
        super(world, sourcePipe, distance);
    }

    @Override
    protected PipeNetWalker<TileEntityLaserPipe> createSubWalker(World world, EnumFacing facingToNextPos,
                                                                 BlockPos nextPos, int walkedBlocks) {
        LaserNetWalker walker = new LaserNetWalker(world, nextPos, walkedBlocks);
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
    protected void checkPipe(TileEntityLaserPipe pipeTile, BlockPos pos) {}

    @Override
    protected void checkNeighbour(TileEntityLaserPipe pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour,
                                  @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null ||
                (GTUtility.arePosEqual(pipePos, sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }

        if (((LaserNetWalker) root).routePath == null) {
            ILaserContainer handler = neighbourTile.getCapability(GregtechTileCapabilities.CAPABILITY_LASER,
                    faceToNeighbour.getOpposite());
            if (handler != null) {
                ((LaserNetWalker) root).routePath = new LaserRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks());
                stop();
            }
        }
    }

    @Override
    protected Class<TileEntityLaserPipe> getBasePipeClass() {
        return TileEntityLaserPipe.class;
    }
}
