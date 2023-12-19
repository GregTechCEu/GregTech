package gregtech.common.inventory.handlers;

import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.unification.OreDictUnifier;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class ToolItemStackHandler extends SingleItemStackHandler {

    public ToolItemStackHandler(int size) {
        super(size);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.getItem().getToolClasses(stack).isEmpty()) return stack;
        if (stack.getItem() instanceof IGTTool &&
                ((IGTTool) stack.getItem()).getToolStats().isSuitableForCrafting(stack)) {
            return super.insertItem(slot, stack, simulate);
        }

        if (stack.isItemStackDamageable() && OreDictUnifier.getOreDictionaryNames(stack).stream()
                .anyMatch(s -> s.startsWith("craftingTool"))) {
            return super.insertItem(slot, stack, simulate);
        }
        return stack;
    }
}
