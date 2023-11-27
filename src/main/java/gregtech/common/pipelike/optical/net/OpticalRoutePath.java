package gregtech.common.pipelike.optical.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.capability.IOpticalDataAccessHatch;
import gregtech.api.pipenet.IRoutePath;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpticalRoutePath implements IRoutePath<TileEntityOpticalPipe> {

    private final TileEntityOpticalPipe targetPipe;
    private final EnumFacing faceToHandler;
    private final int distance;

    public OpticalRoutePath(TileEntityOpticalPipe targetPipe, EnumFacing faceToHandler, int distance) {
        this.targetPipe = targetPipe;
        this.faceToHandler = faceToHandler;
        this.distance = distance;
    }

    @NotNull
    @Override
    public TileEntityOpticalPipe getTargetPipe() {
        return targetPipe;
    }

    @NotNull
    @Override
    public EnumFacing getTargetFacing() {
        return faceToHandler;
    }

    public int getDistance() {
        return distance;
    }

    @Nullable
    public IOpticalDataAccessHatch getDataHatch() {
        IDataAccessHatch dataAccessHatch = getTargetCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS);
        return dataAccessHatch instanceof IOpticalDataAccessHatch opticalHatch ? opticalHatch : null;
    }

    @Nullable
    public IOpticalComputationProvider getComputationHatch() {
        return getTargetCapability(GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER);
    }
}
