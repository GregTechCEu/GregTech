package gregtech.api.items.metaitem.stats;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public interface IEnchantabilityHelper extends IItemComponent {

    boolean isEnchantable(ItemStack stack);

    int getItemEnchantability(ItemStack stack);

    boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment);
}
