package gregtech.common.tools;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ToolPickaxe extends ToolBase {

    private static final Set<String> PICK_TOOL_CLASSES = Collections.singleton("pickaxe");

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        if(enchantment.type == null) {
            return false;
        }

        return enchantment.type.canEnchantItem(Items.IRON_PICKAXE);
    }

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 1.5F;
    }

    @Override
    public float getDigSpeedMultiplier(ItemStack stack) {
        return 1.24f;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && PICK_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.ROCK ||
                block.getMaterial() == Material.IRON ||
                block.getMaterial() == Material.ANVIL ||
                block.getMaterial() == Material.GLASS;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return PICK_TOOL_CLASSES;
    }
}
