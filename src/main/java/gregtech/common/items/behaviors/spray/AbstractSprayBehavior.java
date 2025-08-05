package gregtech.common.items.behaviors.spray;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.color.ColoredBlockContainer;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class AbstractSprayBehavior implements IItemBehaviour {

    /**
     * Get the color of the spray can. {@code null} = solvent
     */
    public abstract @Nullable EnumDyeColor getColor(@NotNull ItemStack stack);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean canSpray(@NotNull ItemStack stack);

    public void onSpray(@NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull ItemStack sprayCan) {
        //
    }

    public @Range(from = -1, to = 15) int getColorOrdinal(@NotNull ItemStack stack) {
        EnumDyeColor color = getColor(stack);
        return color == null ? -1 : color.ordinal();
    }

    public static @Nullable AbstractSprayBehavior getSprayCanBehavior(@NotNull ItemStack stack) {
        if (!(stack.getItem() instanceof MetaItem<?>metaItem)) return null;

        for (IItemBehaviour behaviour : metaItem.getBehaviours(stack)) {
            if (behaviour instanceof AbstractSprayBehavior sprayBehavior) {
                return sprayBehavior;
            }
        }

        return null;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack sprayCan = player.getHeldItem(hand);
        EnumActionResult result = spray(player, hand, world, pos, facing, sprayCan);
        return ActionResult.newResult(result, sprayCan);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static @NotNull EnumActionResult handleExternalSpray(@NotNull EntityPlayer player, @NotNull EnumHand hand,
                                                                @NotNull World world, @NotNull BlockPos pos,
                                                                @NotNull EnumFacing facing) {
        return handleExternalSpray(player, hand, world, pos, facing, player.getHeldItem(hand));
    }

    public static @NotNull EnumActionResult handleExternalSpray(@NotNull EntityPlayer player, @NotNull EnumHand hand,
                                                                @NotNull World world, @NotNull BlockPos pos,
                                                                @NotNull EnumFacing facing,
                                                                @NotNull ItemStack sprayCan) {
        AbstractSprayBehavior sprayBehavior = getSprayCanBehavior(sprayCan);
        if (sprayBehavior == null) {
            return EnumActionResult.PASS;
        } else {
            return sprayBehavior.spray(player, hand, world, pos, facing, sprayCan);
        }
    }

    protected @NotNull EnumActionResult spray(@NotNull EntityPlayer player, @NotNull EnumHand hand,
                                              @NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                              @NotNull ItemStack sprayCan) {
        if (!canSpray(sprayCan)) {
            return EnumActionResult.PASS;
        } else if (!player.canPlayerEdit(pos, facing, sprayCan)) {
            return EnumActionResult.FAIL;
        } else if (!tryPaintBlock(player, world, pos, facing, sprayCan)) {
            return EnumActionResult.PASS;
        }

        world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        return EnumActionResult.SUCCESS;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean tryPaintBlock(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                    @NotNull EnumFacing side, @NotNull ItemStack sprayCan) {
        ColoredBlockContainer blockContainer = ColoredBlockContainer.getInstance(world, pos, side, player);
        return blockContainer.isValid() && blockContainer.setColor(getColor(sprayCan));
    }
}
