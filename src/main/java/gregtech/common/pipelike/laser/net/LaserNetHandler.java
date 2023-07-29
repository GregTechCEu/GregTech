package gregtech.common.pipelike.laser.net;

import gregtech.api.capability.ILaserContainer;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class LaserNetHandler implements ILaserContainer {
    private LaserPipeNet net;
    private final TileEntityLaserPipe pipe;
    private final EnumFacing facing;
    private final World world;

    public LaserNetHandler(LaserPipeNet net, @Nonnull TileEntityLaserPipe pipe, @Nullable EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
        this.world = pipe.getWorld();
    }

    public void updateNetwork(LaserPipeNet net) {
        this.net = net;
    }

    @Override
    public long changeEnergy(long amount, @Nonnull Collection<ILaserContainer> seen) {
        ILaserContainer handler = getInnerContainer(seen);
        if (handler == null) return 0;
        return handler.changeEnergy(amount, seen);
    }

    @Override
    public long getEnergyStored(@Nonnull Collection<ILaserContainer> seen) {
        ILaserContainer handler = getInnerContainer(seen);
        if (handler == null) return 0;
        return handler.getEnergyStored(seen);
    }

    @Nullable
    private ILaserContainer getInnerContainer(@Nonnull Collection<ILaserContainer> seen) {
        if (net == null || pipe == null || pipe.isInvalid() || pipe.isFaceBlocked(facing)) {
            return null;
        }

        LaserPipeNet.LaserData data = net.getNetData(pipe.getPipePos(), facing);
        if (data == null) {
            return null;
        }

        ILaserContainer handler = data.getHandler(world);
        if (seen.contains(handler)) {
            return null;
        }
        return handler;
    }

    @Override
    public long getEnergyCapacity(@Nonnull Collection<ILaserContainer> seen) {
        ILaserContainer handler = getInnerContainer(seen);
        if (handler == null) return 0;
        return handler.getEnergyCapacity(seen);
    }

    public LaserPipeNet getNet() {
        return net;
    }
}
