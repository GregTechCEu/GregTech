package gregtech.common.inventory.handlers;

import gregtech.api.items.toolitem.ToolMetaItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

import javax.annotation.Nonnull;

public class ToolItemStackHandler extends SingleItemStackHandler {

    public ToolItemStackHandler(int size) {
        super(size);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (!(stack.getItem() instanceof ToolMetaItem)
                && !(stack.getItem() instanceof ItemTool)
                && !(stack.isItemStackDamageable())) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }
}
