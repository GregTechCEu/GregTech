package gregtech.common.tools;

import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.RecipeMaps;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class ToolHardHammer extends ToolBase {

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        return enchantment.type.canEnchantItem(Items.IRON_PICKAXE);
    }

    @Override
    public float getNormalDamageBonus(EntityLivingBase entity, ItemStack stack, EntityLivingBase attacker) {
        String name = entity.getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        return name.toLowerCase().contains("golem") ? 2.0F : 1.0F;
    }

    @Override
    public int getToolDamagePerBlockBreak(ItemStack stack) {
        return 1;
    }

    @Override
    public int getToolDamagePerDropConversion(ItemStack stack) {
        return 0;
    }

    @Override
    public int getToolDamagePerContainerCraft(ItemStack stack) {
        return 2;
    }

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 4.0F;
    }

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 2.0f;
    }

    @Override
    public float getDigSpeedMultiplier(ItemStack stack) {
        return 1.5f;
    }

    @Override
    public float getAttackSpeed(ItemStack stack) {
        return -3.0f;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        String tool = block.getBlock().getHarvestTool(block);
        ItemStack itemStack = new ItemStack(block.getBlock(), 1, block.getBlock().getMetaFromState(block));
        return (tool != null && (tool.equals("hammer") || tool.equals("pickaxe"))) ||
            block.getMaterial() == Material.ROCK ||
            block.getMaterial() == Material.GLASS ||
            block.getMaterial() == Material.ICE ||
            block.getMaterial() == Material.PACKED_ICE ||
            RecipeMaps.FORGE_HAMMER_RECIPES.findRecipe(Long.MAX_VALUE, Collections.singletonList(itemStack), Collections.emptyList(), 0, MatchingMode.DEFAULT) != null;
    }

    @Override
    public void convertBlockDrops(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, List<ItemStack> dropList, ItemStack toolStack) {
        ToolUtility.applyHammerDrops(world.rand, blockState, dropList, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, toolStack), player);
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines, boolean isAdvanced) {
        lines.add(I18n.format("metaitem.tool.tooltip.hammer.extra_drop"));
    }
}
