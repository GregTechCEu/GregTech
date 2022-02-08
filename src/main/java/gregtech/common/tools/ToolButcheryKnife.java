package gregtech.common.tools;

import com.google.common.collect.Lists;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ToolButcheryKnife extends ToolBase {

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        return enchantment.type.canEnchantItem(Items.IRON_SWORD);
    }

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 2;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 1;
    }

    @Override
    public int getToolDamagePerEntityAttack(ItemStack stack) {
        return 4;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 6.0f;
    }

    @Override
    public float getAttackSpeed(ItemStack stack) {
        return -3.5f;
    }

    @Override
    public List<EnchantmentData> getEnchantments(ItemStack stack) {
        return Lists.newArrayList(new EnchantmentData(Enchantments.LOOTING, 3));
    }
}
