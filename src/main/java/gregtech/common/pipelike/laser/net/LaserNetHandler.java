package gregtech.common.pipelike.laser.net;

import gregtech.api.capability.ILaserContainer;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    public long acceptEnergy(EnumFacing side, long amount) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        long energyUsed = handler.acceptEnergy(side, amount);
        if (energyUsed > 0) {
            for (BlockPos pos : net.getAllNodes().keySet()) {
                if (world.getTileEntity(pos) instanceof TileEntityLaserPipe laserPipe) {
                    laserPipe.setActive(true, 20);
                }
            }
        }
        return energyUsed;
    }

    @Override
    public long changeEnergy(long amount) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.changeEnergy(amount);
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return false;
        return handler.inputsEnergy(side);
    }

    @Override
    public long getEnergyStored() {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.getEnergyStored();
    }

    @Nullable
    private ILaserContainer getInnerContainer() {
        if (net == null || pipe == null || pipe.isInvalid() || pipe.isFaceBlocked(facing)) {
            return null;
        }

        LaserPipeNet.LaserData data = net.getNetData(pipe.getPipePos(), facing);
        if (data == null) {
            return null;
        }

        return data.getHandler(world);
    }

    @Override
    public long getEnergyCapacity() {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.getEnergyCapacity();
    }

    public LaserPipeNet getNet() {
        return net;
    }
}
