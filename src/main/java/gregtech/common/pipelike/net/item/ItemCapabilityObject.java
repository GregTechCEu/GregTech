package gregtech.common.pipelike.net.item;

import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.NodeExposingCapabilities;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.NodeManagingPCW;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverse.FDTraverse;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicInteger;

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
        long queryTick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        SimulatorKey key = simulate ? SimulatorKey.getNewSimulatorInstance() : null;
        ItemTestObject testObject = new ItemTestObject(stack);
        AtomicInteger report = new AtomicInteger();
        FDTraverse.flood(node.getNet(),
                (n, f) -> {
                    if (n == node) report.addAndGet(f);
                    else if (!simulate) reportFlow(n, f, testObject);
                },
                (e, f) -> reportFlow(e, f, testObject, key, true),
                e -> e instanceof AbstractNetFlowEdge n ?
                        GTUtility.safeCastLongToInt(n.getFlowLimit(testObject, node.getNet(), queryTick, key)) : 0,
                n -> n == node ? flow : getSupply(n, testObject, false), null);

        this.transferring = false;
        return testObject.recombine(stack.getCount() - report.get());
    }

    protected @NotNull ItemStack extractItem(int slot, int amount, boolean simulate, EnumFacing side) {
        // TODO expose connected itemnet through capability & allow extraction
        return ItemStack.EMPTY;
    }

    protected void reportFlow(NetEdge edge, int flow, ItemTestObject testObject, SimulatorKey key, boolean sourceBias) {
        if (edge instanceof AbstractNetFlowEdge n)
            n.consumeFlowLimit(testObject, node.getNet(), flow, getQueryTick(), key);
        if (key == null) {
            NetNode node = sourceBias ? edge.getSource() : edge.getTarget();
            if (node == null) return;
            ItemFlowLogic logic = node.getData().getLogicEntryNullable(ItemFlowLogic.TYPE);
            if (logic == null) {
                logic = ItemFlowLogic.TYPE.getNew();
                node.getData().setLogicEntry(logic);
            }
            logic.recordFlow(getQueryTick(), testObject.recombine(flow));
        }
    }

    protected void reportFlow(NetNode node, int flow, ItemTestObject testObject) {
        if (flow == 0) return;
        if (node instanceof NodeExposingCapabilities exposer) {
            IItemHandler handler = exposer.getProvider().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    exposer.exposedFacing());
            if (handler != null) {
                // positive flow is supply, aka we pulled flow from this node
                if (flow > 0) {
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
                        if (flow == 0) return;
                    }
                }
            }
        }
    }

    protected int getSupply(NetNode node, ItemTestObject testObject, boolean supply) {
        if (node instanceof NodeExposingCapabilities exposer) {
            IItemHandler handler = exposer.getProvider().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    exposer.exposedFacing());
            if (handler != null) {
                if (supply) {
                    int sum = 0;
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.extractItem(i, Integer.MAX_VALUE, true);
                        if (testObject.test(stack)) sum += stack.getCount();
                    }
                    return sum;
                } else {
                    int sum = 0;
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = testObject.recombineSafe(Integer.MAX_VALUE);
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
