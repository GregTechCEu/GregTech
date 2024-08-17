package gregtech.common.pipelike.net.item;

import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverse.ITraverseData;
import gregtech.api.graphnet.traverse.TraverseDataProvider;
import gregtech.api.graphnet.traverse.TraverseGuide;
import gregtech.api.graphnet.traverse.TraverseHelpers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.function.LongConsumer;

public class ItemCapabilityObject implements IPipeCapabilityObject, IItemHandler, IItemTraverseGuideProvider {

    private final WorldPipeNet net;
    private @Nullable PipeTileEntity tile;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);
    private final WorldPipeNetNode node;

    private boolean transferring = false;

    public <N extends WorldPipeNet & FlowWorldPipeNetPath.Provider> ItemCapabilityObject(@NotNull N net,
                                                                                         WorldPipeNetNode node) {
        this.net = net;
        this.node = node;
        for (EnumFacing facing : EnumFacing.VALUES) {
            AbstractNetFlowEdge edge = (AbstractNetFlowEdge) net.getNewEdge();
            edge.setData(NetLogicData.union(node.getData(), (NetLogicData) null));
            wrappers.put(facing, new Wrapper(facing, edge));
        }
    }

    private FlowWorldPipeNetPath.Provider getProvider() {
        return (FlowWorldPipeNetPath.Provider) net;
    }

    private boolean inputDisallowed(EnumFacing side) {
        if (side == null) return false;
        if (tile == null) return true;
        else return tile.isBlocked(side);
    }

    private Iterator<FlowWorldPipeNetPath> getPaths(@NotNull ITraverseData<?, ?> data) {
        assert tile != null;
        return getProvider().getPaths(net.getNode(tile.getPos()), data.getTestObject(), data.getSimulatorKey(),
                data.getQueryTick());
    }

    @Override
    public void setTile(@Nullable PipeTileEntity tile) {
        this.tile = tile;
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldItemNet.CAPABILITIES;
    }

    @Override
    public <T> T getCapabilityForSide(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(facing == null ? this : wrappers.get(facing));
        }
        return null;
    }

    public @NotNull ItemStack insertItem(@NotNull ItemStack stack, boolean simulate, EnumFacing side) {
        if (this.transferring) return stack;
        this.transferring = true;

        var guide = getGuide(ItemTraverseData::new, new ItemTestObject(stack), stack.getCount(), simulate, side);
        if (guide == null) return stack;
        int consumed = (int) TraverseHelpers.traverseFlood(guide.getData(), guide.getPaths(), guide.getFlow());
        guide.reportConsumedFlow(consumed);

        this.transferring = false;
        return guide.getData().getTestObject().recombine(stack.getCount() - consumed);
    }

    @Nullable
    @Override
    public <D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                               TraverseDataProvider<D, ItemTestObject> provider,
                                                                                                                                               ItemTestObject testObject,
                                                                                                                                               long flow,
                                                                                                                                               boolean simulate) {
        return getGuide(provider, testObject, flow, simulate, null);
    }

    @Nullable
    protected <
            D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                               TraverseDataProvider<D, ItemTestObject> provider,
                                                                                                                                               ItemTestObject testObject,
                                                                                                                                               long flow,
                                                                                                                                               boolean simulate,
                                                                                                                                               EnumFacing side) {
        if (tile == null || inputDisallowed(side)) return null;

        SimulatorKey simulator = simulate ? SimulatorKey.getNewSimulatorInstance() : null;
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        D data = provider.of(net, testObject, simulator, tick, tile.getPos(), side);

        LongConsumer flowReport = null;
        Wrapper wrapper = this.wrappers.get(side);
        if (wrapper != null) {
            AbstractNetFlowEdge internalBuffer = wrapper.getBuffer();
            if (internalBuffer != null) {
                long limit = internalBuffer.getFlowLimit(testObject, net, tick, simulator);
                if (limit <= 0) {
                    this.transferring = false;
                    return null;
                }
                flow = Math.min(limit, flow);
                flowReport = l -> data.consumeFlowLimit(internalBuffer, node, l);
            }
        }
        return new TraverseGuide<>(data, () -> getPaths(data), flow, flowReport);
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
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    protected class Wrapper implements IItemHandler, IItemTraverseGuideProvider {

        private final EnumFacing facing;
        private final AbstractNetFlowEdge buffer;

        public Wrapper(EnumFacing facing, AbstractNetFlowEdge buffer) {
            this.facing = facing;
            this.buffer = buffer;
        }

        @Nullable
        @Override
        public <D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                                   TraverseDataProvider<D, ItemTestObject> provider,
                                                                                                                                                   ItemTestObject testObject,
                                                                                                                                                   long flow,
                                                                                                                                                   boolean simulate) {
            return ItemCapabilityObject.this.getGuide(provider, testObject, flow, simulate, facing);
        }

        public AbstractNetFlowEdge getBuffer() {
            return buffer;
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
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    }
}
