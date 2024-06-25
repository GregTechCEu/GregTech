package gregtech.common.pipelike.cable.net;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.pipenet.AbstractGroupData;
import gregtech.api.pipenet.IPipeNetHandler;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeLossResult;
import gregtech.api.pipenet.edge.AbstractNetFlowEdge;
import gregtech.api.pipenet.edge.NetFlowEdge;
import gregtech.api.pipenet.edge.util.FlowConsumerList;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.FacingPos;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.tile.TileEntityCable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLCommonHandler;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class EnergyNetHandler implements IEnergyContainer, IPipeNetHandler {

    protected static final NetNode<Insulation, WireProperties, NetFlowEdge> FAKE_SOURCE = new NetNode<>(
            new WireProperties(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, false));

    private final WorldEnergyNet net;
    private boolean transfer;
    private final TileEntityCable cable;
    private final EnumFacing facing;
    private final Map<NetNode<Insulation, WireProperties, NetFlowEdge>, NodeLossResult> lossResultCache = new Object2ObjectOpenHashMap<>();
    private Object2LongOpenHashMap<FacingPos> destSimulationCache;
    private final NetFlowEdge inputEdge;

    public EnergyNetHandler(WorldEnergyNet net, TileEntityCable cable, EnumFacing facing) {
        this.net = net;
        this.cable = cable;
        this.facing = facing;
        this.inputEdge = new NetFlowEdge(1) {

            @Override
            public NetNode<?, ?, ?> getSource() {
                return FAKE_SOURCE;
            }

            @Override
            public NetNode<?, ?, ?> getTarget() {
                return EnergyNetHandler.this.cable.getNode();
            }
        };
    }

    @Override
    public WorldEnergyNet getNet() {
        return net;
    }

    @Override
    public EnumFacing getFacing() {
        return facing;
    }

    @Override
    public long getInputPerSec() {
        AbstractGroupData<Insulation, WireProperties> data = net.getGroup(cable.getPipePos()).getData();
        if (!(data instanceof EnergyGroupData e)) return 0;
        return e.getEnergyFluxPerSec();
    }

    @Override
    public long getOutputPerSec() {
        AbstractGroupData<Insulation, WireProperties> data = net.getGroup(cable.getPipePos()).getData();
        if (!(data instanceof EnergyGroupData e)) return 0;
        return e.getEnergyFluxPerSec();
    }

    @Override
    public long getEnergyCanBeInserted() {
        return getEnergyCapacity();
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage, boolean simulate) {
        if (transfer) return 0;
        if (side == null) {
            if (facing == null) return 0;
            side = facing;
        }
        if (amperage <= 0 || voltage <= 0)
            return 0;

        long queryTick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        AbstractNetFlowEdge.ChannelSimulatorKey key = simulate ? AbstractNetFlowEdge.getNewSimulatorInstance() : null;
        destSimulationCache = simulate ? new Object2LongOpenHashMap<>() : null;

        List<NetPath<Insulation, WireProperties, NetFlowEdge>> paths = new ObjectArrayList<>(this.net.getPaths(cable));
        long amperesUsed = distributionRespectCapacity(side, voltage, amperage, queryTick, paths, key);
        if (amperesUsed < amperage) {
            // if we still have undistributed amps, attempt to distribute them while going over edge capacities.
            amperesUsed += distributionIgnoreCapacity(side, voltage, amperage - amperesUsed, queryTick, paths, key);
        }
        this.lossResultCache.forEach((k, v) -> v.getPostAction().accept(k));
        this.lossResultCache.clear();

        this.destSimulationCache = null;

        if (this.cable.getNode().getGroupSafe().getData() instanceof EnergyGroupData data)
            data.addEnergyFluxPerSec(amperesUsed * voltage);
        return amperesUsed;
    }

    private long distributionRespectCapacity(EnumFacing side, long voltage, long amperage, long queryTick,
                                             List<NetPath<Insulation, WireProperties, NetFlowEdge>> paths,
                                             AbstractNetFlowEdge.ChannelSimulatorKey simulator) {
        long availableAmperage = amperage;
        mainloop:
        for (int i = 0; i < paths.size(); i++) {
            NetPath<Insulation, WireProperties, NetFlowEdge> path = paths.get(i);
            // skip paths where loss exceeds available voltage
            if (path.getWeight() > voltage) continue;
            Iterator<EnumFacing> iterator = path.getFacingIterator();
            boolean pathDestThis = path.getTargetNode().getNodePos().equals(this.cable.getPipePos());
            while (iterator.hasNext()) {
                NetPath.FacedNetPath<Insulation, WireProperties, NetFlowEdge> facedPath = path
                        .withFacing(iterator.next());
                if (pathDestThis && facedPath.facing == side) {
                    // do not distribute power back into our source
                    continue;
                }

                IEnergyContainer dest = facedPath.getTargetTE()
                        .getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, facedPath.oppositeFacing());
                if (dest == null || dest == this) continue;
                if (!dest.inputsEnergy(facedPath.oppositeFacing()) || dest.getEnergyCanBeInserted() <= 0) continue;

                List<NetNode<Insulation, WireProperties, NetFlowEdge>> nodeList = facedPath.getNodeList();
                DoubleList voltageCaps = new DoubleArrayList();
                long pathAmperage = availableAmperage;

                List<NetFlowEdge> edgeList = facedPath.getEdgeList();
                FlowConsumerList<Insulation, WireProperties, NetFlowEdge> flowLimitConsumers = new FlowConsumerList<>();
                for (int j = 0; j < nodeList.size(); j++) {
                    NetFlowEdge edge = j == 0 ? inputEdge : edgeList.get(j - 1);
                    NetNode<Insulation, WireProperties, NetFlowEdge> target = nodeList.get(j);
                    // amperage capping
                    long max = Math.min(pathAmperage,
                            edge.getFlowLimit(null, this.net.getGraph(), queryTick, simulator));
                    double ratio = (double) max / pathAmperage;
                    pathAmperage = max;
                    flowLimitConsumers.modifyRatios(ratio);

                    TileEntityCable tile = simulator == null ? (TileEntityCable) target.getHeldMTEUnsafe() : null;
                    flowLimitConsumers.add(edge, null, this.net.getGraph(), pathAmperage, queryTick,
                            simulator, tile != null ? amps -> {
                                tile.contributeAmperageFlow(amps);
                                tile.contributeVoltageFlow(voltage);
                            } : null);
                    // voltage loss
                    if (calculateVoltageLoss(voltage, simulator, voltageCaps, pathAmperage, target)) {
                        paths.remove(i);
                        continue mainloop;
                    }
                }
                // skip paths where we can't transfer amperage
                if (pathAmperage <= 0) continue;
                // complete transfer
                this.transfer = true;
                // actual voltage that reaches the destination is the geometric mean of the input and all the caps
                long finalVoltage = (long) GTUtility.geometricMean((double) voltage,
                        voltageCaps.toArray(new double[] {}));
                long accepted = dest.acceptEnergyFromNetwork(facedPath.oppositeFacing(), finalVoltage, pathAmperage,
                        simulator != null);
                this.transfer = false;
                if (simulator != null)
                    accepted = getSimulatedAccepted(destSimulationCache, facedPath.toFacingPos(), accepted);
                flowLimitConsumers.doConsumption((double) accepted / pathAmperage);
                availableAmperage -= accepted;

                if (availableAmperage <= 0) return amperage;
            }
        }
        return amperage - availableAmperage;
    }

    private long distributionIgnoreCapacity(EnumFacing side, long voltage, long amperage, long queryTick,
                                            List<NetPath<Insulation, WireProperties, NetFlowEdge>> paths,
                                            AbstractNetFlowEdge.ChannelSimulatorKey simulator) {
        Object2LongOpenHashMap<FacingPos> localDestSimulationCache = new Object2LongOpenHashMap<>();

        long availableAmperage = amperage;
        mainloop:
        for (NetPath<Insulation, WireProperties, NetFlowEdge> path : paths) {
            // skip paths where loss exceeds available voltage
            if (path.getWeight() > voltage) continue;
            Iterator<EnumFacing> iterator = path.getFacingIterator();
            boolean pathDestThis = path.getTargetNode().getNodePos().equals(this.cable.getPipePos());
            while (iterator.hasNext()) {
                NetPath.FacedNetPath<Insulation, WireProperties, NetFlowEdge> facedPath = path
                        .withFacing(iterator.next());
                if (pathDestThis && facedPath.facing == side) {
                    // do not distribute power back into our source
                    continue;
                }

                IEnergyContainer dest = facedPath.getTargetTE()
                        .getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, facedPath.oppositeFacing());
                if (dest == null || dest == this) continue;
                if (!dest.inputsEnergy(facedPath.oppositeFacing()) || dest.getEnergyCanBeInserted() <= 0) continue;

                List<NetNode<Insulation, WireProperties, NetFlowEdge>> nodeList = facedPath.getNodeList();
                DoubleList voltageCaps = new DoubleArrayList();
                long pathAmperage = availableAmperage;

                List<NetFlowEdge> edgeList = facedPath.getEdgeList();
                List<Consumer<Double>> flowLimitConsumers = new ObjectArrayList<>();
                List<Consumer<Double>> amperageLossHeat = new ObjectArrayList<>();
                for (int j = 0; j < nodeList.size(); j++) {
                    NetFlowEdge edge = j == 0 ? inputEdge : edgeList.get(j - 1);
                    NetNode<Insulation, WireProperties, NetFlowEdge> target = nodeList.get(j);

                    long flowLimit = edge.getFlowLimit(null, this.net.getGraph(), queryTick, simulator);
                    long finalPathAmperage = pathAmperage;
                    if (simulator == null && finalPathAmperage != 0) {
                        amperageLossHeat.add((ratio) -> {
                            long adjustedAmperage = (long) (finalPathAmperage * ratio);
                            if (adjustedAmperage > flowLimit) {
                                int heat = calculateHeat(adjustedAmperage - flowLimit, adjustedAmperage, 1);
                                getOrGenerateLossResult(target, heat, false);
                            }
                        });
                    }

                    TileEntityCable tile = simulator == null ? (TileEntityCable) target.getHeldMTEUnsafe() : null;
                    flowLimitConsumers.add((ratio) -> {
                        long consumption = Math.min((long) (finalPathAmperage * ratio), flowLimit);
                        if (consumption == 0) return;
                        edge.consumeFlowLimit(null, getNet().getGraph(), consumption, queryTick, simulator);
                        if (tile != null) {
                            tile.contributeAmperageFlow(consumption);
                            tile.contributeVoltageFlow(voltage);
                        }
                    });
                    pathAmperage = Math.min(pathAmperage, flowLimit);
                    if (calculateVoltageLoss(voltage, simulator, voltageCaps, pathAmperage, target)) continue mainloop;
                }

                long finalVoltage = (long) GTUtility.geometricMean((double) voltage,
                        voltageCaps.toArray(new double[] {}));
                long simulatedAccepted = dest.acceptEnergyFromNetwork(facedPath.oppositeFacing(), finalVoltage,
                        availableAmperage, true);
                simulatedAccepted = getSimulatedAccepted(localDestSimulationCache, facedPath.toFacingPos(),
                        simulatedAccepted);
                if (simulator != null)
                    simulatedAccepted = getSimulatedAccepted(destSimulationCache, facedPath.toFacingPos(),
                            simulatedAccepted);
                double ratio = (double) simulatedAccepted / availableAmperage;
                flowLimitConsumers.forEach(consumer -> consumer.accept(ratio));
                amperageLossHeat.forEach(consumer -> consumer.accept(ratio));
                availableAmperage -= simulatedAccepted;

                if (availableAmperage <= 0) return amperage;
            }
        }
        return amperage - availableAmperage;
    }

    private long getSimulatedAccepted(Object2LongOpenHashMap<FacingPos> destSimulationCache,
                                      FacingPos facingPos,
                                      long accepted) {
        AtomicLong atomicAccepted = new AtomicLong(accepted);
        destSimulationCache.compute(facingPos, (k, v) -> {
            if (v == null) return atomicAccepted.get();
            atomicAccepted.set(Math.max(atomicAccepted.get() - v, 0));
            return v + atomicAccepted.get();
        });
        return atomicAccepted.get();
    }

    private boolean calculateVoltageLoss(long voltage, AbstractNetFlowEdge.ChannelSimulatorKey simulator,
                                         DoubleList voltageCaps, long pathAmperage,
                                         NetNode<Insulation, WireProperties, NetFlowEdge> target) {
        // TODO undo loss & heating on backflow
        if (target.getData().getVoltage() < voltage) {
            int heat = calculateHeat(pathAmperage, voltage, target.getData().getVoltage());
            NodeLossResult lossResult = getOrGenerateLossResult(target, heat, simulator != null);

            if (lossResult.getLossFunction() == 0) {
                // stop this path, a cable burned
                return true;
            }
            voltageCaps.add(target.getData().getVoltage());
        }
        return false;
    }

    private int calculateHeat(long exceedingAmperage, long voltage, long maxVoltage) {
        return (int) (exceedingAmperage * (Math.log1p(Math.log((double) voltage / maxVoltage)) * 45 + 36.5));
    }

    @Override
    public long getInputAmperage() {
        return cable.getNodeData().getAmperage();
    }

    @Override
    public long getInputVoltage() {
        return cable.getNodeData().getVoltage();
    }

    @Override
    public long getEnergyCapacity() {
        return getInputVoltage() * getInputAmperage();
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        GTLog.logger.fatal("Do not use changeEnergy() for cables! Use acceptEnergyFromNetwork()");
        return acceptEnergyFromNetwork(facing == null ? EnumFacing.UP : facing,
                energyToAdd / getInputAmperage(),
                energyToAdd / getInputVoltage()) * getInputVoltage();
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }

    protected NodeLossResult getOrGenerateLossResult(NetNode<Insulation, WireProperties, NetFlowEdge> node,
                                                     int heat, boolean simulate) {
        var cachedResult = this.lossResultCache.get(node);
        if (cachedResult == null) {
            cachedResult = ((TileEntityCable) node.getHeldMTE()).applyHeat(heat, simulate);
            if (cachedResult.getLossFunction() != 0) return cachedResult;
            else this.lossResultCache.put(node, cachedResult);
        }
        return cachedResult;
    }
}
