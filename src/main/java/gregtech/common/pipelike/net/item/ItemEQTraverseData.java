package gregtech.common.pipelike.net.item;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverseold.IEqualizableTraverseData;
import gregtech.api.util.GTUtility;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

public class ItemEQTraverseData extends ItemTraverseData
                                implements IEqualizableTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    protected int destCount;
    protected int maxMinFlow;

    public ItemEQTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
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

            IItemHandler container = capability.getValue()
                    .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, capability.getKey().getOpposite());
            if (container != null) {
                if (destCount == 0) maxMinFlow = Integer.MAX_VALUE;
                destCount += 1;
                int test = Integer.MAX_VALUE;
                maxMinFlow = Math.min(maxMinFlow, test -
                        IItemTransferController.CONTROL.get(destination.getTileEntity().getCoverHolder()
                                .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(), test,
                                        container, true));
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

            IItemHandler container = capability.getValue()
                    .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, capability.getKey().getOpposite());
            if (container != null) {
                availableFlow = IItemTransferController.CONTROL.get(node.getTileEntity().getCoverHolder()
                        .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                (int) Math.min(Integer.MAX_VALUE, flowPerDestination), container, simulating());
            }
        }
        return flowReachingNode - availableFlow;
    }
}
