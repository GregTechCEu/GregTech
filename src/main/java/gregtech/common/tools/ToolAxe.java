package gregtech.common.tools;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.Set;

public class ToolAxe extends ToolBase {

    private static final Set<String> AXE_TOOL_CLASSES = Collections.singleton("axe");

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        return enchantment.type.canEnchantItem(Items.IRON_AXE);
    }

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public int getToolDamagePerDropConversion(ItemStack stack) {
        return 1;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 3.0F;
    }

    @Override
    public float getDigSpeedMultiplier(ItemStack stack) {
        return 2.0F;
    }

    @Override
    public float getAttackSpeed(ItemStack stack) {
        return -2.6f;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        return (tool != null && AXE_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.WOOD;
    }

    @Override
    public boolean onBlockPreBreak(ItemStack stack, BlockPos blockPos, EntityPlayer player) {
        if (!player.isSneaking()) {
            return ToolUtility.applyTimberAxe(stack, player.world, blockPos, player);
        }
        return false;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return AXE_TOOL_CLASSES;
    }
}
