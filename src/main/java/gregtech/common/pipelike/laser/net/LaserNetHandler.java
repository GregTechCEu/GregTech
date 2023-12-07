package gregtech.common.pipelike.laser.net;

import gregtech.api.capability.ILaserContainer;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaserNetHandler implements ILaserContainer {

    private LaserPipeNet net;
    private final TileEntityLaserPipe pipe;
    private final EnumFacing facing;

    public LaserNetHandler(LaserPipeNet net, @NotNull TileEntityLaserPipe pipe, @Nullable EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
    }

    public void updateNetwork(LaserPipeNet net) {
        this.net = net;
    }

    private void setPipesActive() {
        for (BlockPos pos : net.getAllNodes().keySet()) {
            if (pipe.getWorld().getTileEntity(pos) instanceof TileEntityLaserPipe laserPipe) {
                laserPipe.setActive(true, 100);
            }
        }
    }

    @Nullable
    private ILaserContainer getInnerContainer() {
        if (net == null || pipe == null || pipe.isInvalid() || facing == null) {
            return null;
        }

        LaserRoutePath data = net.getNetData(pipe.getPipePos(), facing);
        if (data == null) {
            return null;
        }

        return data.getHandler();
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        setPipesActive();
        return handler.acceptEnergyFromNetwork(side, voltage, amperage);
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return false;
        return handler.inputsEnergy(side);
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return false;
        return handler.outputsEnergy(side);
    }

    @Override
    public long changeEnergy(long amount) {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        setPipesActive();
        return handler.changeEnergy(amount);
    }

    @Override
    public long getEnergyStored() {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.getEnergyStored();
    }

    @Override
    public long getEnergyCapacity() {
        ILaserContainer handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.getEnergyCapacity();
    }

    @Override
    public long getInputAmperage() {
        return 0;
    }

    @Override
    public long getInputVoltage() {
        return 0;
    }

    public LaserPipeNet getNet() {
        return net;
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
