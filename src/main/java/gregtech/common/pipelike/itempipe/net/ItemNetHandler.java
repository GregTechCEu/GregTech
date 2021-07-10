package gregtech.common.pipelike.itempipe.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.tile.PipeCoverableImplementation;
import gregtech.api.util.GTLog;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ItemNetHandler implements IItemHandler {

    private final ItemPipeNet net;
    private final TileEntityItemPipe pipe;
    private final EnumFacing facing;

    public ItemNetHandler(ItemPipeNet net, TileEntityItemPipe pipe, EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;
        CoverConveyor conveyor = getCover();
        if(conveyor != null) {
            if(conveyor.getDistributionMode() == CoverConveyor.ItemDistributionMode.ROUND_ROBIN) {
                return insertRoundRobin(stack, simulate);
            }
        }
        return insertFirst(stack, simulate);
    }

    private ItemStack insertFirst(ItemStack stack, boolean simulate) {
        for (ItemPipeNet.Inventory inv : net.getNetData(pipe.getPipePos())) {
            GTLog.logger.info(" - try inserting at {} with facing {}", inv.getHandlerPos(), facing);
            if (Objects.equals(pipe.getPipePos(), inv.getPipePos()) && (facing == null || facing == inv.getFaceToHandler()))
                continue;
            IItemHandler handler = inv.getHandler(pipe.getWorld());
            if (handler == null) continue;
            stack = ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
            if (stack.isEmpty())
                return ItemStack.EMPTY;
        }
        return stack;
    }

    private ItemStack insertRoundRobin(ItemStack stack, boolean simulate) {
        List<IItemHandler> handlers = new ArrayList<>();
        for (ItemPipeNet.Inventory inv : net.getNetData(pipe.getPipePos())) {
            if (Objects.equals(pipe.getPipePos(), inv.getPipePos()) && (facing == null || facing == inv.getFaceToHandler()))
                continue;
            IItemHandler handler = inv.getHandler(pipe.getWorld());
            if (handler != null)
                handlers.add(handler);
        }
        if (handlers.size() == 0)
            return stack;
        if (handlers.size() == 1)
            return ItemHandlerHelper.insertItemStacked(handlers.get(0), stack, simulate);
        ItemStack remaining = insertToHandlers(handlers, stack, simulate);
        if (!remaining.isEmpty())
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
    private ItemStack insertToHandlers(List<IItemHandler> handlers, ItemStack stack, boolean simulate) {
        Iterator<IItemHandler> handlerIterator = handlers.iterator();
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
            IItemHandler handler = handlerIterator.next();
            int r = ItemHandlerHelper.insertItemStacked(handler, toInsert, simulate).getCount();
            if (r < stack.getCount())
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

    public CoverConveyor getCover() {
        PipeCoverableImplementation coverable = pipe.getCoverableImplementation();
        CoverBehavior coverBehavior = coverable.getCoverAtSide(facing);
        if(coverBehavior instanceof CoverConveyor && ((CoverConveyor) coverBehavior).getConveyorMode() == CoverConveyor.ConveyorMode.IMPORT)
            return (CoverConveyor) coverBehavior;
        TileEntity tile = pipe.getWorld().getTileEntity(pipe.getPos().offset(facing));
        if(tile != null) {
            ICoverable coverable1 = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
            if(coverable1 != null) {
                CoverBehavior coverBehavior1 = coverable1.getCoverAtSide(facing.getOpposite());
                if(coverBehavior1 instanceof CoverConveyor && ((CoverConveyor) coverBehavior1).getConveyorMode() == CoverConveyor.ConveyorMode.EXPORT)
                    return (CoverConveyor) coverBehavior1;
            }
        }
        return null;
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

}
