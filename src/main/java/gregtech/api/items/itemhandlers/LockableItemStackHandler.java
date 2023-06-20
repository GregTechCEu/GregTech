package gregtech.api.items.itemhandlers;

import gregtech.api.capability.ILockableItemHandler;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class LockableItemStackHandler extends NotifiableItemStackHandler implements ILockableItemHandler {
    protected boolean locked = false;
    protected ItemStack lockedItemStack;
    public LockableItemStackHandler(MetaTileEntity entityToNotify, boolean isExport) {
        super(1, entityToNotify, isExport);
    }

    public void lock() {
        this.locked = true;
        lockedItemStack = this.getStackInSlot(0).copy();
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean isLocked() {
        return this.locked;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (this.locked) {
            if (!this.lockedItemStack.isItemEqual(stack)) {
                return stack;
            }
        }
        return super.insertItem(slot, stack, simulate);
    }
}
