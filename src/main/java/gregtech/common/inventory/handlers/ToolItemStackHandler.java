package gregtech.common.inventory.handlers;

import gregtech.api.items.toolitem.ToolMetaItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

import org.jetbrains.annotations.NotNull;

public class ToolItemStackHandler extends SingleItemStackHandler {

    public ToolItemStackHandler(int size) {
        super(size);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!(stack.getItem() instanceof ToolMetaItem)
                && !(stack.getItem() instanceof ItemTool)
                && !(stack.isItemStackDamageable())) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }
}
