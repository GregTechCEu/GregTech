package gregtech.common.pipelike.net.energy;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.NodeLossResult;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.api.graphnet.traverse.AbstractTraverseData;

import gregtech.api.util.GTUtility;

import gregtech.common.pipelikeold.cable.net.EnergyGroupData;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Supplier;

public class EnergyTraverseData extends AbstractTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    private final Object2ObjectOpenHashMap<WorldPipeNetNode, NodeLossResult> lossCache;

    private final Object2ObjectOpenHashMap<WorldPipeNetNode, OverVoltageInformation> overVoltageInformation;

    private final long startVoltage;
    private long pathVoltage;

    private final BlockPos sourcePos;
    private final EnumFacing inputFacing;

    public EnergyTraverseData(IGraphNet net, IPredicateTestObject testObject, SimulatorKey simulator, long queryTick,
                              long startVoltage, BlockPos sourcePos, EnumFacing inputFacing) {
        super(net, testObject, simulator, queryTick);
        this.lossCache = new Object2ObjectOpenHashMap<>();
        this.overVoltageInformation = new Object2ObjectOpenHashMap<>();
        this.startVoltage = startVoltage;
        this.sourcePos = sourcePos;
        this.inputFacing = inputFacing;
    }

    @Override
    public boolean prepareForPathWalk(FlowWorldPipeNetPath path) {
        this.pathVoltage = startVoltage;
        this.overVoltageInformation.clear();
        this.overVoltageInformation.trim(10);
        return false;
    }

    @Override
    public long traverseToNode(WorldPipeNetNode node, long flowReachingNode) {
        VoltageLimitLogic limitLogic = node.getData().getLogicEntryNullable(VoltageLimitLogic.INSTANCE);
        if (limitLogic != null) {
            long voltage = limitLogic.getValue();
            if (voltage < pathVoltage) overVoltageInformation.put(node,
                    new OverVoltageInformation(voltage, flowReachingNode));
        }


        NodeLossResult result = lossCache.get(node);
        if (result != null) {
            return result.applyLossFunction(flowReachingNode);
        } else {
            TemperatureLogic temperatureLogic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
            result = temperatureLogic == null ? null : temperatureLogic.getLossResult(getQueryTick());
            if (result == null) {
                if (node.getData().getLogicEntryDefaultable(SuperconductorLogic.INSTANCE)
                        .canSuperconduct(temperatureLogic == null ? TemperatureLogic.DEFAULT_TEMPERATURE :
                                temperatureLogic.getTemperature(getQueryTick()))) {
                    return flowReachingNode;
                }
                return flowReachingNode - node.getData().getLogicEntryDefaultable(LossAbsoluteLogic.INSTANCE).getValue();
            }
            if (result.hasPostAction()) lossCache.put(node, result);
            return result.applyLossFunction(flowReachingNode);
        }
    }

    public void handleOverflow(WorldPipeNetNode node, long overflow) {
        TemperatureLogic logic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
        if (logic != null) {
            // this occurs after finalization but before path reset.
            logic.applyThermalEnergy(calculateHeatA(overflow, pathVoltage), getQueryTick());
        }
    }

    @Override
    public long finalizeAtDestination(WorldPipeNetNode destination, long flowReachingDestination) {
        this.pathVoltage = (long) GTUtility.geometricMean(pathVoltage,
                overVoltageInformation.values().stream().mapToDouble(o -> (double) o.voltageCap).toArray());
        overVoltageInformation.forEach((k, v) -> v.doHeating(k, pathVoltage, getQueryTick()));
        long availableFlow = flowReachingDestination;
        for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (GTUtility.arePosEqual(destination.getEquivalencyData(), sourcePos) &&
                    capability.getKey() == inputFacing) continue; // anti insert-to-our-source logic

            IEnergyContainer container = capability.getValue()
                    .getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, capability.getKey().getOpposite());
            if (container != null) {
                availableFlow -= container.acceptEnergyFromNetwork(capability.getKey(), pathVoltage, availableFlow,
                        getSimulatorKey() != null);
            }
        }
        long accepted = flowReachingDestination - availableFlow;
        if (getSimulatorKey() == null && destination.getGroupUnsafe() != null &&
                destination.getGroupSafe().getData() instanceof EnergyGroupData data) {
            data.addEnergyOutPerSec(accepted * pathVoltage, getQueryTick());
        }
        return accepted;
    }

    public void runPostActions() {
        lossCache.forEach((k, v) -> v.triggerPostAction(k));
    }

    private static int calculateHeatV(long amperage, long voltage, long maxVoltage) {
        return (int) (amperage * (Math.log1p(Math.log((double) voltage / maxVoltage)) * 45 + 36.5));
    }

    private static int calculateHeatA(long amperage, long voltage) {
        return (int) (amperage * (Math.log1p(Math.log((double) voltage)) * 45 + 36.5));
    }

    protected static class OverVoltageInformation implements Supplier<Long> {

        public final long voltageCap;

        private final long amperage;

        public OverVoltageInformation(long voltageCap, long amperage) {
            this.voltageCap = voltageCap;
            this.amperage = amperage;
        }

        @Override
        public Long get() {
            return voltageCap;
        }

        public void doHeating(WorldPipeNetNode node, long finalVoltage, long tick) {
            TemperatureLogic logic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
            if (logic != null) {
                logic.applyThermalEnergy(calculateHeatV(amperage, finalVoltage, voltageCap), tick);
            }
        }

    }
}
