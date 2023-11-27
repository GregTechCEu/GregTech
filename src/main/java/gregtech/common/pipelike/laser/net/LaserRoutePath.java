package gregtech.common.pipelike.laser.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.pipenet.IRoutePath;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// jabel moment
public class LaserRoutePath implements IRoutePath<TileEntityLaserPipe> {

    private final TileEntityLaserPipe targetPipe;
    private final EnumFacing faceToHandler;
    private final int distance;

    public LaserRoutePath(TileEntityLaserPipe targetPipe, EnumFacing faceToHandler, int distance) {
        this.targetPipe = targetPipe;
        this.faceToHandler = faceToHandler;
        this.distance = distance;
    }

    /**
     * Gets the current face to handler
     *
     * @return The face to handler
     */
    @NotNull
    public EnumFacing getFaceToHandler() {
        return faceToHandler;
    }

    @NotNull
    @Override
    public TileEntityLaserPipe getTargetPipe() {
        return targetPipe;
    }

    @NotNull
    @Override
    public EnumFacing getTargetFacing() {
        return faceToHandler;
    }

    /**
     * Gets the manhattan distance traveled during walking
     *
     * @return The distance in blocks
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Gets the handler if it exists
     *
     * @return the handler
     */
    @Nullable
    public ILaserContainer getHandler() {
        return getTargetCapability(GregtechTileCapabilities.CAPABILITY_LASER);
    }
}
