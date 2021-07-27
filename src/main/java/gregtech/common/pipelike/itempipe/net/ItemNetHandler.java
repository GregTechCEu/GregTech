package gregtech.common.pipelike.itempipe.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackKey;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverRoboticArm;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.*;

public class ItemNetHandler implements IItemHandler {

    private final ItemPipeNet net;
    private final TileEntityItemPipeTickable pipe;
    private final EnumFacing facing;
    private int simulatedTransfers = 0;

    public ItemNetHandler(ItemPipeNet net, TileEntityItemPipe pipe, EnumFacing facing) {
        this.net = net;
        if (pipe instanceof TileEntityItemPipeTickable)
            this.pipe = (TileEntityItemPipeTickable) pipe;
        else
            this.pipe = (TileEntityItemPipeTickable) pipe.setSupportsTicking();
        this.facing = facing;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;
        simulatedTransfers = 0;
        Tuple<CoverConveyor, Boolean> tuple = getCoverAtPipe(pipe.getPos(), facing);
        if (exportsToPipe(tuple)) {
            if (tuple.getFirst().getDistributionMode() == CoverConveyor.ItemDistributionMode.ROUND_ROBIN) {
                return insertRoundRobin(stack, simulate);
            }
        }
        return insertFirst(stack, simulate);
    }

    public ItemStack insertFirst(ItemStack stack, boolean simulate) {
        for (ItemPipeNet.Inventory inv : net.getNetData(pipe.getPipePos())) {
            if (GTUtility.arePosEqual(pipe.getPipePos(), inv.getPipePos()) && (facing == null || facing == inv.getFaceToHandler()))
                continue;
            IItemHandler handler = inv.getHandler(pipe.getWorld());
            if (handler == null) continue;
            stack = insert(new Handler(handler, inv), stack, simulate);
            if (stack.isEmpty())
                return ItemStack.EMPTY;
        }
        return stack;
    }

    public ItemStack insertRoundRobin(ItemStack stack, boolean simulate) {
        List<Handler> handlers = new ArrayList<>();
        for (ItemPipeNet.Inventory inv : net.getNetData(pipe.getPipePos())) {
            if (GTUtility.arePosEqual(pipe.getPipePos(), inv.getPipePos()) && (facing == null || facing == inv.getFaceToHandler()))
                continue;
            IItemHandler handler = inv.getHandler(pipe.getWorld());
            if (handler != null)
                handlers.add(new Handler(handler, inv));
        }
        if (handlers.size() == 0)
            return stack;
        if (handlers.size() == 1)
            return insert(handlers.get(0), stack, simulate);
        ItemStack remaining = insertToHandlers(handlers, stack, simulate);
        if (!remaining.isEmpty() && handlers.size() > 0)
            remaining = insertToHandlers(handlers, remaining, simulate);
        return remaining;
    }

    /**
     * Inserts items equally to all handlers
     * if it couldn't insert all items, the handler will be removed
     *
     * @param handlers to insert to
     * @param stack    to insert
     * @param simulate simulate
     * @return remainder
     */
    public ItemStack insertToHandlers(List<Handler> handlers, ItemStack stack, boolean simulate) {
        Iterator<Handler> handlerIterator = handlers.iterator();
        boolean didInsert = false;
        int remaining = 0;
        int count = stack.getCount();
        int c = count / handlers.size();
        int m = count % handlers.size();
        while (handlerIterator.hasNext()) {
            int amount = c;
            if (m > 0) {
                amount++;
                m--;
            }
            if (amount == 0) break;
            ItemStack toInsert = stack.copy();
            toInsert.setCount(amount);
            Handler handler = handlerIterator.next();
            int r = insert(handler, toInsert, simulate).getCount();
            if (r < amount)
                didInsert = true;
            if (r > 0) {
                handlerIterator.remove();
                remaining += r;
            }
        }
        if (remaining == 0) {
            if (didInsert)
                return ItemStack.EMPTY;
            return stack;
        }
        ItemStack result = stack.copy();
        result.setCount(remaining);
        return result;
    }

    public ItemStack insert(Handler handler, ItemStack stack, boolean simulate) {
        int allowed = checkTransferable(pipe, handler.getProperties().transferRate, stack.getCount(), simulate);
        if (allowed == 0) return stack;
        Tuple<CoverConveyor, Boolean> tuple = getCoverAtPipe(handler.getPipePos(), handler.getFaceToHandler());
        if (tuple != null) {
            if (!tuple.getFirst().getItemFilterContainer().testItemStack(stack))
                return stack;
            boolean exportsFromPipe = exportsToPipe(tuple);
            if (tuple.getFirst() instanceof CoverRoboticArm && !exportsFromPipe)
                return insertOverRobotArm(handler.handler, (CoverRoboticArm) tuple.getFirst(), tuple.getSecond(), stack, simulate, allowed);
            if (exportsFromPipe && tuple.getFirst().blocksInput())
                return stack;
        }
        return insert(handler.handler, stack, simulate, allowed);
    }

    private ItemStack insert(IItemHandler handler, ItemStack stack, boolean simulate, int allowed) {
        if (stack.getCount() == allowed) {
            ItemStack re = ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
            transfer(pipe, simulate, stack.getCount() - re.getCount());
            return re;
        }
        ItemStack toInsert = stack.copy();
        toInsert.setCount(Math.min(allowed, stack.getCount()));
        int r = ItemHandlerHelper.insertItemStacked(handler, toInsert, simulate).getCount();
        transfer(pipe, simulate, toInsert.getCount() - r);
        ItemStack remainder = stack.copy();
        remainder.setCount(r + (stack.getCount() - toInsert.getCount()));
        return remainder;
    }

    public Tuple<CoverConveyor, Boolean> getCoverAtPipe(BlockPos pipePos, EnumFacing handlerFacing) {
        TileEntity tile = pipe.getWorld().getTileEntity(pipePos);
        if (tile instanceof TileEntityItemPipe) {
            ICoverable coverable = ((TileEntityItemPipe) tile).getCoverableImplementation();
            CoverBehavior cover = coverable.getCoverAtSide(handlerFacing);
            if (cover instanceof CoverConveyor) return new Tuple<>((CoverConveyor) cover, true);
        }
        tile = pipe.getWorld().getTileEntity(pipePos.offset(handlerFacing));
        if (tile != null) {
            ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
            if (coverable == null) return null;
            CoverBehavior cover = coverable.getCoverAtSide(handlerFacing.getOpposite());
            if (cover instanceof CoverConveyor) return new Tuple<>((CoverConveyor) cover, false);
        }
        return null;
    }

    public boolean exportsToPipe(Tuple<CoverConveyor, Boolean> tuple) {
        return tuple != null && (tuple.getSecond() ?
                tuple.getFirst().getConveyorMode() == CoverConveyor.ConveyorMode.IMPORT :
                tuple.getFirst().getConveyorMode() == CoverConveyor.ConveyorMode.EXPORT);
    }

    public ItemStack insertOverRobotArm(IItemHandler handler, CoverRoboticArm arm, boolean isOnPipe, ItemStack stack, boolean simulate, int allowed) {
        int rate;
        boolean isStackSpecific = false;
        Object index = arm.getItemFilterContainer().matchItemStack(stack);
        if (index instanceof Integer) {
            rate = arm.getItemFilterContainer().getSlotTransferLimit(index, Collections.singleton(new ItemStackKey(stack)));
            isStackSpecific = true;
        } else
            rate = arm.getItemFilterContainer().getTransferStackSize();
        int count;
        switch (arm.getTransferMode()) {
            case TRANSFER_ANY:
                return insert(handler, stack, simulate, allowed);
            case KEEP_EXACT:
                count = rate - countStack(handler, stack, arm, isStackSpecific);
                if (count <= 0) return stack;
                count = Math.min(allowed, Math.min(stack.getCount(), count));
                return insert(handler, stack, simulate, count);
            case TRANSFER_EXACT:
                int max = allowed + arm.getBuffer();
                count = Math.min(max, Math.min(rate, stack.getCount()));
                if (count < rate) {
                    arm.buffer(allowed);
                    return stack;
                } else {
                    arm.clearBuffer();
                }
                if (insert(handler, stack, true, count).getCount() != stack.getCount() - count) {
                    return stack;
                }
                return insert(handler, stack, simulate, count);
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

    private int checkTransferable(TileEntityItemPipeTickable pipe, float rate, int amount, boolean simulate) {
        int max = (int) ((rate * 64) + 0.5);
        if (simulate)
            return Math.max(0, Math.min(max - (pipe.getTransferredItems() + simulatedTransfers), amount));
        else
            return Math.max(0, Math.min(max - pipe.getTransferredItems(), amount));
    }

    private void transfer(TileEntityItemPipeTickable pipe, boolean simulate, int amount) {
        if (simulate)
            simulatedTransfers += amount;
        else
            pipe.transferItems(amount);
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

    private static class Handler extends ItemPipeNet.Inventory {
        private final IItemHandler handler;

        private Handler(IItemHandler handler, ItemPipeNet.Inventory inventory) {
            super(inventory.getPipePos(), inventory.getFaceToHandler(), inventory.getDistance(), inventory.getProperties());
            this.handler = handler;
        }
    }
}
