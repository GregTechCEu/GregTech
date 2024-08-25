package gregtech.common.pipelike.net.energy;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.graphnet.AbstractGroupData;
import gregtech.api.graphnet.NetGroup;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.api.graphnet.traverse.TraverseHelpers;
import gregtech.api.util.GTLog;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Iterator;

public class EnergyCapabilityObject implements IPipeCapabilityObject, IEnergyContainer {

    private final WorldPipeNet net;
    private @Nullable PipeTileEntity tile;

    private final EnumMap<EnumFacing, AbstractNetFlowEdge> internalBuffers = new EnumMap<>(EnumFacing.class);
    private final WorldPipeNetNode node;

    private boolean transferring = false;

    public <N extends WorldPipeNet & FlowWorldPipeNetPath.Provider> EnergyCapabilityObject(@NotNull N net,
                                                                                           WorldPipeNetNode node) {
        this.net = net;
        this.node = node;
        for (EnumFacing facing : EnumFacing.VALUES) {
            AbstractNetFlowEdge edge = (AbstractNetFlowEdge) net.getNewEdge();
            edge.setData(NetLogicData.union(node.getData(), (NetLogicData) null));
            internalBuffers.put(facing, edge);
        }
    }

    private FlowWorldPipeNetPath.Provider getProvider() {
        return (FlowWorldPipeNetPath.Provider) net;
    }

    private boolean inputDisallowed(EnumFacing side) {
        if (side == null) return false;
        if (tile == null) return true;
        else return tile.isBlocked(side);
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage, boolean simulate) {
        if (tile == null || this.transferring || inputDisallowed(side)) return 0;
        this.transferring = true;

        SimulatorKey simulator = null;
        if (simulate) simulator = SimulatorKey.getNewSimulatorInstance();
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();

        AbstractNetFlowEdge internalBuffer = this.internalBuffers.get(side);
        long bufferOverflowAmperage = 0;
        if (internalBuffer != null) {
            long limit = internalBuffer.getFlowLimit(IPredicateTestObject.INSTANCE, net, tick, simulator);
            if (limit <= 0) {
                this.transferring = false;
                return 0;
            } else if (amperage > limit) {
                bufferOverflowAmperage = amperage - limit;
            }
        }
        long availableAmperage = amperage;

        EnergyTraverseData data = new EnergyTraverseData(net, IPredicateTestObject.INSTANCE, simulator, tick, voltage,
                tile.getPos(), side, bufferOverflowAmperage);
        availableAmperage -= TraverseHelpers.traverseFlood(data, getPaths(data),
                availableAmperage - bufferOverflowAmperage);
        if (availableAmperage > 0) {
            availableAmperage -= TraverseHelpers.traverseDumb(data, getPaths(data), data::handleOverflow,
                    availableAmperage);
        }
        long accepted = amperage - availableAmperage;

        if (internalBuffer != null) {
            data.resetPathVoltage();
            bufferOverflowAmperage = data.calculateActualBufferOverflow(bufferOverflowAmperage);
            data.consumeFlowLimit(internalBuffer, node, accepted - bufferOverflowAmperage);
            if (bufferOverflowAmperage > 0) {
                data.handleOverflow(node, bufferOverflowAmperage);
                accepted += bufferOverflowAmperage;
            }
        }
        if (!simulate) {
            EnergyGroupData group = getEnergyData();
            if (group != null) {
                group.addEnergyInPerSec(accepted * voltage, data.getQueryTick());
            }
        }
        this.transferring = false;
        return accepted;
    }

    private Iterator<FlowWorldPipeNetPath> getPaths(EnergyTraverseData data) {
        assert tile != null;
        return getProvider().getPaths(net.getNode(tile.getPos()), data.getTestObject(), data.getSimulatorKey(),
                data.getQueryTick());
    }

    @Nullable
    private EnergyGroupData getEnergyData() {
        if (tile == null) return null;
        NetNode node = net.getNode(tile.getPos());
        if (node == null) return null;
        NetGroup group = node.getGroupUnsafe();
        if (group == null) return null;
        AbstractGroupData data = group.getData();
        if (!(data instanceof EnergyGroupData e)) return null;
        return e;
    }

    @Override
    public long getInputAmperage() {
        if (tile == null) return 0;
        return tile.getNetLogicData(net.getNetworkID()).getLogicEntryDefaultable(ThroughputLogic.INSTANCE).getValue();
    }

    @Override
    public long getInputVoltage() {
        if (tile == null) return 0;
        return tile.getNetLogicData(net.getNetworkID()).getLogicEntryDefaultable(VoltageLimitLogic.INSTANCE).getValue();
    }

    @Override
    public void setTile(@Nullable PipeTileEntity tile) {
        this.tile = tile;
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldEnergyNet.CAPABILITIES;
    }

    @Override
    public <T> T getCapabilityForSide(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER
                    .cast(this);
        }
        return null;
    }

    @Override
    public long getInputPerSec() {
        EnergyGroupData data = getEnergyData();
        if (data == null) return 0;
        else return data
                .getEnergyInPerSec(FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter());
    }

    @Override
    public long getOutputPerSec() {
        EnergyGroupData data = getEnergyData();
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
