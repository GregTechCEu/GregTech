package gregtech.common.pipelike.cable.net;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RoutePath {
    private final BlockPos destPipePos;
    private final EnumFacing destFacing;
    private final int distance;
    private final TileEntityCable[] path;
    private final long maxLoss;

    public RoutePath(BlockPos destPipePos, EnumFacing destFacing, TileEntityCable[] path, int distance, long maxLoss) {
        this.destPipePos = destPipePos;
        this.destFacing = destFacing;
        this.path = path;
        this.distance = distance;
        this.maxLoss = maxLoss;
    }

    public int getDistance() {
        return distance;
    }

    public long getMaxLoss() {
        return maxLoss;
    }

    public TileEntityCable[] getPath() {
        return path;
    }

    public BlockPos getPipePos() {
        return destPipePos;
    }

    public EnumFacing getFaceToHandler() {
        return destFacing;
    }

    public BlockPos getHandlerPos() {
        return destPipePos.offset(destFacing);
    }

    public IEnergyContainer getHandler(World world) {
        TileEntity tile = world.getTileEntity(getHandlerPos());
        if (tile != null) {
            return tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, destFacing.getOpposite());
        }
        return null;
    }
}
