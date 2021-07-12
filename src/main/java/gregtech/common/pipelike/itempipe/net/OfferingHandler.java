package gregtech.common.pipelike.itempipe.net;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class OfferingHandler implements IItemHandler, IItemHandlerModifiable {

    private ItemStack stack;

    public OfferingHandler(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean b) {
        return itemStack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack remainder = stack.copy();
        int c = Math.min(amount, stack.getCount());
        if(!simulate) stack.setCount(c);
        remainder.shrink(c);
        return remainder;
    }

    @Override
    public int getSlotLimit(int i) {
        return 64;
    }

    @Override
    public void setStackInSlot(int i, @Nonnull ItemStack itemStack) {
        stack = itemStack;
    }
}
