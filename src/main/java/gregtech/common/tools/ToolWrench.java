package gregtech.common.tools;

import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.common.items.behaviors.WrenchBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ToolWrench extends ToolBase {

    private static final Set<String> WRENCH_TOOL_CLASSES = Collections.singleton("wrench");

    @Override
    public float getNormalDamageBonus(EntityLivingBase entity, ItemStack stack, EntityLivingBase attacker) {
        String name = entity.getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        return name.toLowerCase().contains("golem") ? 3.0F : 1.0F;
    }

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 3.0F;
    }

    @Override
    public boolean canMineBlock(IBlockState blockState, ItemStack stack) {
        Block block = blockState.getBlock();
        String tool = block.getHarvestTool(blockState);
        return (tool != null && WRENCH_TOOL_CLASSES.contains(tool))
                || blockState.getMaterial() == Material.PISTON
                || block == Blocks.HOPPER
                || block == Blocks.DISPENSER
                || block == Blocks.DROPPER;
    }

    @Override
    public void onStatsAddedToTool(MetaValueItem item) {
        item.addComponents(new WrenchBehaviour(DamageValues.DAMAGE_FOR_WRENCH));
    }

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.EFFICIENCY;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return WRENCH_TOOL_CLASSES;
    }

    @Override
    public boolean canPlayBreakingSound(ItemStack stack, IBlockState state) {
        return canMineBlock(state, stack);
    }
}
