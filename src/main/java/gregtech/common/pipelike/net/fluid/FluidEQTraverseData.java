package gregtech.common.pipelike.net.fluid;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverse.IEqualizableTraverseData;
import gregtech.api.util.GTUtility;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

public class FluidEQTraverseData extends FluidTraverseData
                                 implements IEqualizableTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    protected int destCount;
    protected int maxMinFlow;

    public FluidEQTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator, long queryTick,
                               BlockPos sourcePos, EnumFacing inputFacing) {
        super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    protected void compute(@NotNull WorldPipeNetNode destination) {
        this.destCount = 0;
        this.maxMinFlow = 0;
        for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (GTUtility.arePosEqual(destination.getEquivalencyData(), sourcePos) &&
                    capability.getKey() == inputFacing)
                continue; // anti insert-to-our-source logic

            IFluidHandler container = capability.getValue()
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, capability.getKey().getOpposite());
            if (container != null) {
                if (destCount == 0) maxMinFlow = Integer.MAX_VALUE;
                destCount += 1;
                maxMinFlow = Math.min(maxMinFlow,
                        IFluidTransferController.CONTROL.get(destination.getTileEntity().getCoverHolder()
                                .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                        Integer.MAX_VALUE,
                                        container, false));
            }
        }
    }

    @Override
    public int getDestinationsAtNode(@NotNull WorldPipeNetNode node) {
        return destCount;
    }

    @Override
    public boolean shouldSkipPath(@NotNull FlowWorldPipeNetPath path) {
        compute(path.getTargetNode());
        return maxMinFlow == 0;
    }

    @Override
    public long getMaxFlowToLeastDestination(@NotNull WorldPipeNetNode destination) {
        return maxMinFlow;
    }

    @Override
    public long finalizeAtDestination(@NotNull WorldPipeNetNode node, long flowReachingNode, int expectedDestinations) {
        long availableFlow = flowReachingNode;
        long flowPerDestination = flowReachingNode / expectedDestinations;
        if (flowPerDestination == 0) return 0;
        for (var capability : node.getTileEntity().getTargetsWithCapabilities(node).entrySet()) {
            if (GTUtility.arePosEqual(node.getEquivalencyData(), sourcePos) &&
                    capability.getKey() == inputFacing)
                continue; // anti insert-to-our-source logic

            IFluidHandler container = capability.getValue()
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, capability.getKey().getOpposite());
            if (container != null) {
                availableFlow -= IFluidTransferController.CONTROL.get(node.getTileEntity().getCoverHolder()
                        .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                (int) Math.min(Integer.MAX_VALUE, flowPerDestination), container, !simulating());
            }
        }
        return flowReachingNode - availableFlow;
    }
}
