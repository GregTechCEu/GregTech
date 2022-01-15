package gregtech.common.tools;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ToolSword extends ToolBase {

    private static final Set<String> SWORD_TOOL_CLASSES = Collections.singleton("sword");

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        if(enchantment.type == null) {
            return false;
        }

        return enchantment.type.canEnchantItem(Items.IRON_SWORD);
    }

    @Override
    public float getAttackSpeed(ItemStack stack) {
        return -2.3f;
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
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 5.0F;
    }

    @Override
    public boolean canPerformSweepAttack(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && SWORD_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.LEAVES ||
                block.getMaterial() == Material.GOURD ||
                block.getMaterial() == Material.VINE ||
                block.getMaterial() == Material.WEB ||
                block.getMaterial() == Material.CLOTH ||
                block.getMaterial() == Material.CARPET ||
                block.getMaterial() == Material.PLANTS ||
                block.getMaterial() == Material.CACTUS ||
                block.getMaterial() == Material.CAKE ||
                block.getMaterial() == Material.TNT ||
                block.getMaterial() == Material.SPONGE;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return SWORD_TOOL_CLASSES;
    }
}
