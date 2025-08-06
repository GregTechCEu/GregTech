package gregtech.common.items.behaviors.spray;

import gregtech.api.color.ColoredBlockContainer;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.PipeCollectorWalker;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

    public int getColorInt(@NotNull ItemStack stack) {
        EnumDyeColor color = getColor(stack);
        return color == null ? -1 : color.colorValue;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canSpray(@NotNull ItemStack stack) {
        return true;
    }

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

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack sprayCan = player.getHeldItem(hand);
        EnumActionResult result = spray(player, hand, world, pos, facing, sprayCan);
        return ActionResult.newResult(result, sprayCan);
    }

    protected @NotNull EnumActionResult spray(@NotNull EntityPlayer player, @NotNull EnumHand hand,
                                              @NotNull World world, @NotNull BlockPos pos,
                                              @NotNull EnumFacing facing,
                                              @NotNull ItemStack sprayCan) {
        if (!canSpray(sprayCan)) {
            return EnumActionResult.PASS;
        } else if (!player.canPlayerEdit(pos, facing, sprayCan)) {
            return EnumActionResult.FAIL;
        }

        if (player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IPipeTile<?, ?>pipeTile && (pipeTile.isPainted() ?
                    pipeTile.getPaintingColor() != getColorInt(sprayCan) : getColor(sprayCan) != null)) {
                traversePipes(world, player, hand, pos, pipeTile, sprayCan);
                world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                        SoundCategory.PLAYERS, 1.0f, 1.0f);
                return EnumActionResult.SUCCESS;
            }
        }

        ColoredBlockContainer blockContainer = ColoredBlockContainer.getInstance(world, pos, facing, player);
        if (blockContainer.isValid() && blockContainer.setColor(getColor(sprayCan))) {
            onSpray(player, hand, sprayCan);
            world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                    SoundCategory.PLAYERS, 1.0f, 1.0f);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    protected void traversePipes(@NotNull World world, @NotNull EntityPlayer player, @NotNull EnumHand hand,
                                 @NotNull BlockPos startPos, @NotNull IPipeTile<?, ?> startingPipe,
                                 @NotNull ItemStack sprayCan) {
        EnumDyeColor dyeColor = getColor(sprayCan);
        int color = dyeColor == null ? -1 : dyeColor.colorValue;
        boolean[] metSplit = { false };
        PipeCollectorWalker.collectPipeNet(world, startPos, startingPipe, pipe -> {
            if (metSplit[0] || !canSpray(sprayCan)) {
                return false;
            }

            if (pipe.getPaintingColor() != color) {
                pipe.setPaintingColor(color);
                pipe.scheduleRenderUpdate();
                onSpray(player, hand, sprayCan);
            }

            if (pipe.getNumConnections() > 2) {
                // Returning false here will not stop subwalkers from continuing, so make them exit immediately later
                metSplit[0] = true;
                return false;
            }

            return true;
        });
    }
}
