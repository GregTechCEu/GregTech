package gregtech.common.pipelike.net.energy;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.NodeLossCache;
import gregtech.api.graphnet.pipenet.NodeLossResult;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.traverse.FlowManagerMap;
import gregtech.api.graphnet.pipenet.traverse.ITileFlowManager;
import gregtech.api.graphnet.pipenet.traverse.LocalTransferInformation;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.api.graphnet.traverse.AbstractTraverseData;
import gregtech.api.graphnet.traverse.util.ReversibleLossOperator;
import gregtech.api.util.GTUtility;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class EnergyTraverseData extends AbstractTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    private final Object2ObjectOpenHashMap<WorldPipeNetNode, OverVoltageInformation> overVoltageInformation;

    protected final FlowManagerMap managers = new FlowManagerMap(EnergyFlowManager::new);

    private final long startVoltage;
    private long pathVoltage;
    private long bufferOverflow;

    private final BlockPos sourcePos;
    private final EnumFacing inputFacing;

    public EnergyTraverseData(IGraphNet net, IPredicateTestObject testObject, SimulatorKey simulator, long queryTick,
                              long startVoltage, BlockPos sourcePos, EnumFacing inputFacing, long bufferOverflow) {
        super(net, testObject, simulator, queryTick);
        this.overVoltageInformation = new Object2ObjectOpenHashMap<>();
        this.startVoltage = startVoltage;
        this.sourcePos = sourcePos;
        this.inputFacing = inputFacing;
        this.bufferOverflow = bufferOverflow;
    }

    @Override
    public boolean prepareForPathWalk(@NotNull FlowWorldPipeNetPath path, long flow) {
        resetPathVoltage();
        if (flow <= 0 || !managers.access(path.getTargetNode()).canAcceptFlow()) return true;
        managers.access(path.getTargetNode()).reportAttemptingFlow(flow);
        this.overVoltageInformation.clear();
        this.overVoltageInformation.trim(10);
        return false;
    }

    public void resetPathVoltage() {
        this.pathVoltage = startVoltage;
    }

    @Override
    public ReversibleLossOperator traverseToNode(@NotNull WorldPipeNetNode node, long flowReachingNode) {
        VoltageLimitLogic limitLogic = node.getData().getLogicEntryNullable(VoltageLimitLogic.INSTANCE);
        if (limitLogic != null) {
            long voltage = limitLogic.getValue();
            if (voltage < pathVoltage) overVoltageInformation.put(node, new OverVoltageInformation(voltage));
        }
        TemperatureLogic temperatureLogic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
        if (!node.getData().getLogicEntryDefaultable(SuperconductorLogic.INSTANCE)
                .canSuperconduct(temperatureLogic == null ? TemperatureLogic.DEFAULT_TEMPERATURE :
                        temperatureLogic.getTemperature(getQueryTick()))) {
            pathVoltage -= node.getData().getLogicEntryDefaultable(VoltageLossLogic.INSTANCE).getValue();
        }

        NodeLossCache.Key key = NodeLossCache.key(node, this);
        NodeLossResult result = NodeLossCache.getLossResult(key, simulating());
        if (result != null) {
            return result.getLossFunction();
        } else {
            result = temperatureLogic == null ? null : temperatureLogic.getLossResult(getQueryTick());
            if (result == null) {
                return ReversibleLossOperator.IDENTITY;
            }
            if (result.hasPostAction()) NodeLossCache.registerLossResult(key, result, simulating());
            return result.getLossFunction();
        }
    }

    public void handleOverflow(@NotNull WorldPipeNetNode node, long overflow) {
        if (bufferOverflow > 0) {
            long difference = bufferOverflow - overflow;
            if (difference < 0) {
                overflow -= bufferOverflow;
                bufferOverflow = 0;
            } else {
                bufferOverflow -= overflow;
                return;
            }
        }
        if (overflow > 0) {
            TemperatureLogic logic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
            if (logic != null) {
                // this occurs after finalization but before path reset.
                logic.applyThermalEnergy(calculateHeatA(overflow, pathVoltage), getQueryTick());
            }
        }
    }

    public long calculateActualBufferOverflow(long startingOverflow) {
        return startingOverflow - bufferOverflow;
    }

    @Override
    public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
        this.pathVoltage = (long) GTUtility.geometricMean(pathVoltage,
                overVoltageInformation.values().stream().filter(o -> o.voltageCap < this.pathVoltage)
                        .mapToDouble(o -> (double) o.voltageCap).toArray());
        long accepted = managers.access(destination).acceptFlow(flowReachingDestination);
        if (!simulating() && destination.getGroupUnsafe() != null &&
                destination.getGroupSafe().getData() instanceof EnergyGroupData data) {
            data.addEnergyOutPerSec(accepted * pathVoltage, getQueryTick());
        }
        return accepted;
    }

    @Override
    public void consumeFlowLimit(@NotNull AbstractNetFlowEdge edge, NetNode targetNode,
                                 long consumption) {
        super.consumeFlowLimit(edge, targetNode, consumption);
        if (consumption > 0 && !simulating()) {
            recordFlow(targetNode, consumption);
            OverVoltageInformation info = overVoltageInformation.get((WorldPipeNetNode) targetNode);
            if (info != null) info.doHeating((WorldPipeNetNode) targetNode, pathVoltage, getQueryTick(), consumption);
        }
    }

    private void recordFlow(@NotNull NetNode node, long amperes) {
        EnergyFlowLogic logic = node.getData().getLogicEntryNullable(EnergyFlowLogic.INSTANCE);
        if (logic == null) {
            logic = EnergyFlowLogic.INSTANCE.getNew();
            node.getData().setLogicEntry(logic);
        }
        logic.recordFlow(getQueryTick(), new EnergyFlowData(amperes, pathVoltage));
    }

    private static int calculateHeatV(long amperage, long voltage, long maxVoltage) {
        return (int) (amperage * (Math.log1p((double) voltage / maxVoltage) * 85 + 36));
    }

    private static int calculateHeatA(long amperage, long voltage) {
        return (int) (amperage * (Math.log1p(Math.log(voltage)) * 85 + 36));
    }

    protected static class OverVoltageInformation {

        public final long voltageCap;

        public OverVoltageInformation(long voltageCap) {
            this.voltageCap = voltageCap;
        }

        public void doHeating(WorldPipeNetNode node, long finalVoltage, long tick, long amperage) {
            TemperatureLogic logic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
            if (logic != null) {
                logic.applyThermalEnergy(calculateHeatV(amperage, finalVoltage, voltageCap), tick);
            }
        }
    }

    protected class EnergyFlowManager extends
                                      Object2LongOpenHashMap<LocalTransferInformation<IEnergyTransferController, IEnergyContainer>>
                                      implements ITileFlowManager {

        public EnergyFlowManager(@NotNull WorldPipeNetNode node) {
            for (var capability : node.getTileEntity().getTargetsWithCapabilities(node).entrySet()) {
                if (GTUtility.arePosEqual(node.getEquivalencyData(), sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                IEnergyContainer container = capability.getValue()
                        .getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER,
                                capability.getKey().getOpposite());
                if (container != null) {
                    IEnergyTransferController controller = IEnergyTransferController.CONTROL.get(node.getTileEntity()
                            .getCoverHolder().getCoverAtSide(capability.getKey()));
                    this.put(new LocalTransferInformation<>(capability.getKey(), controller, container),
                            controller.insertToHandler(pathVoltage, Long.MAX_VALUE, container, capability.getKey(),
                                    true));
                }
            }
        }

        @Override
        public long getMaximumFlow() {
            long sum = 0;
            for (long l : this.values()) {
                sum += l;
            }
            return sum;
        }

        @Override
        public void reportAttemptingFlow(long flow) {
            for (var entry : this.entrySet()) {
                entry.setValue(Math.max(entry.getValue() - flow, 0));
            }
        }

        @Override
        public long acceptFlow(long flow) {
            long availableFlow = flow;
            var iter = this.entrySet().iterator();
            while (iter.hasNext()) {
                var entry = iter.next();
                var info = entry.getKey();
                long accepted = info.controller().insertToHandler(pathVoltage, availableFlow, info.container(),
                        info.facing(), simulating());
                if (entry.getValue() == 0) iter.remove();
                availableFlow -= accepted;
            }
            return flow - availableFlow;
        }
    }
}
