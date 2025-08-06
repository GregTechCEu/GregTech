package gregtech.common.items.behaviors.spray;

import gregtech.api.color.ColoredBlockContainer;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.ConfigHolder;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class AbstractSprayBehavior implements IItemBehaviour {

    private static final int MAX_PIPE_TRAVERSAL_LENGTH = 128;

    /**
     * Get the color of the spray can. {@code null} = solvent
     */
    public abstract @Nullable EnumDyeColor getColor(@NotNull ItemStack stack);

    public int getColorInt(@NotNull ItemStack stack) {
        EnumDyeColor color = getColor(stack);
        return color == null ? -1 : color.colorValue;
    }

    public @Range(from = -1, to = 15) int getColorOrdinal(@NotNull ItemStack stack) {
        EnumDyeColor color = getColor(stack);
        return color == null ? -1 : color.ordinal();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canSpray(@NotNull ItemStack stack) {
        return true;
    }

    public void onSpray(@NotNull EntityPlayer player, @NotNull ItemStack sprayCan) {
        //
    }

    public boolean hasSpraySound(@NotNull ItemStack sprayCan) {
        return true;
    }

    public @NotNull SoundEvent getSpraySound(@NotNull ItemStack sprayCan) {
        return GTSoundEvents.SPRAY_CAN_TOOL;
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

    public static boolean isSprayCan(@NotNull ItemStack stack) {
        return getSprayCanBehavior(stack) != null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static @NotNull EnumActionResult handleExternalSpray(@NotNull EntityPlayer player, @NotNull EnumHand hand,
                                                                @NotNull World world, @NotNull BlockPos pos,
                                                                @NotNull EnumFacing facing) {
        return handleExternalSpray(player, world, pos, facing, player.getHeldItem(hand));
    }

    public static @NotNull EnumActionResult handleExternalSpray(@NotNull EntityPlayer player,
                                                                @NotNull World world, @NotNull BlockPos pos,
                                                                @NotNull EnumFacing facing,
                                                                @NotNull ItemStack sprayCan) {
        AbstractSprayBehavior sprayBehavior = getSprayCanBehavior(sprayCan);
        if (sprayBehavior == null) {
            return EnumActionResult.PASS;
        } else {
            return sprayBehavior.spray(player, world, pos, facing, sprayCan);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack sprayCan = player.getHeldItem(hand);
        EnumActionResult result = spray(player, world, pos, facing, sprayCan);
        if (hasSpraySound(sprayCan) && result == EnumActionResult.SUCCESS) {
            world.playSound(null, player.posX, player.posY, player.posZ, getSpraySound(sprayCan), SoundCategory.PLAYERS,
                    1.0f, 1.0f);
        }
        return ActionResult.newResult(result, sprayCan);
    }

    protected @NotNull EnumActionResult spray(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                              @NotNull EnumFacing facing, @NotNull ItemStack sprayCan) {
        if (!canSpray(sprayCan)) {
            return EnumActionResult.PASS;
        } else if (!player.canPlayerEdit(pos, facing, sprayCan)) {
            return EnumActionResult.FAIL;
        }

        if (player.isSneaking()) {
            int color = getColorInt(sprayCan);
            if (world.getBlockState(pos).getBlock() instanceof BlockPipe<?, ?, ?>blockPipe) {
                RayTraceResult hitResult = blockPipe.getServerCollisionRayTrace(player, pos, world);
                if (hitResult != null) {
                    EnumFacing hitSide = CoverRayTracer.determineGridSideHit(hitResult);
                    IPipeTile<?, ?> firstPipe = blockPipe.getPipeTileEntity(world, pos);
                    if (hitSide != null && firstPipe != null && firstPipe.isConnected(hitSide)) {
                        traversePipes(firstPipe, hitSide, player, sprayCan, color);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }

        ColoredBlockContainer colorContainer = ColoredBlockContainer.getInstance(world, pos, facing, player);
        if (colorContainer.isValid() && colorContainer.supportsARGB() ? colorContainer.setColor(getColorInt(sprayCan)) :
                colorContainer.setColor(getColor(sprayCan))) {
            onSpray(player, sprayCan);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    protected void traversePipes(@NotNull IPipeTile<?, ?> pipeTile, @NotNull EnumFacing facing,
                                 @NotNull EntityPlayer player, @NotNull ItemStack sprayCan, int color) {
        if (canPipeBePainted(pipeTile, color) && pipeTile.getNeighbor(facing) instanceof IPipeTile<?, ?>nextPipe) {
            pipeTile.setPaintingColor(color);
            onSpray(player, sprayCan);
            pipeTile = nextPipe;
        } else {
            return;
        }

        for (int count = 1; count < ConfigHolder.tools.maxRecursiveSprayLength && canSpray(sprayCan); count++) {
            if (canPipeBePainted(pipeTile, color)) {
                pipeTile.setPaintingColor(color);
                onSpray(player, sprayCan);
            } else {
                break;
            }

            if (pipeTile.getNumConnections() == 2) {
                int connections = pipeTile.getConnections();
                connections &= ~(1 << facing.getOpposite().getIndex());
                for (EnumFacing other : EnumFacing.VALUES) {
                    if ((connections & (1 << other.getIndex())) != 0) {
                        facing = other;
                        if (pipeTile.getNeighbor(facing) instanceof IPipeTile<?, ?>neighboringPipe) {
                            pipeTile = neighboringPipe;
                        } else {
                            break;
                        }
                    }
                }
            } else {
                break;
            }
        }
    }

    private static boolean canPipeBePainted(@NotNull IPipeTile<?, ?> pipeTile, int color) {
        return pipeTile.isPainted() ? pipeTile.getPaintingColor() != color : color != -1;
    }
}
