package gregtech.common.pipelike.net.energy;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.graphnet.group.GroupData;
import gregtech.api.graphnet.group.NetGroup;
import gregtech.api.graphnet.group.PathCacheGroupData;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.util.GTLog;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;

public class EnergyCapabilityObject implements IPipeCapabilityObject, IEnergyContainer {

    public static final int ACTIVE_KEY = 122;

    private @Nullable PipeTileEntity tile;

    private final @NotNull WorldPipeNode node;

    private boolean transferring = false;

    public EnergyCapabilityObject(@NotNull WorldPipeNode node) {
        this.node = node;
    }

    private boolean inputDisallowed(EnumFacing side) {
        if (side == null) return false;
        if (tile == null) return true;
        else return tile.isBlocked(side);
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage, boolean simulate) {
        if (tile == null || this.transferring || inputDisallowed(side)) return 0;
        NetGroup group = node.getGroupSafe();
        if (!(group.getData() instanceof EnergyGroupData data)) return 0;

        this.transferring = true;

        PathCacheGroupData.SecondaryCache cache = data.getOrCreate(node);
        List<EnergyPath> paths = new ObjectArrayList<>(group.getNodesUnderKey(ACTIVE_KEY).size());
        for (NetNode dest : group.getNodesUnderKey(ACTIVE_KEY)) {
            if (!(dest instanceof WorldPipeNode)) continue;
            EnergyPath path = (EnergyPath) cache.getOrCompute(dest);
            if (path == null) continue;
            // construct the path list in order of ascending weight
            int i = 0;
            while (i < paths.size()) {
                if (paths.get(i).getWeight() >= path.getWeight()) break;
                else i++;
            }
            paths.add(i, path);
        }
        long available = amperage;
        for (int i = 0; i < paths.size(); i++) {
            EnergyPath path = paths.get(i);
            NetNode target = path.getTargetNode();
            // WorldPipeNode-ness was already determined in earlier loop
            IWorldPipeNetTile pipeTile = ((WorldPipeNode) target).getTileEntity();
            EnumMap<EnumFacing, TileEntity> targets = pipeTile.getTargetsWithCapabilities(((WorldPipeNode) target));
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (target == node && facing == side) continue; // anti insert-to-our-source logic
                TileEntity tile = targets.get(facing);
                if (tile == null) continue;
                IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                        facing.getOpposite());
                if (container == null) continue;
                long allowed = container.acceptEnergyFromNetwork(facing, voltage, amperage, true);
                if (allowed <= 0) continue;
                EnergyPath.PathFlowReport flow = path.traverse(voltage, allowed);
                if (flow.euOut() <= 0) continue;
                available -= allowed;
                if (!simulate) {
                    flow.report();
                    container.acceptEnergyFromNetwork(facing, flow.voltageOut(), flow.amperageOut(), false);
                }
            }
        }

        this.transferring = false;
        return amperage - available;
    }

    @Nullable
    private EnergyGroupData getGroupData() {
        NetGroup group = node.getGroupUnsafe();
        if (group == null) return null;
        GroupData data = group.getData();
        if (!(data instanceof EnergyGroupData e)) return null;
        return e;
    }

    @Override
    public long getInputAmperage() {
        return node.getData().getLogicEntryDefaultable(AmperageLimitLogic.TYPE).getValue();
    }

    @Override
    public long getInputVoltage() {
        return node.getData().getLogicEntryDefaultable(VoltageLimitLogic.TYPE).getValue();
    }

    @Override
    public void init(@NotNull PipeTileEntity tile, @NotNull PipeCapabilityWrapper wrapper) {
        this.tile = tile;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this);
        }
        return null;
    }

    @Override
    public long getInputPerSec() {
        EnergyGroupData data = getGroupData();
        if (data == null) return 0;
        else return data
                .getEnergyInPerSec(FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter());
    }

    @Override
    public long getOutputPerSec() {
        EnergyGroupData data = getGroupData();
        if (data == null) return 0;
        else return data
                .getEnergyOutPerSec(FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter());
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return !inputDisallowed(side);
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        GTLog.logger.fatal("Do not use changeEnergy() for cables! Use acceptEnergyFromNetwork()");
        return acceptEnergyFromNetwork(null,
                differenceAmount / getInputAmperage(),
                differenceAmount / getInputVoltage(), false) * getInputVoltage();
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public long getEnergyCapacity() {
        return getInputAmperage() * getInputVoltage();
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
