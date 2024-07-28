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
import gregtech.api.graphnet.traverse.TraverseHelpers;
import gregtech.common.pipelike.net.energy.WorldEnergyNet;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Iterator;

public class ItemCapabilityObject implements IPipeCapabilityObject, IItemHandler {

    private final WorldPipeNet net;
    private @Nullable PipeTileEntity tile;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);

    private boolean transferring = false;

    public <N extends WorldPipeNet & FlowWorldPipeNetPath.Provider> ItemCapabilityObject(@NotNull N net, WorldPipeNetNode node) {
        this.net = net;
        for (EnumFacing facing : EnumFacing.VALUES) {
            AbstractNetFlowEdge edge = (AbstractNetFlowEdge) net.getNewEdge();
            edge.setData(NetLogicData.union(node.getData(), (NetLogicData) null));
            wrappers.put(facing, new Wrapper(facing, edge));
        }
    }

    private FlowWorldPipeNetPath.Provider getProvider() {
        return (FlowWorldPipeNetPath.Provider) net;
    }

    private Iterator<FlowWorldPipeNetPath> getPaths(ItemTraverseData data) {
        assert tile != null;
        return getProvider().getPaths(net.getNode(tile.getPos()), data.getTestObject(), data.getSimulatorKey(), data.getQueryTick());
    }

    @Override
    public void setTile(@Nullable PipeTileEntity tile) {
        this.tile = tile;
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldEnergyNet.CAPABILITIES;
    }

    @Override
    public <T> T getCapabilityForSide(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(facing == null ? this : wrappers.get(facing));
        }
        return null;
    }

    public @NotNull ItemStack insertItem(@NotNull ItemStack stack, boolean simulate, EnumFacing side) {
        if (tile == null || this.transferring) return stack;
        this.transferring = true;

        SimulatorKey simulator = null;
        if (simulate) simulator = SimulatorKey.getNewSimulatorInstance();
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();

        ItemTestObject testObject = new ItemTestObject(stack);

        AbstractNetFlowEdge internalBuffer = this.wrappers.get(side).getBuffer();
        int available = stack.getCount();
        if (internalBuffer != null) {
            long limit = internalBuffer.getFlowLimit(testObject, net, tick, simulator);
            if (limit <= 0) return stack;
            available = (int) Math.min(limit, available);
        }

        ItemTraverseData data = new ItemTraverseData(net, testObject, simulator, tick, tile.getPos(), side);
        available = (int) TraverseHelpers.traverseFlood(data, getPaths(data), available);

        if (internalBuffer != null)
            internalBuffer.consumeFlowLimit(testObject, net, stack.getCount() - available, tick, simulator);
        this.transferring = false;
        return testObject.recombine(available);
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

    protected class Wrapper implements IItemHandler {

        private final EnumFacing facing;
        private final AbstractNetFlowEdge buffer;

        public Wrapper(EnumFacing facing, AbstractNetFlowEdge buffer) {
            this.facing = facing;
            this.buffer = buffer;
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
