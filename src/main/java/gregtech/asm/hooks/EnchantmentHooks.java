package gregtech.asm.hooks;

import gregtech.api.items.toolitem.IGTTool;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
public class EnchantmentHooks {

    public static boolean checkTool(boolean initialReturn, ItemStack stack, Enchantment enchantment) {
        if (stack.getItem() instanceof IGTTool) {
            return initialReturn && stack.getItem().canApplyAtEnchantingTable(stack, enchantment);
        }
        return initialReturn;
    }
}
