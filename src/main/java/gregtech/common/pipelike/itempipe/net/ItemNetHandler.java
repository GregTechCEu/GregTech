package gregtech.common.pipelike.itempipe.net;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.tile.PipeCoverableImplementation;
import gregtech.api.util.GTLog;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
        if (conveyor != null) {
            if (conveyor.getDistributionMode() == CoverConveyor.ItemDistributionMode.ROUND_ROBIN) {
                return insertRoundRobin(stack, simulate);
            }
        }
        return insertFirst(stack, simulate);
    }

    public ItemStack insertFirst(ItemStack stack, boolean simulate) {
        for (ItemPipeNet.Inventory inv : net.getNetData(pipe.getPipePos())) {
            GTLog.logger.info(" - try inserting at {} with facing {}", inv.getHandlerPos(), facing);
            if (Objects.equals(pipe.getPipePos(), inv.getPipePos()) && (facing == null || facing == inv.getFaceToHandler()))
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
            if(inv.getDistance() > inv.getProperties().maxRange)
                continue;
            if (Objects.equals(pipe.getPipePos(), inv.getPipePos()) && (facing == null || facing == inv.getFaceToHandler()))
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

    public ItemStack insert(Handler handler, ItemStack stack, boolean simulate) {
        //GTLog.logger.info("Inserting in handler with dist {} and stack {} and prop {}", handler.getDistance(), stack, handler.getProperties());
        if(handler.getDistance() > handler.getProperties().maxRange)
            return stack;
        CoverConveyor conveyor = getCoverAtPipe(handler.getPipePos(), handler.getFaceToHandler());
        if(conveyor != null) {
            if(!conveyor.getItemFilterContainer().testItemStack(stack))
                return stack;
        }

        int allowed = ((TileEntityItemPipeTickable)pipe).checkTransferableItems((int) ((handler.getProperties().transferRate * 64) + 0.5), stack.getCount());
        if(allowed == 0) return stack;
        ItemStack toInsert = stack.copy();
        toInsert.setCount(allowed);
        GTLog.logger.info("Inserting {}, allowed {}", stack, toInsert);
        int r = ItemHandlerHelper.insertItemStacked(handler.handler, toInsert, simulate).getCount();
        if(!simulate) ((TileEntityItemPipeTickable)pipe).transferItems(allowed - r);
        ItemStack remainder = stack.copy();
        remainder.setCount(r + (stack.getCount() - allowed));
        GTLog.logger.info("Remainder {}", remainder);
        return remainder;
    }

    public CoverConveyor getCoverAtPipe(BlockPos pipePos, EnumFacing handlerFacing) {
        TileEntity tile = pipe.getWorld().getTileEntity(pipePos);
        if (tile instanceof TileEntityItemPipe) {
            ICoverable coverable = ((TileEntityItemPipe) tile).getCoverableImplementation();
            CoverBehavior cover = coverable.getCoverAtSide(handlerFacing);
            if (cover instanceof CoverConveyor) return (CoverConveyor) cover;
        }
        tile = pipe.getWorld().getTileEntity(pipePos.offset(handlerFacing));
        if (tile != null) {
            ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
            if (coverable == null) return null;
            CoverBehavior cover = coverable.getCoverAtSide(handlerFacing.getOpposite());
            if (cover instanceof CoverConveyor) return (CoverConveyor) cover;
        }
        return null;
    }

    public CoverConveyor getCover() {
        PipeCoverableImplementation coverable = pipe.getCoverableImplementation();
        CoverBehavior coverBehavior = coverable.getCoverAtSide(facing);
        if (coverBehavior instanceof CoverConveyor && ((CoverConveyor) coverBehavior).getConveyorMode() == CoverConveyor.ConveyorMode.IMPORT)
            return (CoverConveyor) coverBehavior;
        TileEntity tile = pipe.getWorld().getTileEntity(pipe.getPos().offset(facing));
        if (tile != null) {
            ICoverable coverable1 = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
            if (coverable1 == null) return null;
            CoverBehavior coverBehavior1 = coverable1.getCoverAtSide(facing.getOpposite());
            if (coverBehavior1 instanceof CoverConveyor && ((CoverConveyor) coverBehavior1).getConveyorMode() == CoverConveyor.ConveyorMode.EXPORT)
                return (CoverConveyor) coverBehavior1;
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

    private static class Handler extends ItemPipeNet.Inventory {
        private final IItemHandler handler;

        private Handler(IItemHandler handler, ItemPipeNet.Inventory inventory) {
            super(inventory.getPipePos(), inventory.getFaceToHandler(), inventory.getDistance(), inventory.getProperties());
            this.handler = handler;
        }
    }
}
