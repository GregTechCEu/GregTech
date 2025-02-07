package gregtech.common.inventory.handlers;

import gregtech.api.items.toolitem.ToolHelper;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class ToolItemStackHandler extends SingleItemStackHandler {

    public ToolItemStackHandler(int size) {
        super(size);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return ToolHelper.isTool(stack);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (isItemValid(slot, stack)) {
            return super.insertItem(slot, stack, simulate);
        }

        return stack;
    }
}
