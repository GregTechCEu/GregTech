package gregtech.common.inventory.handlers;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class TapeItemStackHandler extends GTItemStackHandler {

    public TapeItemStackHandler(MetaTileEntity metaTileEntity, int size) {
        super(metaTileEntity, size);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!stack.isEmpty() && stack.isItemEqual(MetaItems.DUCT_TAPE.getStackForm())) {
            return super.insertItem(slot, stack, simulate);
        }
        return stack;
    }
}
