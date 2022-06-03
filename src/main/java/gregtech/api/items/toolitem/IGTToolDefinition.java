package gregtech.api.items.toolitem;

import gregtech.api.items.toolitem.behaviour.IToolBehaviour;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * GT Tool Definition
 */
public interface IGTToolDefinition {

    /**
     * Tool Component/Behaviours
     */
    List<IToolBehaviour> getBehaviours();

    boolean isToolEffective(IBlockState state);

    /**
     * Durability Spec
     */
    int getDamagePerAction(ItemStack stack);

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
        int action = getDamagePerAction(stack);
        return isSuitableForCrafting(stack) ? action : action * 2;
    }

    /**
     * Tool Stat
     */
    default int getBaseDurability(ItemStack stack) {
        return 0;
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

    default AoEDefinition getAoEDefinition(ItemStack stack) {
        return AoEDefinition.none();
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
