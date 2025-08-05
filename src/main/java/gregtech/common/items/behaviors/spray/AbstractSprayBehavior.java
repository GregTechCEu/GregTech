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
        }

        int returnCode = tryPaintBlock(world, pos, player, hand, facing, sprayCan);
        if (returnCode == -2) {
            return EnumActionResult.PASS;
        } else if (returnCode == -1) {
            onSpray(player, hand, sprayCan);
        }

        world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        return EnumActionResult.SUCCESS;
    }

    /**
     * Return codes:<br/>
     * {@code -2}: didn't paint any block(s)<br/>
     * {@code -1}: colored 1 block</br>
     * {@code 0+}: colored multiple blocks and {@link #onSpray(EntityPlayer, EnumHand, ItemStack)} was handled already
     */
    protected int tryPaintBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull EntityPlayer player,
                                @NotNull EnumHand hand, @NotNull EnumFacing side, @NotNull ItemStack sprayCan) {
        if (player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IPipeTile<?, ?>pipeTile) {
                if (pipeTile.getPaintingColor() == getColorInt(sprayCan)) return -2;
                return traversePipes(world, player, hand, pos, pipeTile, sprayCan);
            }
        }

        ColoredBlockContainer blockContainer = ColoredBlockContainer.getInstance(world, pos, side, player);
        if (blockContainer.isValid() && blockContainer.setColor(getColor(sprayCan))) {
            return -1;
        }

        return -2;
    }

    protected int traversePipes(@NotNull World world, @NotNull EntityPlayer player, @NotNull EnumHand hand,
                                @NotNull BlockPos startPos, @NotNull IPipeTile<?, ?> startingPipe,
                                @NotNull ItemStack sprayCan) {
        EnumDyeColor dyeColor = getColor(sprayCan);
        int color = dyeColor == null ? -1 : dyeColor.colorValue;
        int[] paintedCountHolder = { 0 };
        PipeCollectorWalker.collectPipeNet(world, startPos, startingPipe, pipe -> {
            if (!canSpray(sprayCan)) {
                return false;
            }

            if (pipe.getPaintingColor() != color) {
                pipe.setPaintingColor(color);
                pipe.scheduleRenderUpdate();
                onSpray(player, hand, sprayCan);
                paintedCountHolder[0]++;
            }

            return true;
        });

        return paintedCountHolder[0];
    }
}
