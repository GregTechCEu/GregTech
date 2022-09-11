package gregtech.common.inventory.handlers;

import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

public class TapeItemStackHandler extends ItemStackHandler {

    public TapeItemStackHandler(int size) {
        super(size);
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
