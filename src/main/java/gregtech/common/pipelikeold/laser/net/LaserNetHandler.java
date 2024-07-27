package gregtech.common.pipelikeold.laser.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserRelay;
import gregtech.api.graphnet.pipenetold.IPipeNetHandler;
import gregtech.api.graphnet.NetGroup;
import gregtech.api.graphnet.pipenetold.PipeNetNode;
import gregtech.api.graphnet.pipenetold.PipeNetPath;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.common.pipelikeold.laser.LaserPipeProperties;
import gregtech.common.pipelikeold.laser.LaserPipeType;
import gregtech.common.pipelikeold.laser.tile.TileEntityLaserPipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

public class LaserNetHandler implements ILaserRelay, IPipeNetHandler {

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
        NetGroup<LaserPipeType, LaserPipeProperties, NetEdge> group = getNet().getNode(this.pipe.getPipePos())
                .getGroupSafe();
        if (group != null) {
            for (PipeNetNode<LaserPipeType, LaserPipeProperties, NetEdge> node : group.getNodes()) {
                if (node.getHeldMTE() instanceof TileEntityLaserPipe laserPipe) {
                    laserPipe.setActive(true, 100);
                }
            }
        }
    }

    @Nullable
    private ILaserRelay getInnerContainer() {
        if (net == null || pipe.isInvalid() || facing == null) {
            return null;
        }

        Iterator<PipeNetPath<LaserPipeType, LaserPipeProperties, NetEdge>> data = net.getPaths(this.pipe);
        if (data == null || !data.hasNext()) return null;
        Map<EnumFacing, TileEntity> connecteds = data.next().getTargetTEs();
        if (data.hasNext()) return null;
        if (connecteds.size() != 1) return null;
        EnumFacing facing = connecteds.keySet().iterator().next();

        return connecteds.get(facing).getCapability(GregtechTileCapabilities.CAPABILITY_LASER, facing.getOpposite());
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage, boolean simulate) {
        ILaserRelay handler = getInnerContainer();
        if (handler == null) return 0;
        setPipesActive();
        return handler.acceptEnergyFromNetwork(side, voltage, amperage, simulate);
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        ILaserRelay handler = getInnerContainer();
        if (handler == null) return false;
        return handler.inputsEnergy(side);
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        ILaserRelay handler = getInnerContainer();
        if (handler == null) return false;
        return handler.outputsEnergy(side);
    }

    @Override
    public long changeEnergy(long amount) {
        ILaserRelay handler = getInnerContainer();
        if (handler == null) return 0;
        setPipesActive();
        return handler.changeEnergy(amount);
    }

    @Override
    public long getEnergyStored() {
        ILaserRelay handler = getInnerContainer();
        if (handler == null) return 0;
        return handler.getEnergyStored();
    }

    @Override
    public long getEnergyCapacity() {
        ILaserRelay handler = getInnerContainer();
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
