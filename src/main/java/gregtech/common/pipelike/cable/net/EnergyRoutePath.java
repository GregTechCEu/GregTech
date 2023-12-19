package gregtech.common.pipelike.cable.net;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.pipenet.IRoutePath;
import gregtech.common.pipelike.cable.tile.TileEntityCable;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

public class EnergyRoutePath implements IRoutePath<TileEntityCable> {

    private final TileEntityCable targetPipe;
    private final EnumFacing destFacing;
    private final int distance;
    private final TileEntityCable[] path;
    private final long maxLoss;

    public EnergyRoutePath(EnumFacing destFacing, TileEntityCable[] path, int distance, long maxLoss) {
        this.targetPipe = path[path.length - 1];
        this.destFacing = destFacing;
        this.path = path;
        this.distance = distance;
        this.maxLoss = maxLoss;
    }

    @Override
    public @NotNull TileEntityCable getTargetPipe() {
        return targetPipe;
    }

    @Override
    public @NotNull EnumFacing getTargetFacing() {
        return destFacing;
    }

    @Override
    public int getDistance() {
        return distance;
    }

    public long getMaxLoss() {
        return maxLoss;
    }

    public TileEntityCable[] getPath() {
        return path;
    }

    public IEnergyContainer getHandler() {
        return getTargetCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
    }
}
