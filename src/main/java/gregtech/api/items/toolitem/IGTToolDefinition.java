package gregtech.api.items.toolitem;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

/**
 * GT Tool Definition
 */
public interface IGTToolDefinition {

    /**
     * Durability Spec
     */
    int getToolDamagePerBlockBreak(ItemStack stack);

    int getToolDamagePerContainerCraft(ItemStack stack);

    int getToolDamagePerEntityAttack(ItemStack stack);

    /**
     * Tool Stat
     */
    default int getBaseQuality(ItemStack stack) {
        return 0;
    }

    default float getBaseDamage(ItemStack stack) {
        return 1.0f;
    }

    default float getBaseEfficiency(ItemStack stack) {
        return 1.0f;
    }

    default float getAttackSpeed(ItemStack stack) {
        return 0.0f;
    }

    /**
     * Enchantments
     */
    default boolean isEnchantable(ItemStack stack) {
        return true;
    }

    boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment);

    /**
     * Misc
     */
    boolean doesSneakBypassUse();

    default ItemStack getBrokenStack() {
        return ItemStack.EMPTY;
    }

}
