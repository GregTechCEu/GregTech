package gregtech.common.pipelike.net.item;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.traverse.SimpleTileRoundRobinData;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverse.IRoundRobinTraverseData;
import gregtech.api.util.GTUtility;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class ItemRRTraverseData extends ItemTraverseData implements
                                IRoundRobinTraverseData<SimpleTileRoundRobinData<IItemHandler>, WorldPipeNetNode, FlowWorldPipeNetPath> {

    private final Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> cache;

    public ItemRRTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                              BlockPos sourcePos, EnumFacing inputFacing,
                              @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> cache) {
        super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        this.cache = cache;
    }

    @Override
    public @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> getTraversalCache() {
        return cache;
    }

    @Override
    public boolean shouldSkipPath(@NotNull FlowWorldPipeNetPath path) {
        return false;
    }

    @Override
    public @NotNull SimpleTileRoundRobinData<IItemHandler> createRRData(@NotNull WorldPipeNetNode destination) {
        return new SimpleTileRoundRobinData<>(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    @Override
    public long finalizeAtDestination(@NotNull SimpleTileRoundRobinData<IItemHandler> data,
                                      @NotNull WorldPipeNetNode destination,
                                      long flowReachingDestination) {
        long availableFlow = flowReachingDestination;
        EnumFacing pointerFacing = data.getPointerFacing(getSimulatorKey());
        if (GTUtility.arePosEqual(destination.getEquivalencyData(), sourcePos) && pointerFacing == inputFacing)
            return 0; // anti insert-to-our-source logic

        IItemHandler container = data.getAtPointer(destination, getSimulatorKey());
        if (container != null) {
            availableFlow = IItemTransferController.CONTROL.get(destination.getTileEntity().getCoverHolder()
                    .getCoverAtSide(pointerFacing)).insertToHandler(getTestObject(),
                            (int) Math.min(Integer.MAX_VALUE, availableFlow), container, simulating());
        }
        return flowReachingDestination - availableFlow;
    }
}
