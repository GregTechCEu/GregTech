package gregtech.api.items.itemhandlers;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class InaccessibleItemStackHandler extends GTItemStackHandler {
    public InaccessibleItemStackHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        this.stacks.set(slot, stack);
    }
}
