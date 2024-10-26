package gregtech.common.pipelike.net.fluid;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.traverse.SimpleTileRoundRobinData;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverse.IRoundRobinTraverseData;
import gregtech.api.util.GTUtility;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class FluidRRTraverseData extends FluidTraverseData implements
                                 IRoundRobinTraverseData<SimpleTileRoundRobinData<IFluidHandler>, WorldPipeNetNode, FlowWorldPipeNetPath> {

    private final Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IFluidHandler>> cache;

    public FluidRRTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator, long queryTick,
                               BlockPos sourcePos, EnumFacing inputFacing,
                               @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IFluidHandler>> cache) {
        super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        this.cache = cache;
    }

    @Override
    public @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IFluidHandler>> getTraversalCache() {
        return cache;
    }

    @Override
    public boolean shouldSkipPath(@NotNull FlowWorldPipeNetPath path) {
        return false;
    }

    @Override
    public @NotNull SimpleTileRoundRobinData<IFluidHandler> createRRData(@NotNull WorldPipeNetNode destination) {
        return new SimpleTileRoundRobinData<>(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
    }

    @Override
    public long finalizeAtDestination(@NotNull SimpleTileRoundRobinData<IFluidHandler> data,
                                      @NotNull WorldPipeNetNode destination, long flowReachingDestination) {
        long availableFlow = flowReachingDestination;
        EnumFacing pointerFacing = data.getPointerFacing(getSimulatorKey());
        // anti insert-to-our-source logic
        if (!GTUtility.arePosEqual(destination.getEquivalencyData(), sourcePos) || !(pointerFacing == inputFacing)) {
            IFluidHandler container = data.getAtPointer(destination, getSimulatorKey());
            if (container != null) {
                availableFlow -= IFluidTransferController.CONTROL.get(destination.getTileEntity().getCoverHolder()
                        .getCoverAtSide(pointerFacing)).insertToHandler(getTestObject(),
                                (int) Math.min(Integer.MAX_VALUE, availableFlow), container, !simulating());
            }
        }
        return flowReachingDestination - availableFlow;
    }
}
