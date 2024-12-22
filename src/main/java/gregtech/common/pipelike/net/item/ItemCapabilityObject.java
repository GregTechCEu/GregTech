package gregtech.common.pipelike.net.item;

import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.NodeExposingCapabilities;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.NodeManagingPCW;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverse.iter.EdgeDirection;
import gregtech.api.graphnet.traverse.iter.EdgeSelector;
import gregtech.api.graphnet.traverse.iter.ResilientNetClosestIterator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MapUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.EnumMap;

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

    protected @Nullable NetNode getRelevantNode(EnumFacing facing) {
        return facing == null ? node : capabilityWrapper.getNodeForFacing(facing);
    }

    protected @NotNull ItemStack insertItem(@NotNull ItemStack stack, boolean simulate, EnumFacing side) {
        if (this.transferring || inputDisallowed(side)) return stack;
        NetNode node = getRelevantNode(side);
        if (node == null) return stack;
        this.transferring = true;

        int flow = stack.getCount();
        ItemTestObject testObject = new ItemTestObject(stack);
        ResilientNetClosestIterator iter = new ResilientNetClosestIterator(node,
                EdgeSelector.filtered(EdgeDirection.OUTGOING, NetEdge.standardBlacklist(testObject)));
        Object2IntOpenHashMap<NetNode> availableDemandCache = new Object2IntOpenHashMap<>();
        Object2IntOpenHashMap<NetNode> flowLimitCache = new Object2IntOpenHashMap<>();
        main:
        while (iter.hasNext()) {
            if (flow <= 0) break;
            final NetNode next = iter.next();
            int limit = Math.min(MapUtil.computeIfAbsent(flowLimitCache, next, n -> getFlowLimit(n, testObject)), flow);
            if (limit <= 0) {
                iter.markInvalid(next);
                continue;
            }
            int demand = MapUtil.computeIfAbsent(availableDemandCache, next,
                    n -> getSupplyOrDemand(n, testObject, false));
            if (demand <= 0) continue;
            demand = Math.min(demand, limit);
            NetEdge span;
            NetNode trace = next;
            ArrayDeque<NetNode> seen = new ArrayDeque<>();
            seen.add(next);
            while ((span = iter.getSpanningTreeEdge(trace)) != null) {
                trace = span.getOppositeNode(trace);
                if (trace == null) continue main;
                int l = MapUtil.computeIfAbsent(flowLimitCache, trace, n -> getFlowLimit(n, testObject));
                if (l == 0) {
                    iter.markInvalid(node);
                    continue main;
                }
                demand = Math.min(demand, l);
                seen.addFirst(trace);
            }
            flow -= demand;
            for (NetNode n : seen) {
                if (!simulate) reportFlow(n, demand, testObject);
                int remaining = flowLimitCache.getInt(n) - demand;
                flowLimitCache.put(n, remaining);
                if (remaining <= 0) {
                    iter.markInvalid(n);
                }
            }
            if (!simulate) reportExtractedInserted(next, demand, testObject, false);
            availableDemandCache.put(next, availableDemandCache.getInt(next) - demand);
        }
        this.transferring = false;
        return testObject.recombine(flow);
    }

    protected @NotNull ItemStack extractItem(int slot, int amount, boolean simulate, EnumFacing side) {
        // TODO expose connected itemnet through capability & allow extraction
        return ItemStack.EMPTY;
    }

    protected int getFlowLimit(NetNode node, ItemTestObject testObject) {
        ThroughputLogic throughput = node.getData().getLogicEntryNullable(ThroughputLogic.TYPE);
        if (throughput == null) return Integer.MAX_VALUE;
        ItemFlowLogic history = node.getData().getLogicEntryNullable(ItemFlowLogic.TYPE);
        if (history == null) return GTUtility.safeCastLongToInt(throughput.getValue() * ItemFlowLogic.BUFFER_MULT);
        Object2LongMap<ItemTestObject> sum = history.getSum();
        if (sum.isEmpty()) return GTUtility.safeCastLongToInt(throughput.getValue() * ItemFlowLogic.BUFFER_MULT);
        if (sum.size() < node.getData().getLogicEntryDefaultable(ChannelCountLogic.TYPE).getValue() ||
                sum.containsKey(testObject)) {
            return GTUtility
                    .safeCastLongToInt(throughput.getValue() * ItemFlowLogic.BUFFER_MULT - sum.getLong(testObject));
        }
        return 0;
    }

    public static void reportFlow(NetNode node, int flow, ItemTestObject testObject) {
        ItemFlowLogic logic = node.getData().getLogicEntryNullable(ItemFlowLogic.TYPE);
        if (logic == null) {
            logic = ItemFlowLogic.TYPE.getNew();
            node.getData().setLogicEntry(logic);
        }
        logic.recordFlow(GTUtility.getTick(), testObject.recombine(flow));
    }

    public static void reportExtractedInserted(NetNode node, int flow, ItemTestObject testObject, boolean extracted) {
        if (flow == 0) return;
        if (node instanceof NodeExposingCapabilities exposer) {
            IItemHandler handler = exposer.getProvider().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    exposer.exposedFacing());
            if (handler != null) {
                // positive flow is supply, aka we pulled flow from this node
                if (extracted) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.extractItem(i, flow, true);
                        if (testObject.test(stack)) {
                            stack = handler.extractItem(i, flow, false);
                            flow -= stack.getCount();
                        }
                        if (flow == 0) return;
                    }
                } else {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = testObject.recombineSafe(flow);
                        flow -= stack.getCount() - handler.insertItem(i, stack, false).getCount();
                        if (flow <= 0) return;
                    }
                }
            }
        }
    }

    public static int getSupplyOrDemand(NetNode node, ItemTestObject testObject, boolean supply) {
        if (node instanceof NodeExposingCapabilities exposer) {
            IItemHandler handler = exposer.getProvider().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    exposer.exposedFacing());
            if (handler != null && !(handler instanceof ItemCapabilityObject)) {
                if (supply) {
                    int sum = 0;
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, true);
                        if (testObject.test(stack)) sum += stack.getCount();
                    }
                    return sum;
                } else {
                    int sum = 0;
                    ItemStack stack = testObject.recombineSafe(Integer.MAX_VALUE);
                    for (int i = 0; i < handler.getSlots(); i++) {
                        sum += stack.getCount() - handler.insertItem(i, stack, true).getCount();
                    }
                    return sum;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return insertItem(stack, simulate, null);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return extractItem(slot, amount, simulate, null);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    protected class Wrapper implements IItemHandler {

        private final EnumFacing facing;

        public Wrapper(EnumFacing facing) {
            this.facing = facing;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return ItemCapabilityObject.this.insertItem(stack, simulate, facing);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemCapabilityObject.this.extractItem(slot, amount, simulate, facing);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    }
}
