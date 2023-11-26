package gregtech.common.inventory.handlers;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class TapeItemStackHandler extends GTItemStackHandler {

    public TapeItemStackHandler(MetaTileEntity metaTileEntity, int size) {
        super(metaTileEntity, size);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!stack.isEmpty() && stack.isItemEqual(MetaItems.DUCT_TAPE.getStackForm())) {
            return super.insertItem(slot, stack, simulate);
        }
        return stack;
    }
}
