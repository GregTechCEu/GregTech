package gregtech.common.inventory.handlers;

import gregtech.api.unification.OreDictUnifier;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ToolItemStackHandler extends SingleItemStackHandler {

    public ToolItemStackHandler(int size) {
        super(size);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isItemStackDamageable() &&
                (!stack.getItem().getToolClasses(stack).isEmpty() ||
                        OreDictUnifier.getOreDictionaryNames(stack).stream().anyMatch(s -> s.startsWith("craftingTool")))) {
            return super.insertItem(slot, stack, simulate);
        }
        return stack;
    }
}
