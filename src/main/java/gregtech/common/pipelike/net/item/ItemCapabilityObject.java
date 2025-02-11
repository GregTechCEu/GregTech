package gregtech.common.pipelike.net.item;

import gregtech.api.graphnet.GraphNetUtility;
import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.NodeManagingPCW;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverse.EdgeDirection;
import gregtech.api.graphnet.traverse.ResilientNetClosestIterator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TickUtil;
import gregtech.api.util.collection.ListHashSet;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class ItemCapabilityObject implements IPipeCapabilityObject, IItemHandler {

    private PipeTileEntity tile;
    private NodeManagingPCW capabilityWrapper;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);
    private final WorldPipeNode node;

    private boolean transferring = false;

    public ItemCapabilityObject(WorldPipeNode node) {
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
            throw new IllegalArgumentException("ItemCapabilityObjects must be initialized to NodeManagingPCWs!");
        this.capabilityWrapper = p;
    }

    private boolean inputDisallowed(EnumFacing side) {
        if (side == null) return false;
        else return tile.isBlocked(side);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(facing == null ? this : wrappers.get(facing));
        }
        return null;
    }

    protected @Nullable NetNode getRelevantNode(@Nullable EnumFacing facing) {
        return facing == null ? node : capabilityWrapper.getNodeForFacing(facing);
    }

    protected @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, final boolean simulate,
                                            EnumFacing side) {
        @NotNull
        ItemStack result = stack;
        if (!this.transferring && !inputDisallowed(side)) {
            NetNode node = getRelevantNode(side);
            if (node == null) node = this.node;
            this.transferring = true;
            ItemNetworkView networkView = getNetworkView(node);
            IItemHandler targetHandler = networkView.getHandler().getHandlerBySlot(slot);
            NetNode targetNode = networkView.getBiMap().get(targetHandler);
            if (targetNode != null) {
                int handlerSlot = slot - networkView.getHandler().getOffsetByHandler(targetHandler);
                final ItemStack remainder = targetHandler.insertItem(handlerSlot, stack, true);
                int insertable = stack.getCount() - remainder.getCount();
                if (insertable > 0) {
                    Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>(
                            node.getGroupSafe().getNodes().size());
                    final ItemTestObject testObject = new ItemTestObject(stack);
                    ListHashSet<NetPath> pathCache = networkView.getPathCache(targetNode);
                    ResilientNetClosestIterator forwardFrontier = null;
                    ResilientNetClosestIterator backwardFrontier = null;
                    Iterator<NetPath> iterator = pathCache.iterator();
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
                                e -> !e.test(testObject));
                        if (insert > 0) {
                            insertable -= insert;
                            ImmutableList<NetNode> asList = path.getOrderedNodes().asList();
                            for (int j = 0; j < asList.size(); j++) {
                                NetNode n = asList.get(j);
                                if (!simulate) reportFlow(n, insert, testObject);
                                flowLimitCache.put(n, flowLimitCache.getInt(n) - insert);
                            }
                            if (!simulate) targetHandler.insertItem(handlerSlot, testObject.recombine(insert), false);
                        }
                    }
                    result = testObject.recombine(remainder.getCount() + insertable);
                }
            }
            this.transferring = false;
        }
        return result;
    }

    protected @NotNull ItemStack extractItem(int slot, int amount, final boolean simulate, EnumFacing side) {
        @NotNull
        ItemStack result = ItemStack.EMPTY;
        if (!this.transferring && !inputDisallowed(side)) {
            NetNode node = getRelevantNode(side);
            if (node == null) node = this.node;
            this.transferring = true;
            ItemNetworkView networkView = getNetworkView(node);
            IItemHandler targetHandler = networkView.getHandler().getHandlerBySlot(slot);
            NetNode targetNode = networkView.getBiMap().get(targetHandler);
            if (targetNode != null) {
                int handlerSlot = slot - networkView.getHandler().getOffsetByHandler(targetHandler);
                final ItemStack stack = targetHandler.extractItem(handlerSlot, amount, true);
                int extractable = stack.getCount();
                if (extractable > 0) {
                    Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>(
                            node.getGroupSafe().getNodes().size());
                    final ItemTestObject testObject = new ItemTestObject(stack);
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
                                e -> !e.test(testObject));
                        if (extract > 0) {
                            extractable -= extract;
                            ImmutableList<NetNode> asList = path.getOrderedNodes().asList();
                            for (int j = 0; j < asList.size(); j++) {
                                NetNode n = asList.get(j);
                                if (!simulate) reportFlow(n, extract, testObject);
                                flowLimitCache.put(n, flowLimitCache.getInt(n) - extract);
                            }
                            if (!simulate) targetHandler.extractItem(handlerSlot, extract, false);
                        }
                    }
                    result = testObject.recombine(stack.getCount() - extractable);
                }
            }
            this.transferring = false;
        }

        return result;
    }

    protected int attemptPath(NetPath path, int available, ToIntFunction<NetNode> limit, Predicate<NetEdge> filter) {
        ImmutableList<NetEdge> edges = path.getOrderedEdges().asList();
        for (int i = 0; i < edges.size(); i++) {
            if (filter.test(edges.get(i))) return 0;
        }
        ImmutableList<NetNode> nodes = path.getOrderedNodes().asList();
        for (int i = 0; i < nodes.size(); i++) {
            available = Math.min(limit.applyAsInt(nodes.get(i)), available);
            if (available <= 0) return 0;
        }
        return available;
    }

    public static int getFlowLimitCached(Reference2IntOpenHashMap<NetNode> cache, NetNode n,
                                         ItemTestObject testObject) {
        return GraphNetUtility.computeIfAbsent(cache, n, z -> getFlowLimit(z, testObject));
    }

    public static int getFlowLimit(NetNode node, ItemTestObject testObject) {
        ThroughputLogic throughput = node.getData().getLogicEntryNullable(ThroughputLogic.TYPE);
        if (throughput == null) return Integer.MAX_VALUE;
        ItemFlowLogic history = node.getData().getLogicEntryNullable(ItemFlowLogic.TYPE);
        if (history == null) return GTUtility.safeCastLongToInt(throughput.getValue());
        Object2LongMap<ItemTestObject> sum = history.getSum(false);
        if (sum.isEmpty()) return GTUtility.safeCastLongToInt(throughput.getValue());
        if (sum.size() < node.getData().getLogicEntryDefaultable(ChannelCountLogic.TYPE).getValue() ||
                sum.containsKey(testObject)) {
            return GTUtility.safeCastLongToInt(throughput.getValue() - sum.getLong(testObject));
        }
        return 0;
    }

    public static void reportFlow(NetNode node, int flow, ItemTestObject testObject) {
        ItemFlowLogic logic = node.getData().getLogicEntryNullable(ItemFlowLogic.TYPE);
        if (logic == null) {
            logic = ItemFlowLogic.TYPE.getNew();
            node.getData().setLogicEntry(logic);
        }
        logic.recordFlow(TickUtil.getTick(), testObject.recombine(flow));
    }

    public @NotNull ItemNetworkView getNetworkView(@Nullable EnumFacing facing) {
        NetNode node = getRelevantNode(facing);
        if (node == null) node = this.node;
        return getNetworkView(node);
    }

    public static @NotNull ItemNetworkView getNetworkView(@NotNull NetNode node) {
        if (node.getGroupSafe().getData() instanceof ItemNetworkViewGroupData data) {
            return data.getOrCreate(node);
        }
        return ItemNetworkView.EMPTY;
    }

    @Override
    public int getSlots() {
        return getNetworkView(node).getHandler().getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return getNetworkView(node).getHandler().getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return insertItem(slot, stack, simulate, null);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return extractItem(slot, amount, simulate, null);
    }

    @Override
    public int getSlotLimit(int slot) {
        return getNetworkView(node).getHandler().getSlotLimit(slot);
    }

    @Nullable
    public static ItemCapabilityObject instanceOf(IItemHandler handler) {
        if (handler instanceof ItemCapabilityObject i) return i;
        if (handler instanceof Wrapper w) return w.getParent();
        return null;
    }

    @Nullable
    public static EnumFacing facingOf(IItemHandler handler) {
        if (handler instanceof Wrapper w) {
            return w.facing;
        }
        return null;
    }

    protected class Wrapper implements IItemHandler {

        private final EnumFacing facing;

        public Wrapper(EnumFacing facing) {
            this.facing = facing;
        }

        @Override
        public int getSlots() {
            return getNetworkView(facing).getHandler().getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return getNetworkView(facing).getHandler().getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return ItemCapabilityObject.this.insertItem(slot, stack, simulate, facing);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemCapabilityObject.this.extractItem(slot, amount, simulate, facing);
        }

        @Override
        public int getSlotLimit(int slot) {
            return getNetworkView(facing).getHandler().getSlotLimit(slot);
        }

        public ItemCapabilityObject getParent() {
            return ItemCapabilityObject.this;
        }
    }
}
