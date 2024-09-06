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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class FluidTraverseData extends AbstractTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    public static final float TEMPERATURE_EFFECT = 0.05f;

    static {
        ContainmentFailure.init();
    }

    protected final BlockPos sourcePos;
    protected final EnumFacing inputFacing;

    protected final FluidStack stack;
    protected final FluidState state;
    protected final int fluidTemp;
    protected final boolean gaseous;
    protected final @Nullable Collection<FluidAttribute> attributes;

    public FluidTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator, long queryTick,
                             BlockPos sourcePos, EnumFacing inputFacing) {
        super(net, testObject, simulator, queryTick);
        this.sourcePos = sourcePos;
        this.inputFacing = inputFacing;
        this.stack = testObject.recombine();
        this.state = FluidState.inferState(stack);
        this.fluidTemp = stack.getFluid().getTemperature(stack);
        this.gaseous = stack.getFluid().isGaseous(stack);
        if (stack.getFluid() instanceof AttributedFluid at) {
            attributes = at.getAttributes();
        } else attributes = null;
    }

    @Override
    public FluidTestObject getTestObject() {
        return (FluidTestObject) super.getTestObject();
    }

    @Override
    public boolean prepareForPathWalk(@NotNull FlowWorldPipeNetPath path, long flow) {
        return flow <= 0;
    }

    @Override
    public ReversibleLossOperator traverseToNode(@NotNull WorldPipeNetNode node, long flowReachingNode) {
        NodeLossCache.Key key = NodeLossCache.key(node, this);
        NodeLossResult result = NodeLossCache.getLossResult(key, simulating());
        if (result != null) {
            return result.getLossFunction();
        } else {
            FluidContainmentLogic containmentLogic = node.getData()
                    .getLogicEntryDefaultable(FluidContainmentLogic.INSTANCE);

            TemperatureLogic temperatureLogic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
            if (temperatureLogic != null) {
                result = temperatureLogic.getLossResult(getQueryTick());
                boolean overMax = fluidTemp > containmentLogic.getMaximumTemperature() &&
                        !(state == FluidState.PLASMA && containmentLogic.contains(FluidState.PLASMA));
                if (overMax) {
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
                } else if (temperatureLogic.isUnderMinimum(fluidTemp)) {
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
                }
            }

            if (!containmentLogic.contains(state)) {
                result = NodeLossResult.combine(result, ContainmentFailure.getFailure(state).computeLossResult(stack));
            }

            if (attributes != null) {
                for (FluidAttribute attribute : attributes) {
                    if (!containmentLogic.contains(attribute)) {
                        result = NodeLossResult.combine(result,
                                ContainmentFailure.getFailure(attribute).computeLossResult(stack));
                    }
                }
            }

            if (result == null) return ReversibleLossOperator.IDENTITY;
            NodeLossCache.registerLossResult(key, result, simulating());
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
                                (int) Math.min(Integer.MAX_VALUE, availableFlow), container, !simulating());
            }
        }
        return flowReachingDestination - availableFlow;
    }

    @Override
    public void consumeFlowLimit(@NotNull AbstractNetFlowEdge edge, NetNode targetNode,
                                 long consumption) {
        super.consumeFlowLimit(edge, targetNode, consumption);
        if (consumption > 0 && !simulating()) {
            recordFlow(targetNode, consumption);
            TemperatureLogic temperatureLogic = targetNode.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
            if (temperatureLogic != null) {
                FluidContainmentLogic containmentLogic = targetNode.getData()
                        .getLogicEntryDefaultable(FluidContainmentLogic.INSTANCE);
                boolean overMax = fluidTemp > containmentLogic.getMaximumTemperature() &&
                        !(state == FluidState.PLASMA && containmentLogic.contains(FluidState.PLASMA));
                temperatureLogic.moveTowardsTemperature(fluidTemp,
                        getQueryTick(), consumption * TEMPERATURE_EFFECT, !overMax);
            }
        }
    }

    private void recordFlow(@NotNull NetNode node, long flow) {
        FluidFlowLogic logic = node.getData().getLogicEntryNullable(FluidFlowLogic.INSTANCE);
        if (logic == null) {
            logic = FluidFlowLogic.INSTANCE.getNew();
            node.getData().setLogicEntry(logic);
        }
        logic.recordFlow(getQueryTick(), getTestObject().recombine(GTUtility.safeCastLongToInt(flow)));
    }
}
