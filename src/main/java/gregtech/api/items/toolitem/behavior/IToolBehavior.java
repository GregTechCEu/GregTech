package gregtech.api.items.toolitem.behavior;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Describes generic behaviour attachable to tools. Multiple behaviours can be attached to one tool.
 */
public interface IToolBehavior {

    /**
     * @param stack    The current ItemStack
     * @param target   the entity being hit
     * @param attacker the entity hitting the other
     */
    default void hitEntity(@Nonnull ItemStack stack, @Nonnull EntityLivingBase target, @Nonnull EntityLivingBase attacker) {

    }

    /**
     * Called before a block is broken.
     * <p>
     * This is called on only the server side!
     *
     * @param stack  The current ItemStack
     * @param pos    Block's position in world
     * @param player The Player that is wielding the item
     */
    default void onBlockStartBreak(@Nonnull ItemStack stack, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {

    }

    /**
     * Called when a Block is destroyed using this Item.
     *
     * @param stack        The current ItemStack
     * @param world        The current world
     * @param state        The state of the destroyed block
     * @param pos          The position of the destroyed block
     * @param entityLiving the entity destroying the block
     */
    default void onBlockDestroyed(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EntityLivingBase entityLiving) {

    }

    /**
     * Called when an entity tries to play the 'swing' animation.
     *
     * @param entityLiving The entity swinging the item.
     * @param stack        The Item stack
     */
    default void onEntitySwing(@Nonnull EntityLivingBase entityLiving, @Nonnull ItemStack stack) {

    }

    /**
     *
     * @param stack  the tool
     * @param shield the shield to disable
     * @param entity the entity holding the shield
     * @param attacker the entity attacking the shield
     * @return if the tool can disable shields
     */
    default boolean canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity, EntityLivingBase attacker) {
        return false;
    }

    /**
     * Called when a Block is right-clicked with this Item, but before the block is activated
     *
     * @param player the player clicking with the item
     * @param world  the world in which the block is clicked
     * @param pos    the position of the blocked clicked
     * @param facing the face of the block hit
     * @param hitX   the x location of the block hit
     * @param hitY   the y location of the block hit
     * @param hitZ   the z location of the block hit
     * @param hand   the hand holding the item
     */
    default EnumActionResult onItemUseFirst(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, @Nonnull EnumHand hand) {
        return EnumActionResult.PASS;
    }

    /**
     * Called when a Block is right-clicked with this Item
     *
     * @param player the player clicking with the item
     * @param world  the world in which the block is clicked
     * @param pos    the position of the blocked clicked
     * @param hand   the hand holding the item
     * @param facing the face of the block hit
     * @param hitX   the x location of the block hit
     * @param hitY   the y location of the block hit
     * @param hitZ   the z location of the block hit
     */
    @Nonnull
    default EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.PASS;
    }

    /**
     * Called when the equipped item is right-clicked.
     *
     * @param world  the world in which the click happened
     * @param player the player clicking the item
     * @param hand   the hand holding the item
     */
    @Nonnull
    default ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @SideOnly(Side.CLIENT)
    default void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flag) {

    }

    /**
     * Add the necessary NBT information to the tool
     * @param stack the tool
     * @param tag   the nbt tag to add to
     */
    default void addBehaviorNBT(@Nonnull ItemStack stack, @Nonnull NBTTagCompound tag) {

    }
}
