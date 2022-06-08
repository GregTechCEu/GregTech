package gregtech.common.tools;

import com.google.common.collect.ImmutableSet;
import gregtech.api.enchants.EnchantmentHardHammer;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import java.util.Set;

public class ToolHardHammer extends ToolBase {

    private static final Set<String> HAMMER_TOOL_CLASSES = ImmutableSet.of("hammer", "pickaxe");

    @Override
    public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
        return enchantment != EnchantmentHardHammer.INSTANCE && enchantment.type.canEnchantItem(Items.IRON_PICKAXE);
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
        ItemStack itemStack = GTUtility.toItem(block);
        return (tool != null && HAMMER_TOOL_CLASSES.contains(tool)) ||
                block.getMaterial() == Material.ROCK ||
                block.getMaterial() == Material.GLASS ||
                block.getMaterial() == Material.ICE ||
                block.getMaterial() == Material.PACKED_ICE ||
                RecipeMaps.FORGE_HAMMER_RECIPES.findRecipe(Long.MAX_VALUE, Collections.singletonList(itemStack), Collections.emptyList(), 0) != null;
    }

    @Override
    public void convertBlockDrops(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, List<ItemStack> dropList, ItemStack toolStack) {
        ToolUtility.applyHammerDrops(world.rand, blockState, dropList, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, toolStack), player);
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return HAMMER_TOOL_CLASSES;
    }

    @Override
    public void onStatsAddedToTool(MetaItem.MetaValueItem item) {
        item.addComponents(new HardHammerBehavior(DamageValues.DAMAGE_FOR_HAMMER));
    }
}
