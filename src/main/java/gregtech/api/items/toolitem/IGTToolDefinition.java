package gregtech.api.items.toolitem;

import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import java.util.List;

/**
 * GT Tool Definition
 */
public interface IGTToolDefinition {

    /**
     * Tool Component/Behaviours
     */
    List<IToolBehavior> getBehaviors();

    boolean isToolEffective(IBlockState state);

    /**
     * Durability Spec
     */
    int getDamagePerAction(ItemStack stack);

    int getDamagePerCraftingAction(ItemStack stack);

    boolean isSuitableForBlockBreak(ItemStack stack);

    boolean isSuitableForAttacking(ItemStack stack);

    boolean isSuitableForCrafting(ItemStack stack);

    default int getToolDamagePerBlockBreak(ItemStack stack) {
        int action = getDamagePerAction(stack);
        return isSuitableForBlockBreak(stack) ? action : action * 2;
    }

    default int getToolDamagePerAttack(ItemStack stack) {
        int action = getDamagePerAction(stack);
        return isSuitableForAttacking(stack) ? action : action * 2;
    }

    default int getToolDamagePerCraft(ItemStack stack) {
        int action = getDamagePerCraftingAction(stack);
        return isSuitableForCrafting(stack) ? action : action * 2;
    }

    /**
     * Tool Stat
     */
    default int getBaseDurability(ItemStack stack) {
        return 0;
    }

    default float getDurabilityMultiplier(ItemStack stack) {
        return 1f;
    }

    default int getBaseQuality(ItemStack stack) {
        return 0;
    }

    default float getBaseDamage(ItemStack stack) {
        return 1.0F;
    }

    default float getBaseEfficiency(ItemStack stack) {
        return 1.0F;
    }

    default float getEfficiencyMultiplier(ItemStack stack) {
        return 1.0F;
    }

    default float getAttackSpeed(ItemStack stack) {
        return 0.0F;
    }

    default AoESymmetrical getAoEDefinition(ItemStack stack) {
        return AoESymmetrical.none();
    }

    /**
     * Enchantments
     */
    default boolean isEnchantable(ItemStack stack) {
        return true;
    }

    boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment);

    Object2ObjectMap<Enchantment, EnchantmentLevel> getDefaultEnchantments(ItemStack stack);

    /**
     * Misc
     */
    boolean doesSneakBypassUse();

    default ItemStack getBrokenStack() {
        return ItemStack.EMPTY;
    }
}
