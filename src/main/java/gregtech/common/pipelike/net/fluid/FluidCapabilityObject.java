package gregtech.common.pipelike.net.fluid;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.graphnet.GraphNetUtility;
import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.NodeExposingCapabilities;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import gregtech.api.graphnet.pipenet.physical.tile.NodeManagingPCW;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverse.EdgeDirection;
import gregtech.api.graphnet.traverse.EdgeSelector;
import gregtech.api.graphnet.traverse.ResilientNetClosestIterator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TickUtil;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public class FluidCapabilityObject implements IPipeCapabilityObject, IFluidHandler, IFluidTankProperties {

    private PipeTileEntity tile;
    private NodeManagingPCW capabilityWrapper;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);
    private final WorldPipeNode node;
    private final IFluidTankProperties[] properties;

    private boolean transferring = false;

    public FluidCapabilityObject(WorldPipeNode node) {
        this.node = node;
        properties = new IFluidTankProperties[node.getData().getLogicEntryDefaultable(ChannelCountLogic.TYPE)
                .getValue()];
        Arrays.fill(properties, this);
        for (EnumFacing facing : EnumFacing.VALUES) {
            wrappers.put(facing, new Wrapper(facing));
        }
    }

    public WorldPipeNode getNode() {
        return node;
    }

    @Override
    public void init(@NotNull PipeTileEntity tile, @NotNull PipeCapabilityWrapper wrapper) {
        this.tile = tile;
        if (!(wrapper instanceof NodeManagingPCW p))
            throw new IllegalArgumentException("FluidCapabilityObjects must be initialized to NodeManagingPCWs!");
        this.capabilityWrapper = p;
    }

    private boolean inputDisallowed(EnumFacing side) {
        if (side == null) return false;
        else return tile.isBlocked(side);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        // can't expose the sided capability if there is no node to interact with
        if (facing != null && capabilityWrapper.getNodeForFacing(facing) == null) return null;
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(facing == null ? this : wrappers.get(facing));
        }
        return null;
    }

    protected @Nullable NetNode getRelevantNode(EnumFacing facing) {
        return facing == null ? node : capabilityWrapper.getNodeForFacing(facing);
    }

    protected int fill(FluidStack resource, boolean doFill, EnumFacing side) {
        if (this.transferring || inputDisallowed(side)) return 0;
        NetNode node = getRelevantNode(side);
        if (node == null) node = this.node;
        this.transferring = true;

        int flow = resource.amount;
        FluidTestObject testObject = new FluidTestObject(resource);
        ResilientNetClosestIterator iter = new ResilientNetClosestIterator(node,
                EdgeSelector.filtered(EdgeDirection.OUTGOING, GraphNetUtility.standardEdgeBlacklist(testObject)));
        int maxPredictedSize = node.getGroupSafe().getNodes().size();
        Reference2IntOpenHashMap<NetNode> availableDemandCache = new Reference2IntOpenHashMap<>(maxPredictedSize);
        Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>(maxPredictedSize);
        Reference2BooleanOpenHashMap<NetNode> lossyCache = new Reference2BooleanOpenHashMap<>(maxPredictedSize);
        List<Runnable> postActions = new ObjectArrayList<>();
        int total = 0;
        main:
        while (iter.hasNext()) {
            if (flow <= 0) break;
            final NetNode next = iter.next();
            int limit = Math
                    .min(GraphNetUtility.computeIfAbsent(flowLimitCache, next, n -> getFlowLimit(n, testObject)), flow);
            if (limit <= 0) {
                iter.markInvalid(next);
                continue;
            }
            int supply = GraphNetUtility.computeIfAbsent(availableDemandCache, next,
                    n -> getSupplyOrDemand(n, testObject, false));
            if (supply <= 0) continue;
            supply = Math.min(supply, limit);
            NetEdge span;
            NetNode trace = next;
            ArrayDeque<NetNode> seen = new ArrayDeque<>();
            seen.add(next);
            while ((span = iter.getSpanningTreeEdge(trace)) != null) {
                trace = span.getOppositeNode(trace);
                if (trace == null) continue main;
                int l = GraphNetUtility.computeIfAbsent(flowLimitCache, trace, n -> getFlowLimit(n, testObject));
                if (l == 0) {
                    iter.markInvalid(node);
                    continue main;
                }
                supply = Math.min(supply, l);
                seen.addFirst(trace);
            }
            total += supply;
            flow -= supply;
            int finalSupply = supply;
            for (NetNode n : seen) {
                // reporting flow can cause temperature pipe destruction which causes graph modification while
                // iterating.
                if (doFill) postActions.add(() -> reportFlow(n, finalSupply, testObject));
                int remaining = flowLimitCache.getInt(n) - supply;
                flowLimitCache.put(n, remaining);
                if (remaining <= 0) {
                    iter.markInvalid(n);
                }
                if (GraphNetUtility.computeIfAbsent(lossyCache, n, a -> isLossyNode(a, testObject))) {
                    // reporting loss can cause misc pipe destruction which causes graph modification while iterating.
                    if (doFill) postActions.add(() -> handleLoss(n, finalSupply, testObject));
                    continue main;
                }
            }
            if (doFill) reportExtractedInserted(next, supply, testObject, false);
            availableDemandCache.put(next, availableDemandCache.getInt(next) - supply);
        }
        postActions.forEach(Runnable::run);
        this.transferring = false;
        return total;
    }

    protected FluidStack drain(int maxDrain, boolean doDrain, EnumFacing side) {
        FluidStack stack = getNetworkView().handler().drain(maxDrain, false);
        if (stack == null) return null;
        return drain(stack, doDrain, side);
    }

    protected FluidStack drain(FluidStack resource, boolean doDrain, EnumFacing side) {
        if (this.transferring) return null;
        NetNode node = getRelevantNode(side);
        if (node == null) node = this.node;
        this.transferring = true;

        int flow = resource.amount;
        FluidTestObject testObject = new FluidTestObject(resource);
        ResilientNetClosestIterator iter = new ResilientNetClosestIterator(node,
                EdgeSelector.filtered(EdgeDirection.INCOMING, GraphNetUtility.standardEdgeBlacklist(testObject)));
        Reference2IntOpenHashMap<NetNode> availableSupplyCache = new Reference2IntOpenHashMap<>();
        Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>();
        Reference2BooleanOpenHashMap<NetNode> lossyCache = new Reference2BooleanOpenHashMap<>();
        List<Runnable> postActions = new ObjectArrayList<>();
        int total = 0;
        main:
        while (iter.hasNext()) {
            if (flow <= 0) break;
            final NetNode next = iter.next();
            int limit = Math
                    .min(GraphNetUtility.computeIfAbsent(flowLimitCache, next, n -> getFlowLimit(n, testObject)), flow);
            if (limit <= 0) {
                iter.markInvalid(next);
                continue;
            }
            int supply = GraphNetUtility.computeIfAbsent(availableSupplyCache, next,
                    n -> getSupplyOrDemand(n, testObject, true));
            if (supply <= 0) continue;
            supply = Math.min(supply, limit);
            NetEdge span;
            NetNode trace = next;
            ArrayDeque<NetNode> seen = new ArrayDeque<>();
            seen.add(next);
            while ((span = iter.getSpanningTreeEdge(trace)) != null) {
                trace = span.getOppositeNode(trace);
                if (trace == null) continue main;
                int l = GraphNetUtility.computeIfAbsent(flowLimitCache, trace, n -> getFlowLimit(n, testObject));
                if (l == 0) {
                    iter.markInvalid(node);
                    continue main;
                }
                supply = Math.min(supply, l);
                seen.addFirst(trace);
            }
            total += supply;
            flow -= supply;
            int finalSupply = supply;
            for (NetNode n : seen) {
                // reporting flow can cause temperature pipe destruction which causes graph modification while
                // iterating.
                if (doDrain) postActions.add(() -> reportFlow(n, finalSupply, testObject));
                int remaining = flowLimitCache.getInt(n) - supply;
                flowLimitCache.put(n, remaining);
                if (remaining <= 0) {
                    iter.markInvalid(n);
                }
                if (GraphNetUtility.computeIfAbsent(lossyCache, n, a -> isLossyNode(a, testObject))) {
                    // reporting loss can cause misc pipe destruction which causes graph modification while iterating.
                    if (doDrain) postActions.add(() -> handleLoss(n, finalSupply, testObject));
                    continue main;
                }
            }
            if (doDrain) reportExtractedInserted(next, supply, testObject, true);
            availableSupplyCache.put(next, availableSupplyCache.getInt(next) - supply);
        }
        postActions.forEach(Runnable::run);
        this.transferring = false;
        return testObject.recombine(total);
    }

    public static int getFlowLimit(NetNode node, FluidTestObject testObject) {
        ThroughputLogic throughput = node.getData().getLogicEntryNullable(ThroughputLogic.TYPE);
        if (throughput == null) return Integer.MAX_VALUE;
        FluidFlowLogic history = node.getData().getLogicEntryNullable(FluidFlowLogic.TYPE);
        if (history == null) return GTUtility.safeCastLongToInt(throughput.getValue() * FluidFlowLogic.MEMORY_TICKS);
        Object2LongMap<FluidTestObject> sum = history.getSum(false);
        if (sum.isEmpty()) return GTUtility.safeCastLongToInt(throughput.getValue() * FluidFlowLogic.MEMORY_TICKS);
        if (sum.size() < node.getData().getLogicEntryDefaultable(ChannelCountLogic.TYPE).getValue() ||
                sum.containsKey(testObject)) {
            return GTUtility
                    .safeCastLongToInt(throughput.getValue() * FluidFlowLogic.MEMORY_TICKS - sum.getLong(testObject));
        }
        return 0;
    }

    public static boolean isLossyNode(NetNode node, FluidTestObject testObject) {
        FluidContainmentLogic containmentLogic = node.getData().getLogicEntryNullable(FluidContainmentLogic.TYPE);
        return containmentLogic != null && !containmentLogic.handles(testObject);
    }

    public static void reportFlow(NetNode node, int flow, FluidTestObject testObject) {
        FluidFlowLogic logic = node.getData().getLogicEntryNullable(FluidFlowLogic.TYPE);
        if (logic == null) {
            logic = FluidFlowLogic.TYPE.getNew();
            node.getData().setLogicEntry(logic);
        }
        logic.recordFlow(TickUtil.getTick(), testObject, flow);
        TemperatureLogic temp = node.getData().getLogicEntryNullable(TemperatureLogic.TYPE);
        if (temp != null) {
            FluidStack stack = testObject.recombine(flow);
            FluidContainmentLogic cont = node.getData().getLogicEntryDefaultable(FluidContainmentLogic.TYPE);
            int t = stack.getFluid().getTemperature(stack);
            temp.moveTowardsTemperature(t, TickUtil.getTick(), stack.amount, cont.getMaximumTemperature() >= t);
            if (node instanceof WorldPipeNode n) {
                temp.defaultHandleTemperature(n.getNet().getWorld(), n.getEquivalencyData());
            }
        }
    }

    public static void reportExtractedInserted(NetNode node, int flow, FluidTestObject testObject, boolean extracted) {
        if (flow == 0) return;
        if (node instanceof NodeExposingCapabilities exposer) {
            IFluidHandler handler = exposer.getProvider().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    exposer.exposedFacing());
            if (handler != null) {
                if (extracted) {
                    handler.drain(testObject.recombine(flow), true);
                } else {
                    handler.fill(testObject.recombine(flow), true);
                }
            }
        }
    }

    public static void handleLoss(NetNode node, int flow, FluidTestObject testObject) {
        if (flow == 0) return;
        FluidContainmentLogic logic = node.getData().getLogicEntryDefaultable(FluidContainmentLogic.TYPE);
        if (node instanceof WorldPipeNode n) {
            IWorldPipeNetTile tile = n.getTileEntity();
            FluidStack stack = testObject.recombine(flow);
            // failing attributes take priority over state
            for (FluidAttribute attribute : FluidAttribute.inferAttributes(stack)) {
                if (!logic.contains(attribute)) {
                    attribute.handleFailure(tile.getWorld(), tile.getPos(), stack);
                    return;
                }
            }
            FluidState state = FluidState.inferState(stack);
            if (!logic.contains(state)) state.handleFailure(tile.getWorld(), tile.getPos(), stack);
        }
    }

    public static int getSupplyOrDemand(NetNode node, FluidTestObject testObject, boolean supply) {
        if (node instanceof NodeExposingCapabilities exposer) {
            IFluidHandler handler = exposer.getProvider().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    exposer.exposedFacing());
            if (handler != null && instanceOf(handler) == null) {
                if (supply) {
                    FluidStack s = handler.drain(testObject.recombine(Integer.MAX_VALUE), false);
                    return s == null ? 0 : s.amount;
                } else {
                    return handler.fill(testObject.recombine(Integer.MAX_VALUE), false);
                }
            }
        }
        return 0;
    }

    public @NotNull FluidNetworkView getNetworkView() {
        if (node.getGroupSafe().getData() instanceof FluidNetworkViewGroupData data) {
            return data.getOrCreate(node);
        }
        return FluidNetworkView.EMPTY;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return fill(resource, doFill, null);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return properties;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return drain(maxDrain, doDrain, null);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return drain(resource, doDrain, null);
    }

    @Override
    public FluidStack getContents() {
        return null;
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canFill() {
        return true;
    }

    @Override
    public boolean canDrain() {
        return true;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluidStack) {
        return true;
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluidStack) {
        return true;
    }

    @Nullable
    public static FluidCapabilityObject instanceOf(IFluidHandler handler) {
        if (handler instanceof FluidCapabilityObject f) return f;
        if (handler instanceof Wrapper w) return w.getParent();
        return null;
    }

    protected class Wrapper implements IFluidHandler, IFluidTankProperties {

        private final EnumFacing facing;
        private final IFluidTankProperties[] properties;

        public Wrapper(EnumFacing facing) {
            this.facing = facing;
            properties = new IFluidTankProperties[FluidCapabilityObject.this.properties.length];
            Arrays.fill(properties, this);
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return properties;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return FluidCapabilityObject.this.fill(resource, doFill, facing);
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return FluidCapabilityObject.this.drain(resource, doDrain, facing);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return FluidCapabilityObject.this.drain(maxDrain, doDrain, facing);
        }

        @Override
        public FluidStack getContents() {
            return null;
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canFill() {
            return true;
        }

        @Override
        public boolean canDrain() {
            return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return true;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return true;
        }

        public FluidCapabilityObject getParent() {
            return FluidCapabilityObject.this;
        }
    }
}
