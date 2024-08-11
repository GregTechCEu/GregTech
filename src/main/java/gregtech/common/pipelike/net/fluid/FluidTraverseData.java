package gregtech.common.pipelike.net.fluid;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.NodeLossCache;
import gregtech.api.graphnet.pipenet.NodeLossResult;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverse.AbstractTraverseData;
import gregtech.api.graphnet.traverse.util.MultLossOperator;
import gregtech.api.graphnet.traverse.util.ReversibleLossOperator;
import gregtech.api.util.EntityDamageUtil;
import gregtech.api.util.GTUtility;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongConsumer;

public class FluidTraverseData extends AbstractTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    public static final float TEMPERATURE_EFFECT = 0.005f;

    static {
        ContainmentFailure.init();
    }

    protected final BlockPos sourcePos;
    protected final EnumFacing inputFacing;

    protected final Object2ObjectOpenHashMap<NetNode, LongConsumer> temperatureUpdates = new Object2ObjectOpenHashMap<>();

    public FluidTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator, long queryTick,
                             BlockPos sourcePos, EnumFacing inputFacing) {
        super(net, testObject, simulator, queryTick);
        this.sourcePos = sourcePos;
        this.inputFacing = inputFacing;
    }

    @Override
    public FluidTestObject getTestObject() {
        return (FluidTestObject) super.getTestObject();
    }

    @Override
    public boolean prepareForPathWalk(@NotNull FlowWorldPipeNetPath path, long flow) {
        if (flow <= 0) return true;
        temperatureUpdates.clear();
        temperatureUpdates.trim(16);
        return false;
    }

    @Override
    public ReversibleLossOperator traverseToNode(@NotNull WorldPipeNetNode node, long flowReachingNode) {
        NodeLossCache.Key key = NodeLossCache.key(node, this);
        NodeLossResult result = NodeLossCache.getLossResult(key);
        if (result != null) {
            return result.getLossFunction();
        } else {
            FluidStack stack = getTestObject().recombine();
            FluidContainmentLogic containmentLogic = node.getData()
                    .getLogicEntryDefaultable(FluidContainmentLogic.INSTANCE);
            FluidState state = FluidState.inferState(stack);

            TemperatureLogic temperatureLogic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
            if (temperatureLogic != null) {
                result = temperatureLogic.getLossResult(getQueryTick());
                int fluidTemp = stack.getFluid().getTemperature(stack);
                boolean gaseous = stack.getFluid().isGaseous(stack);
                // prevent plasmas from melting valid pipes due to raw temperature
                boolean temperatureSafe = state == FluidState.PLASMA && containmentLogic.contains(FluidState.PLASMA);
                temperatureUpdates.put(node, l -> temperatureLogic.moveTowardsTemperature(fluidTemp, getQueryTick(),
                        l * TEMPERATURE_EFFECT, temperatureSafe));
                if (temperatureLogic.isUnderMinimum(fluidTemp)) {
                    result = NodeLossResult.combine(result, new NodeLossResult(pipe -> {
                        IWorldPipeNetTile tile = pipe.getTileEntityNoLoading();
                        if (tile != null) {
                            tile.playLossSound();
                            tile.spawnParticles(EnumFacing.UP, EnumParticleTypes.CLOUD, 3 + GTValues.RNG.nextInt(2));
                            tile.dealAreaDamage(gaseous ? 2 : 1,
                                    entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                            fluidTemp, 2.0F, 10));
                        }
                    }, MultLossOperator.EIGHTHS[2]));
                } else if (temperatureLogic.isOverMaximum(fluidTemp)) {
                    result = NodeLossResult.combine(result, new NodeLossResult(GTValues.RNG.nextInt(4) == 0 ? pipe -> {
                        IWorldPipeNetTile tile = pipe.getTileEntityNoLoading();
                        if (tile != null) {
                            tile.playLossSound();
                            tile.spawnParticles(EnumFacing.UP, EnumParticleTypes.CLOUD, 3 + GTValues.RNG.nextInt(2));
                            tile.dealAreaDamage(gaseous ? 2 : 1,
                                    entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                            fluidTemp, 2.0F, 10));
                            tile.setNeighborsToFire();
                        }
                    } : pipe -> {
                        IWorldPipeNetTile tile = pipe.getTileEntityNoLoading();
                        if (tile != null) {
                            tile.playLossSound();
                            tile.spawnParticles(EnumFacing.UP, EnumParticleTypes.CLOUD, 3 + GTValues.RNG.nextInt(2));
                            tile.dealAreaDamage(gaseous ? 2 : 1,
                                    entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                            fluidTemp, 2.0F, 10));
                        }
                    }, MultLossOperator.EIGHTHS[2]));
                }
            }

            if (!containmentLogic.contains(state)) {
                result = NodeLossResult.combine(result, ContainmentFailure.getFailure(state).computeLossResult(stack));
            }

            if (stack instanceof AttributedFluid fluid) {
                for (FluidAttribute attribute : fluid.getAttributes()) {
                    if (!containmentLogic.contains(attribute)) {
                        result = NodeLossResult.combine(result,
                                ContainmentFailure.getFailure(attribute).computeLossResult(stack));
                    }
                }
            }

            if (result == null) return ReversibleLossOperator.IDENTITY;
            NodeLossCache.registerLossResult(key, result);
            return result.getLossFunction();
        }
    }

    @Override
    public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
        long availableFlow = flowReachingDestination;
        for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (GTUtility.arePosEqual(destination.getEquivalencyData(), sourcePos) &&
                    capability.getKey() == inputFacing)
                continue; // anti insert-to-our-source logic

            IFluidHandler container = capability.getValue()
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, capability.getKey().getOpposite());
            if (container != null) {
                availableFlow -= IFluidTransferController.CONTROL.get(destination.getTileEntity().getCoverHolder()
                        .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                (int) Math.min(Integer.MAX_VALUE, availableFlow), container, getSimulatorKey() == null);
            }
        }
        return flowReachingDestination - availableFlow;
    }

    @Override
    public void consumeFlowLimit(@NotNull AbstractNetFlowEdge edge, NetNode targetNode, long consumption) {
        super.consumeFlowLimit(edge, targetNode, consumption);
        temperatureUpdates.getOrDefault(targetNode, l -> {}).accept(consumption);
    }
}
