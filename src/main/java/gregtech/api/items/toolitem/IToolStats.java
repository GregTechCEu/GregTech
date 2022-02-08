package gregtech.api.items.toolitem;

import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

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
     * @return Damage the Tool receives when breaking a Block. 100 is one Damage Point (or 100 EU).
     */
    int getToolDamagePerBlockBreak(ItemStack stack);

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
    default float getBaseEfficiency(ItemStack stack) {
        return 1.0f;
    }

    boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment);

    /**
     * block.getHarvestTool(metaData) can return the following Values for example.
     * "axe", "pickaxe", "sword", "shovel", "hoe", "grafter", "saw", "wrench", "crowbar", "file", "hammer", "plow", "plunger", "scoop", "screwdriver", "sense", "scythe", "softhammer", "cutter", "plasmatorch"
     *
     * @return If this is a minable Block. Tool Quality checks (like Diamond Tier or something) are separate from this check.
     */
    boolean canMineBlock(IBlockState block, ItemStack stack);

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
     * @return attack speed of weapon
     */
    default float getAttackSpeed(ItemStack stack) {
        return 0.0f;
    }

    default boolean canPlayBreakingSound(ItemStack stack, IBlockState state) {
        return false;
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
