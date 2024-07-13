package gregtech.common.pipelike.net.energy;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.graphnet.AbstractGroupData;
import gregtech.api.graphnet.NetGroup;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.NetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.edge.util.FlowConsumer;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.PipeTileEntity;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.api.graphnet.traverse.TraverseHelpers;
import gregtech.api.util.GTLog;
import gregtech.common.pipelikeold.cable.net.EnergyGroupData;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Iterator;

public class EnergyCapabilityObject implements IPipeCapabilityObject, IEnergyContainer {

    private final WorldPipeNet net;
    private final FlowWorldPipeNetPath.Provider provider;
    private @Nullable PipeTileEntity tile;

    private final EnumMap<EnumFacing, AbstractNetFlowEdge> internalBuffers = new EnumMap<>(EnumFacing.class);

    private boolean transferring = false;

    public <N extends WorldPipeNet & FlowWorldPipeNetPath.Provider> EnergyCapabilityObject(@NotNull N net, WorldPipeNetNode node) {
        // eh, duplicate references so what
        this.net = net;
        this.provider = net;
        for (EnumFacing facing : EnumFacing.VALUES) {
            AbstractNetFlowEdge edge = (AbstractNetFlowEdge) net.getNewEdge();
            edge.setData(NetLogicData.union(node.getData(), (NetLogicData) null));
            internalBuffers.put(facing, edge);
        }
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage, boolean simulate) {
        if (tile == null || this.transferring) return 0;
        this.transferring = true;

        SimulatorKey simulator = null;
        if (simulate) simulator = SimulatorKey.getNewSimulatorInstance();
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();

        AbstractNetFlowEdge internalBuffer = this.internalBuffers.get(side);
        long limit = internalBuffer.getFlowLimit(IPredicateTestObject.INSTANCE, net, tick, simulator);
        if (limit <= 0) return 0;

        long availableAmperage = Math.min(amperage, limit);
        FlowConsumer consumer = new FlowConsumer(internalBuffer, IPredicateTestObject.INSTANCE, net, availableAmperage,
                tick, simulator);

        EnergyTraverseData data = new EnergyTraverseData(net, IPredicateTestObject.INSTANCE, simulator, tick, voltage,
                tile.getPos(), side);
        availableAmperage -= TraverseHelpers.traverseFlood(data, getPaths(data), amperage);
        if (availableAmperage > 0) {
            TraverseHelpers.traverseDumb(data, getPaths(data), data::handleOverflow, availableAmperage);
        }
        data.runPostActions();

        consumer.finalReduction(availableAmperage);
        this.transferring = false;
        return amperage - availableAmperage;
    }

    private Iterator<FlowWorldPipeNetPath> getPaths(EnergyTraverseData data) {
        assert tile != null;
        return provider.getPaths(net.getNode(tile.getPos()), data.getTestObject(), data.getSimulatorKey(), data.getQueryTick());
    }

    private long getFlux() {
        if (tile == null) return 0;
        NetNode node = net.getNode(tile.getPos());
        if (node == null) return 0;
        NetGroup group = node.getGroupUnsafe();
        if (group == null) return 0;
        AbstractGroupData data = group.getData();
        if (!(data instanceof EnergyGroupData e)) return 0;
        return e.getEnergyFluxPerSec();
    }

    @Override
    public long getInputAmperage() {
        if (tile == null) return 0;
        return tile.getNetLogicData(net.mapName).getLogicEntryDefaultable(ThroughputLogic.INSTANCE).getValue();
    }

    @Override
    public long getInputVoltage() {
        if (tile == null) return 0;
        return tile.getNetLogicData(net.mapName).getLogicEntryDefaultable(VoltageLimitLogic.INSTANCE).getValue();
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
        return getFlux();
    }

    @Override
    public long getOutputPerSec() {
        return getFlux();
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return true;
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
                differenceAmount / getInputVoltage()) * getInputVoltage();
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public long getEnergyCapacity() {
        return getInputAmperage() * getInputVoltage();
    }
}
