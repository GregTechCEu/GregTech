package gregtech.api.items.toolitem;

import gregtech.api.enchants.EnchantmentData;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.unification.material.Material;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The Stats for GT Tools. Not including any Material Modifiers.
 */
public interface IToolStats {

    /**
     * Called when aPlayer crafts this Tool
     */
    default void onToolCrafted(ItemStack stack, EntityPlayer player) {
    }

    /**
     * Called when this gets added to a Tool Item
     */
    default void onStatsAddedToTool(MetaValueItem metaValueItem) {
    }

    /**
     * @return Damage the Tool receives when breaking a Block. 100 is one Damage Point (or 100 EU).
     */
    int getToolDamagePerBlockBreak(ItemStack stack);

    /**
     * @return Damage the Tool receives when converting the drops of a Block. 100 is one Damage Point (or 100 EU).
     */
    default int getToolDamagePerDropConversion(ItemStack stack) {
        return 0; //we are not converting anything by default
    }

    /**
     * @return Damage the Tool receives when being used as Container Item. 100 is one use, however it is usually 8 times more than normal.
     */
    int getToolDamagePerContainerCraft(ItemStack stack);

    /**
     * @return Damage the Tool receives when being used as Weapon, 200 is the normal Value, 100 for actual Weapons.
     */
    int getToolDamagePerEntityAttack(ItemStack stack);

    /**
     * @return Basic Quality of the Tool, 0 is normal. If increased, it will increase the general quality of all Tools of this Type. Decreasing is also possible.
     */
    default int getBaseQuality(ItemStack stack) {
        return 0;
    }

    /**
     * @return The Damage Bonus for this Type of Tool against Mobs. 1.0F is normal punch.
     */
    default float getBaseDamage(ItemStack stack) {
        return 1.0f;
    }

    /**
     * @return This is a multiplier for the Tool Speed. 1.0F = no special Speed.
     */
    default float getDigSpeedMultiplier(ItemStack stack) {
        return 1.0f;
    }

    /**
     * @return This is a multiplier for the Tool Durability. 1.0F = no special Durability.
     */
    default float getMaxDurabilityMultiplier(ItemStack stack) {
        return 1.0f;
    }

    default List<EnchantmentData> getEnchantments(ItemStack stack) {
        return Collections.emptyList();
    }

    boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment);

    /**
     * block.getHarvestTool(metaData) can return the following Values for example.
     * "axe", "pickaxe", "sword", "shovel", "hoe", "grafter", "saw", "wrench", "crowbar", "file", "hammer", "plow", "plunger", "scoop", "screwdriver", "sense", "scythe", "softhammer", "cutter", "plasmatorch"
     *
     * @return If this is a minable Block. Tool Quality checks (like Diamond Tier or something) are separate from this check.
     */
    boolean canMineBlock(IBlockState block, ItemStack stack);

    /**
     * @return If this Tool can be used as an RC Crowbar.
     */
    default boolean isCrowbar(ItemStack stack) {
        return false;
    }

    default float getSaplingModifier(ItemStack stack, World world, EntityPlayer player, BlockPos pos) {
        return 0.0f;
    }

    default List<BlockPos> getAOEBlocks(ItemStack itemStack, EntityPlayer player, RayTraceResult rayTraceResult) {
        return Collections.emptyList();
    }

    default void onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase entity) {
    }

    default boolean onBlockPreBreak(ItemStack stack, BlockPos blockPos, EntityPlayer player) {
        return false;
    }

    default void convertBlockDrops(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, List<ItemStack> dropList, ItemStack toolStack) {
    }

    default void addInformation(ItemStack stack, List<String> lines, boolean isAdvanced) {
    }

    /**
     * @return the Damage actually done to the Mob.
     */
    default float getNormalDamageBonus(EntityLivingBase entity, ItemStack stack, EntityLivingBase attacker) {
        return 0.0f;
    }

    /**
     * @return the Damage actually done to the Mob.
     */
    default float getMagicDamageBonus(EntityLivingBase entity, ItemStack stack, EntityLivingBase player) {
        return 0.0f;
    }

    /**
     * @return attack speed of weapon
     */
    default float getAttackSpeed(ItemStack stack) {
        return -2.8f;
    }

    default boolean canPerformSweepAttack(ItemStack stack) {
        return false;
    }

    default boolean isUsingDurability(ItemStack stack) {
        return true;
    }

    default boolean canPlayBreakingSound(ItemStack stack, IBlockState state) {
        return false;
    }

    default ItemStack getBrokenStack(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    default int getColor(ItemStack stack, int tintIndex) {
        Material primaryMaterial = ToolMetaItem.getToolMaterial(stack);
        return tintIndex % 2 == 1 ? primaryMaterial.getMaterialRGB() : 0xFFFFFF;
    }

    /**
     * @return The MC tool classes for cross-mod compatibility.
     * Default: no tool classes.
     */
    default Set<String> getToolClasses(ItemStack stack) {
        return Collections.emptySet();
    }

    default void onCraftingUse(ItemStack stack, EntityPlayer player) {
        if (ConfigHolder.client.toolCraftingSounds && stack.getItem() instanceof ToolMetaItem<?>) {
            //noinspection ConstantConditions
            if (((ToolMetaItem<?>) stack.getItem()).canPlaySound(stack) && player != null && player.getEntityWorld() != null) {
                ((ToolMetaItem<?>) stack.getItem()).setCraftingSoundTime(stack);
                player.getEntityWorld().playSound(null, player.getPosition(), ((ToolMetaItem<?>) stack.getItem()).getItem(stack).getSound(), SoundCategory.PLAYERS, 1, 1);
            }
        }
    }

    default void onBreakingUse(ItemStack stack, World world, BlockPos pos) {
        if (ConfigHolder.client.toolUseSounds && stack.getItem() instanceof ToolMetaItem<?> && this.canPlayBreakingSound(stack, world.getBlockState(pos)))
            world.playSound(null, pos, ((ToolMetaItem<?>) stack.getItem()).getItem(stack).getSound(), SoundCategory.PLAYERS, 1, 1);
    }

    static void onOtherUse(@Nonnull ItemStack stack, World world, BlockPos pos) {
        if (stack.getItem() instanceof ToolMetaItem<?>) {
            IToolStats stats = ((ToolMetaItem<?>) stack.getItem()).getItem(stack).getToolStats();
            if (ConfigHolder.client.toolUseSounds && stack.getItem() instanceof ToolMetaItem<?>)
                world.playSound(null, pos, ((ToolMetaItem<?>) stack.getItem()).getItem(stack).getSound(), SoundCategory.PLAYERS, 1, 1);
        }
    }
}
