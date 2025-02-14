package gregtech.common.pipelike.net.fluid;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.graphnet.GraphNetUtility;
import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import gregtech.api.graphnet.pipenet.physical.tile.NodeManagingPCW;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverse.EdgeDirection;
import gregtech.api.graphnet.traverse.ResilientNetClosestIterator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TickUtil;
import gregtech.api.util.collection.ListHashSet;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class FluidCapabilityObject implements IPipeCapabilityObject, IFluidHandler {

    private PipeTileEntity tile;
    private NodeManagingPCW capabilityWrapper;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);
    private final WorldPipeNode node;

    private boolean transferring = false;

    public FluidCapabilityObject(WorldPipeNode node) {
        this.node = node;
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

    protected @Nullable NetNode getRelevantNode(@Nullable EnumFacing facing) {
        return facing == null ? node : capabilityWrapper.getNodeForFacing(facing);
    }

    protected int fill(FluidStack resource, final boolean doFill, EnumFacing side) {
        if (this.transferring || inputDisallowed(side)) return 0;
        NetNode node = getRelevantNode(side);
        if (node == null) node = this.node;
        this.transferring = true;

        int flow = resource.amount;
        FluidNetworkView networkView = getNetworkView(node);
        FluidTestObject testObject = new FluidTestObject(resource);

        int maxPredictedSize = node.getGroupSafe().getNodes().size();
        Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>(maxPredictedSize);
        Reference2BooleanOpenHashMap<NetNode> lossyCache = new Reference2BooleanOpenHashMap<>(maxPredictedSize);
        List<Runnable> postActions = new ObjectArrayList<>();

        for (IFluidHandler targetHandler : networkView.getHandler().getBackingHandlers()) {
            NetNode targetNode = networkView.getBiMap().get(targetHandler);
            if (targetNode == null) continue;
            final int filled = targetHandler.fill(testObject.recombine(flow), false);
            int insertable = filled;
            if (insertable <= 0) continue;
            ListHashSet<NetPath> pathCache = networkView.getPathCache(targetNode);
            ResilientNetClosestIterator forwardFrontier = null;
            ResilientNetClosestIterator backwardFrontier = null;
            Iterator<NetPath> iterator = pathCache.iterator();
            pathloop:
            while (insertable > 0) {
                NetPath path;
                if (iterator != null && iterator.hasNext()) path = iterator.next();
                else {
                    iterator = null;
                    if (forwardFrontier == null) {
                        forwardFrontier = new ResilientNetClosestIterator(node, EdgeDirection.OUTGOING);
                        backwardFrontier = new ResilientNetClosestIterator(targetNode, EdgeDirection.INCOMING);
                    }
                    path = GraphNetUtility.p2pNextPath(
                            n -> getFlowLimitCached(flowLimitCache, n, testObject) <= 0,
                            e -> !e.test(testObject), forwardFrontier, backwardFrontier);
                    if (path == null) break;
                    int i = pathCache.size();
                    while (i > 0 && pathCache.get(i - 1).getWeight() > path.getWeight()) {
                        i--;
                    }
                    if (!pathCache.addSensitive(i, path)) break;
                }
                int insert = attemptPath(path, insertable,
                        n -> getFlowLimitCached(flowLimitCache, n, testObject),
                        e -> !e.test(testObject),
                        n -> isLossyNodeCached(lossyCache, n, testObject));
                if (insert > 0) {
                    insertable -= insert;
                    ImmutableList<NetNode> asList = path.getOrderedNodes().asList();
                    for (int j = 0; j < asList.size(); j++) {
                        NetNode n = asList.get(j);
                        if (doFill) {
                            // reporting temp change can cause temperature pipe destruction which causes
                            // graph modification while iterating.
                            Runnable post = reportFlow(n, insert, testObject);
                            if (post != null) postActions.add(post);
                        }
                        flowLimitCache.put(n, flowLimitCache.getInt(n) - insert);
                        if (isLossyNodeCached(lossyCache, n, testObject)) {
                            // reporting loss can cause misc pipe destruction which causes
                            // graph modification while iterating.
                            if (doFill) postActions.add(() -> handleLoss(n, insert, testObject));
                            // a lossy node will prevent filling the target
                            continue pathloop;
                        }
                    }
                    if (doFill) targetHandler.fill(testObject.recombine(insert), true);
                }
            }
            flow -= filled - insertable;
        }
        postActions.forEach(Runnable::run);
        this.transferring = false;
        return resource.amount - flow;
    }

    protected FluidStack drain(int maxDrain, boolean doDrain, EnumFacing side) {
        FluidStack stack = getNetworkView(side).getHandler().drain(maxDrain, false);
        if (stack == null) return null;
        return drain(stack, doDrain, side);
    }

    protected FluidStack drain(FluidStack resource, boolean doDrain, EnumFacing side) {
        if (this.transferring) return null;
        NetNode node = getRelevantNode(side);
        if (node == null) node = this.node;
        this.transferring = true;

        int flow = resource.amount;
        FluidNetworkView networkView = getNetworkView(node);
        FluidTestObject testObject = new FluidTestObject(resource);

        int maxPredictedSize = node.getGroupSafe().getNodes().size();
        Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>(maxPredictedSize);
        Reference2BooleanOpenHashMap<NetNode> lossyCache = new Reference2BooleanOpenHashMap<>(maxPredictedSize);
        List<Runnable> postActions = new ObjectArrayList<>();

        for (IFluidHandler targetHandler : networkView.getHandler().getBackingHandlers()) {
            NetNode targetNode = networkView.getBiMap().get(targetHandler);
            if (targetNode == null) continue;
            final FluidStack drained = targetHandler.drain(testObject.recombine(flow), false);
            int extractable = drained == null ? 0 : drained.amount;
            if (extractable <= 0) continue;
            ListHashSet<NetPath> pathCache = getNetworkView(targetNode).getPathCache(node);
            ResilientNetClosestIterator forwardFrontier = null;
            ResilientNetClosestIterator backwardFrontier = null;
            Iterator<NetPath> iterator = pathCache.iterator();
            while (extractable > 0) {
                NetPath path;
                if (iterator != null && iterator.hasNext()) path = iterator.next();
                else {
                    iterator = null;
                    if (forwardFrontier == null) {
                        forwardFrontier = new ResilientNetClosestIterator(targetNode, EdgeDirection.OUTGOING);
                        backwardFrontier = new ResilientNetClosestIterator(node, EdgeDirection.INCOMING);
                    }
                    path = GraphNetUtility.p2pNextPath(
                            n -> getFlowLimitCached(flowLimitCache, n, testObject) <= 0,
                            e -> !e.test(testObject), forwardFrontier, backwardFrontier);
                    if (path == null) break;
                    int i = pathCache.size();
                    while (i > 0 && pathCache.get(i - 1).getWeight() > path.getWeight()) {
                        i--;
                    }
                    if (!pathCache.addSensitive(i, path)) break;
                }
                int extract = attemptPath(path, extractable,
                        n -> getFlowLimitCached(flowLimitCache, n, testObject),
                        e -> !e.test(testObject),
                        n -> isLossyNodeCached(lossyCache, n, testObject));
                if (extract > 0) {
                    extractable -= extract;
                    ImmutableList<NetNode> asList = path.getOrderedNodes().asList();
                    for (int j = 0; j < asList.size(); j++) {
                        NetNode n = asList.get(j);
                        if (doDrain) {
                            // reporting temp change can cause temperature pipe destruction which causes
                            // graph modification while iterating.
                            Runnable post = reportFlow(n, extract, testObject);
                            if (post != null) postActions.add(post);
                        }
                        flowLimitCache.put(n, flowLimitCache.getInt(n) - extract);
                        if (isLossyNodeCached(lossyCache, n, testObject)) {
                            // reporting loss can cause misc pipe destruction which causes
                            // graph modification while iterating.
                            if (doDrain) postActions.add(() -> handleLoss(n, extract, testObject));
                            // a lossy node will prevent receiving extracted fluid
                            extractable += extract;
                            break;
                        }
                    }
                    if (doDrain) targetHandler.drain(testObject.recombine(extract), true);
                }
            }
            flow -= drained.amount - extractable;
        }
        postActions.forEach(Runnable::run);
        this.transferring = false;
        return testObject.recombine(resource.amount - flow);
    }

    protected int attemptPath(NetPath path, int available, ToIntFunction<NetNode> limit, Predicate<NetEdge> filter,
                              Predicate<NetNode> lossy) {
        ImmutableList<NetEdge> edges = path.getOrderedEdges().asList();
        for (int i = 0; i < edges.size(); i++) {
            if (filter.test(edges.get(i))) return 0;
        }
        ImmutableList<NetNode> nodes = path.getOrderedNodes().asList();
        for (int i = 0; i < nodes.size(); i++) {
            NetNode n = nodes.get(i);
            if (lossy.test(n)) return available;
            available = Math.min(limit.applyAsInt(n), available);
            if (available <= 0) return 0;
        }
        return available;
    }

    public static int getFlowLimitCached(Reference2IntOpenHashMap<NetNode> cache, NetNode n,
                                         FluidTestObject testObject) {
        return GraphNetUtility.computeIfAbsent(cache, n, z -> getFlowLimit(z, testObject));
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

    public static boolean isLossyNodeCached(Reference2BooleanOpenHashMap<NetNode> cache, NetNode n,
                                            FluidTestObject testObject) {
        return GraphNetUtility.computeIfAbsent(cache, n, z -> isLossyNode(z, testObject));
    }

    public static boolean isLossyNode(NetNode node, FluidTestObject testObject) {
        FluidContainmentLogic containmentLogic = node.getData().getLogicEntryNullable(FluidContainmentLogic.TYPE);
        return containmentLogic != null && !containmentLogic.handles(testObject);
    }

    public static Runnable reportFlow(NetNode node, int flow, FluidTestObject testObject) {
        FluidFlowLogic logic = node.getData().getLogicEntryNullable(FluidFlowLogic.TYPE);
        if (logic == null) {
            logic = FluidFlowLogic.TYPE.getNew();
            node.getData().setLogicEntry(logic);
        }
        logic.recordFlow(TickUtil.getTick(), testObject, flow);
        TemperatureLogic temp = node.getData().getLogicEntryNullable(TemperatureLogic.TYPE);
        if (temp == null) return null;
        FluidContainmentLogic cont = node.getData().getLogicEntryDefaultable(FluidContainmentLogic.TYPE);
        FluidStack stack = testObject.recombine();
        int t = stack.getFluid().getTemperature(stack);
        boolean noParticle = cont.getMaximumTemperature() >= t;
        return () -> {
            temp.moveTowardsTemperature(t, TickUtil.getTick(), flow, noParticle);
            if (node instanceof WorldPipeNode n) {
                temp.defaultHandleTemperature(n.getNet().getWorld(), n.getEquivalencyData());
            }
        };
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

    public @NotNull FluidNetworkView getNetworkView(@Nullable EnumFacing facing) {
        NetNode node = getRelevantNode(facing);
        if (node == null) node = this.node;
        return getNetworkView(node);
    }

    public static @NotNull FluidNetworkView getNetworkView(@NotNull NetNode node) {
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
        return getNetworkView(node).getHandler().getTankProperties();
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return drain(maxDrain, doDrain, null);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return drain(resource, doDrain, null);
    }

    @Nullable
    public static FluidCapabilityObject instanceOf(IFluidHandler handler) {
        if (handler instanceof FluidCapabilityObject f) return f;
        if (handler instanceof Wrapper w) return w.getParent();
        return null;
    }

    @Nullable
    public static EnumFacing facingOf(IFluidHandler handler) {
        if (handler instanceof Wrapper w) {
            return w.facing;
        }
        return null;
    }

    protected class Wrapper implements IFluidHandler {

        private final EnumFacing facing;

        public Wrapper(EnumFacing facing) {
            this.facing = facing;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return getNetworkView(facing).getHandler().getTankProperties();
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

        public FluidCapabilityObject getParent() {
            return FluidCapabilityObject.this;
        }
    }
}
