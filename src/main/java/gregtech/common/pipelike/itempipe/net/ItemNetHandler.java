package gregtech.common.pipelike.itempipe.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.util.FacingPos;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.ItemStackKey;
import gregtech.common.covers.*;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemNetHandler implements IItemHandler {

    private ItemPipeNet net;
    private TileEntityItemPipe pipe;
    private TileEntityItemPipeTickable tickingPipe;
    private final World world;
    private final EnumFacing facing;
    private final Map<FacingPos, Integer> simulatedTransfersGlobalRoundRobin = new HashMap<>();
    private int simulatedTransfers = 0;
    private final ItemStackHandler testHandler = new ItemStackHandler(1);

    public ItemNetHandler(ItemPipeNet net, TileEntityItemPipe pipe, EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
        this.world = pipe.getWorld();
    }

    public void updateNetwork(ItemPipeNet net) {
        this.net = net;
    }

    public ItemPipeNet getNet() {
        return net;
    }

    private void copyTransferred() {
        simulatedTransfers = tickingPipe.getTransferredItems();
        simulatedTransfersGlobalRoundRobin.clear();
        simulatedTransfersGlobalRoundRobin.putAll(pipe.getTransferred());
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;
        // only set pipe to ticking when something is inserted
        if (tickingPipe == null) {
            this.tickingPipe = (TileEntityItemPipeTickable) pipe.setSupportsTicking();
            this.pipe = tickingPipe;
        }

        if (net == null || pipe == null || pipe.isInvalid() || pipe.isFaceBlocked(facing)) {
            return stack;
        }

        copyTransferred();
        CoverBehavior pipeCover = getCoverOnPipe(pipe.getPipePos(), facing);
        CoverBehavior tileCover = getCoverOnNeighbour(pipe.getPipePos(), facing);

        boolean pipeConveyor = pipeCover instanceof CoverConveyor, tileConveyor = tileCover instanceof CoverConveyor;
        // abort if there are two conveyors
        if (pipeConveyor && tileConveyor) return stack;

        if (tileCover != null && !checkImportCover(tileCover, false, stack))
            return stack;

        if (!pipeConveyor && !tileConveyor)
            return insertFirst(stack, simulate);

        CoverConveyor conveyor = (CoverConveyor) (pipeConveyor ? pipeCover : tileCover);
        if (conveyor.getConveyorMode() == (pipeConveyor ? CoverConveyor.ConveyorMode.IMPORT : CoverConveyor.ConveyorMode.EXPORT)) {
            boolean roundRobinGlobal = conveyor.getDistributionMode() == DistributionMode.ROUND_ROBIN_GLOBAL;
            if (roundRobinGlobal || conveyor.getDistributionMode() == DistributionMode.ROUND_ROBIN_PRIO)
                return insertRoundRobin(stack, simulate, roundRobinGlobal);
        }

        return insertFirst(stack, simulate);
    }

    public boolean checkImportCover(CoverBehavior cover, boolean onPipe, ItemStack stack) {
        if (cover == null) return true;
        if (cover instanceof CoverItemFilter) {
            CoverItemFilter filter = (CoverItemFilter) cover;
            return (filter.getFilterMode() != ItemFilterMode.FILTER_BOTH &&
                    (filter.getFilterMode() != ItemFilterMode.FILTER_INSERT || !onPipe) &&
                    (filter.getFilterMode() != ItemFilterMode.FILTER_EXTRACT || onPipe)) || filter.testItemStack(stack);
        }
        return true;
    }

    public ItemStack insertFirst(ItemStack stack, boolean simulate) {
        for (ItemPipeNet.Inventory inv : net.getNetData(pipe.getPipePos(), facing)) {
            stack = insert(inv, stack, simulate);
            if (stack.isEmpty())
                return ItemStack.EMPTY;
        }
        return stack;
    }

    public ItemStack insertRoundRobin(ItemStack stack, boolean simulate, boolean global) {
        List<ItemPipeNet.Inventory> handlers = net.getNetData(pipe.getPipePos(), facing);
        if (handlers.size() == 0)
            return stack;
        if (handlers.size() == 1)
            return insert(handlers.get(0), stack, simulate);
        List<ItemPipeNet.Inventory> handlersCopy = new ArrayList<>(handlers);
        int original = stack.getCount();

        if (global) {
            stack = insertToHandlersEnhanced(handlersCopy, stack, handlers.size(), simulate);
        } else {
            stack = insertToHandlers(handlersCopy, stack, simulate);
            if (!stack.isEmpty() && handlersCopy.size() > 0)
                stack = insertToHandlers(handlersCopy, stack, simulate);
        }

        return stack;
    }

    /**
     * Inserts items equally to all handlers
     * if it couldn't insert all items, the handler will be removed
     *
     * @param copy     to insert to
     * @param stack    to insert
     * @param simulate simulate
     * @return remainder
     */
    private ItemStack insertToHandlers(List<ItemPipeNet.Inventory> copy, ItemStack stack, boolean simulate) {
        Iterator<ItemPipeNet.Inventory> handlerIterator = copy.listIterator();
        int inserted = 0;
        int count = stack.getCount();
        int c = count / copy.size();
        int m = c == 0 ? count % copy.size() : 0;
        while (handlerIterator.hasNext()) {
            ItemPipeNet.Inventory handler = handlerIterator.next();

            int amount = c;
            if (m > 0) {
                amount++;
                m--;
            }
            amount = Math.min(amount, stack.getCount() - inserted);
            if (amount == 0) break;
            ItemStack toInsert = stack.copy();
            toInsert.setCount(amount);
            int r = insert(handler, toInsert, simulate).getCount();
            if (r < amount) {
                inserted += (amount - r);
            }
            if (r == 1 && c == 0 && amount == 1) {
                m++;
            }

            if (r > 0)
                handlerIterator.remove();
        }

        ItemStack remainder = stack.copy();
        remainder.setCount(count - inserted);
        return remainder;
    }

    private ItemStack insertToHandlersEnhanced(List<ItemPipeNet.Inventory> copy, ItemStack stack, int dest, boolean simulate) {
        LinkedList<EnhancedRoundRobinData> transferred = new LinkedList<>();
        LinkedList<Integer> steps = new LinkedList<>();
        int min = Integer.MAX_VALUE;
        ItemStack simStack;

        // find inventories that are not full and get the amount that was inserted in total
        for (ItemPipeNet.Inventory inv : copy) {
            simStack = stack.copy();
            int ins = stack.getCount() - insert(inv, simStack, true, true).getCount();
            if (ins <= 0)
                continue;
            int didTransfer = didTransferTo(inv, simulate);
            EnhancedRoundRobinData data = new EnhancedRoundRobinData(inv, ins, didTransfer);
            transferred.addLast(data);

            min = Math.min(min, didTransfer);

            if (!steps.contains(didTransfer)) {
                steps.add(didTransfer);
            }
        }

        if (transferred.isEmpty() || steps.isEmpty())
            return stack;

        if (!simulate && min < Integer.MAX_VALUE) {
            decrementBy(min);
        }

        transferred.sort(Comparator.comparingInt(data -> data.transferred));
        steps.sort(Integer::compare);

        if (transferred.get(0).transferred != steps.get(0)) {
            return stack;
        }

        int amount = stack.getCount();
        int c = amount / transferred.size();
        int m = amount % transferred.size();
        List<EnhancedRoundRobinData> transferredCopy = new ArrayList<>(transferred);
        int nextStep = steps.isEmpty() ? -1 : steps.pollFirst();

        // equally distribute items over all inventories
        // it takes into account how much was inserted in total
        // f.e. if inv1 has 2 inserted and inv2 has 6 inserted, it will first try to insert 4 into inv1 so that both have 6 and then it will distribute the rest equally
        outer:
        while (amount > 0 && !transferredCopy.isEmpty()) {
            Iterator<EnhancedRoundRobinData> iterator = transferredCopy.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                EnhancedRoundRobinData data = iterator.next();
                if (nextStep >= 0 && data.transferred >= nextStep)
                    break;

                int toInsert;
                if (nextStep <= 0) {
                    if (amount <= m) {
                        //break outer;
                        toInsert = 1;
                    } else {
                        toInsert = Math.min(c, amount);
                    }
                } else {
                    toInsert = Math.min(amount, nextStep - data.transferred);
                }
                if (data.toTransfer + toInsert >= data.maxInsertable) {
                    data.toTransfer = data.maxInsertable;
                    iterator.remove();
                } else {
                    data.toTransfer += toInsert;
                }

                data.transferred += toInsert;

                if ((amount -= toInsert) == 0) {
                    break outer;
                }
                i++;
            }

            for (EnhancedRoundRobinData data : transferredCopy) {
                if (data.transferred < nextStep)
                    continue outer;
            }
            if (steps.isEmpty()) {
                if (nextStep >= 0) {
                    c = amount / transferredCopy.size();
                    m = amount % transferredCopy.size();
                    nextStep = -1;
                }
            } else {
                nextStep = steps.pollFirst();
            }
        }

        int inserted = 0;

        // finally actually insert the item
        for (EnhancedRoundRobinData data : transferred) {
            ItemStack toInsert = stack.copy();
            toInsert.setCount(data.toTransfer);
            int ins = data.toTransfer - insert(data.inventory, toInsert, simulate).getCount();
            inserted += ins;
            transferTo(data.inventory, simulate, ins);
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }

    public ItemStack insert(ItemPipeNet.Inventory handler, ItemStack stack, boolean simulate) {
        return insert(handler, stack, simulate, false);
    }

    public ItemStack insert(ItemPipeNet.Inventory handler, ItemStack stack, boolean simulate, boolean ignoreLimit) {
        int allowed = ignoreLimit ? stack.getCount() : checkTransferable(handler.getProperties().getTransferRate(), stack.getCount(), simulate);
        if (allowed == 0 || !handler.matchesFilters(stack)) {
            return stack;
        }
        CoverBehavior pipeCover = getCoverOnPipe(handler.getPipePos(), handler.getFaceToHandler());
        CoverBehavior tileCover = getCoverOnNeighbour(handler.getPipePos(), handler.getFaceToHandler());
        if (pipeCover != null) {
            testHandler.setStackInSlot(0, stack.copy());
            IItemHandler itemHandler = pipeCover.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, testHandler);
            if (itemHandler == null || (itemHandler != testHandler && (allowed = itemHandler.extractItem(0, allowed, true).getCount()) <= 0)) {
                testHandler.setStackInSlot(0, ItemStack.EMPTY);
                return stack;
            }
            testHandler.setStackInSlot(0, ItemStack.EMPTY);
        }
        IItemHandler neighbourHandler = handler.getHandler(world);
        if (pipeCover instanceof CoverRoboticArm && ((CoverRoboticArm) pipeCover).getConveyorMode() == CoverConveyor.ConveyorMode.EXPORT) {
            return insertOverRobotArm(neighbourHandler, (CoverRoboticArm) pipeCover, stack, simulate, allowed, ignoreLimit);
        }
        if (tileCover instanceof CoverRoboticArm && ((CoverRoboticArm) tileCover).getConveyorMode() == CoverConveyor.ConveyorMode.IMPORT) {
            return insertOverRobotArm(neighbourHandler, (CoverRoboticArm) tileCover, stack, simulate, allowed, ignoreLimit);
        }

        return insert(neighbourHandler, stack, simulate, allowed, ignoreLimit);
    }

    private ItemStack insert(IItemHandler handler, ItemStack stack, boolean simulate, int allowed, boolean ignoreLimit) {
        if (stack.getCount() == allowed) {
            ItemStack re = GTTransferUtils.insertItem(handler, stack, simulate);
            if (!ignoreLimit)
                transfer(simulate, stack.getCount() - re.getCount());
            return re;
        }
        ItemStack toInsert = stack.copy();
        toInsert.setCount(Math.min(allowed, stack.getCount()));
        int r = GTTransferUtils.insertItem(handler, toInsert, simulate).getCount();
        if (!ignoreLimit)
            transfer(simulate, toInsert.getCount() - r);
        ItemStack remainder = stack.copy();
        remainder.setCount(r + (stack.getCount() - toInsert.getCount()));
        return remainder;
    }

    public CoverBehavior getCoverOnPipe(BlockPos pos, EnumFacing handlerFacing) {
        TileEntity tile = pipe.getWorld().getTileEntity(pos);
        if (tile instanceof TileEntityItemPipe) {
            ICoverable coverable = ((TileEntityItemPipe) tile).getCoverableImplementation();
            return coverable.getCoverAtSide(handlerFacing);
        }
        return null;
    }

    public CoverBehavior getCoverOnNeighbour(BlockPos pos, EnumFacing handlerFacing) {
        TileEntity tile = pipe.getWorld().getTileEntity(pos.offset(handlerFacing));
        if (tile != null) {
            ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, handlerFacing.getOpposite());
            if (coverable == null) return null;
            return coverable.getCoverAtSide(handlerFacing.getOpposite());
        }
        return null;
    }

    public ItemStack insertOverRobotArm(IItemHandler handler, CoverRoboticArm arm, ItemStack stack, boolean simulate, int allowed, boolean ignoreLimit) {
        int rate;
        boolean isStackSpecific = false;
        Object index = arm.getItemFilterContainer().matchItemStack(stack);
        if (index instanceof Integer) {
            rate = arm.getItemFilterContainer().getSlotTransferLimit(index);
            isStackSpecific = true;
        } else
            rate = arm.getItemFilterContainer().getTransferStackSize();
        int count;
        switch (arm.getTransferMode()) {
            case TRANSFER_ANY:
                return insert(handler, stack, simulate, allowed, ignoreLimit);
            case KEEP_EXACT:
                count = rate - countStack(handler, stack, arm, isStackSpecific);
                if (count <= 0) return stack;
                count = Math.min(allowed, Math.min(stack.getCount(), count));
                return insert(handler, stack, simulate, count, ignoreLimit);
            case TRANSFER_EXACT:
                int max = allowed + arm.getBuffer();
                count = Math.min(max, Math.min(rate, stack.getCount()));
                if (count < rate) {
                    arm.buffer(allowed);
                    return stack;
                } else {
                    arm.clearBuffer();
                }
                if (insert(handler, stack, true, count, ignoreLimit).getCount() != stack.getCount() - count) {
                    return stack;
                }
                return insert(handler, stack, simulate, count, ignoreLimit);
        }
        return stack;
    }

    public int countStack(IItemHandler handler, ItemStack stack, CoverRoboticArm arm, boolean isStackSpecific) {
        if (arm == null) return 0;
        ItemStackKey key = new ItemStackKey(stack);
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (slot.isEmpty()) continue;
            if (isStackSpecific ? key.isItemStackEqual(slot) : arm.getItemFilterContainer().testItemStack(slot)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    private int checkTransferable(float rate, int amount, boolean simulate) {
        int max = (int) ((rate * 64) + 0.5);
        if (simulate)
            return Math.max(0, Math.min(max - simulatedTransfers, amount));
        else
            return Math.max(0, Math.min(max - tickingPipe.getTransferredItems(), amount));
    }

    private void transfer(boolean simulate, int amount) {
        if (simulate)
            simulatedTransfers += amount;
        else
            tickingPipe.transferItems(amount);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int i) {
        return 64;
    }

    private void transferTo(ItemPipeNet.Inventory handler, boolean simulate, int amount) {
        if (simulate)
            simulatedTransfersGlobalRoundRobin.merge(handler.toFacingPos(), amount, Integer::sum);
        else
            pipe.getTransferred().merge(handler.toFacingPos(), amount, Integer::sum);

    }

    private boolean contains(ItemPipeNet.Inventory handler, boolean simulate) {
        return simulate ? simulatedTransfersGlobalRoundRobin.containsKey(handler.toFacingPos()) : pipe.getTransferred().containsKey(handler.toFacingPos());
    }

    private int didTransferTo(ItemPipeNet.Inventory handler, boolean simulate) {
        if (simulate)
            return simulatedTransfersGlobalRoundRobin.getOrDefault(handler.toFacingPos(), 0);
        return pipe.getTransferred().getOrDefault(handler.toFacingPos(), 0);
    }

    private void resetTransferred(boolean simulated) {
        if (simulated)
            simulatedTransfersGlobalRoundRobin.clear();
        else
            pipe.resetTransferred();
    }

    private void decrementBy(int amount) {
        for (Map.Entry<FacingPos, Integer> entry : pipe.getTransferred().entrySet()) {
            entry.setValue(entry.getValue() - amount);
        }
    }

    private static class EnhancedRoundRobinData {
        private final ItemPipeNet.Inventory inventory;
        private final int maxInsertable;
        private int transferred;
        private int toTransfer = 0;

        private EnhancedRoundRobinData(ItemPipeNet.Inventory inventory, int maxInsertable, int transferred) {
            this.maxInsertable = maxInsertable;
            this.transferred = transferred;
            this.inventory = inventory;
        }
    }
}
