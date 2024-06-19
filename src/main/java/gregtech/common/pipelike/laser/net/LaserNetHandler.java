package gregtech.common.pipelike.laser.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.pipenet.IPipeNetHandler;
import gregtech.api.pipenet.NetGroup;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeG;
import gregtech.common.pipelike.laser.LaserPipeProperties;
import gregtech.common.pipelike.laser.LaserPipeType;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class LaserNetHandler implements ILaserContainer, IPipeNetHandler {

    private final WorldLaserPipeNet net;
    private final TileEntityLaserPipe pipe;
    private final EnumFacing facing;

    public LaserNetHandler(WorldLaserPipeNet net, @NotNull TileEntityLaserPipe pipe, @Nullable EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
    }

    @Override
    public WorldLaserPipeNet getNet() {
        return net;
    }

    @Override
    public EnumFacing getFacing() {
        return facing;
    }

    private void setPipesActive() {
        NetGroup<LaserPipeType, LaserPipeProperties> group = getNet().getNode(this.pipe.getPipePos()).getGroupSafe();
        if (group != null) {
            for (NodeG<LaserPipeType, LaserPipeProperties> node : group.getNodes()) {
                if (node.getHeldMTE() instanceof TileEntityLaserPipe laserPipe) {
                    laserPipe.setActive(true, 100);
                }
            }
        }
    }

    @Nullable
    private ILaserContainer getInnerContainer() {
        if (net == null || pipe.isInvalid() || facing == null) {
            return null;
        }

        List<NetPath<LaserPipeType, LaserPipeProperties>> data = net.getPaths(this.pipe, null);
        if (data == null || data.size() != 1) return null;
        Map<EnumFacing, TileEntity> connecteds = data.get(0).getTargetTEs();
        if (connecteds.size() != 1) return null;
        EnumFacing facing = connecteds.keySet().iterator().next();

        return connecteds.get(facing).getCapability(GregtechTileCapabilities.CAPABILITY_LASER, facing.getOpposite());
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

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
