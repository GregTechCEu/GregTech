package gregtech.api.items.itemhandlers;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class InaccessibleItemStackHandler extends GTItemStackHandler {

    public InaccessibleItemStackHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

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
