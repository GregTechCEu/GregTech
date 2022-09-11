package gregtech.api.items.itemhandlers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

public class InaccessibleItemStackHandler extends ItemStackHandler {
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        this.stacks.set(slot, stack);
    }
}
