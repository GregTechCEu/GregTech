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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    /**
     * Get the color of the spray can. <br/>
     * {@code null} = solvent
     */
    public abstract @Nullable EnumDyeColor getColor(@NotNull ItemStack sprayCan);

    public int getColorInt(@NotNull ItemStack sprayCan) {
        EnumDyeColor color = getColor(sprayCan);
        return color == null ? -1 : color.colorValue;
    }

    public @Range(from = -1, to = 15) int getColorOrdinal(@NotNull ItemStack sprayCan) {
        EnumDyeColor color = getColor(sprayCan);
        return color == null ? -1 : color.ordinal();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canSpray(@NotNull ItemStack sprayCan) {
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

    public int getMaximumSprayLength(@NotNull ItemStack sprayCan) {
        return ConfigHolder.tools.maxRecursiveSprayLength;
    }

    public static @Nullable AbstractSprayBehavior getSprayCanBehavior(@NotNull ItemStack sprayCan) {
        if (!(sprayCan.getItem() instanceof MetaItem<?>metaItem)) return null;

        for (IItemBehaviour behaviour : metaItem.getBehaviours(sprayCan)) {
            if (behaviour instanceof AbstractSprayBehavior sprayBehavior) {
                return sprayBehavior;
            }
        }

        return null;
    }

    public static boolean isSprayCan(@NotNull ItemStack stack) {
        return getSprayCanBehavior(stack) != null;
    }

    /**
     * Call from your items
     * {@link Item#onItemUseFirst(EntityPlayer, World, BlockPos, EnumFacing, float, float, float, EnumHand)}
     * or the meta item equivalent to check if block is sprayable early enough in the click handling chain.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static @NotNull EnumActionResult handleExternalSpray(@NotNull EntityPlayer player, @NotNull EnumHand hand,
                                                                @NotNull World world, @NotNull BlockPos pos,
                                                                @NotNull EnumFacing facing) {
        return handleExternalSpray(player, world, pos, facing, player.getHeldItem(hand));
    }

    /**
     * Call from your items
     * {@link Item#onItemUseFirst(EntityPlayer, World, BlockPos, EnumFacing, float, float, float, EnumHand)}
     * or the meta item equivalent to check if block is sprayable early enough in the click handling chain.
     */
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
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
                                           float hitY, float hitZ, EnumHand hand) {
        ItemStack sprayCan = player.getHeldItem(hand);
        EnumActionResult result = spray(player, world, pos, side, sprayCan);
        if (hasSpraySound(sprayCan) && result == EnumActionResult.SUCCESS) {
            world.playSound(null, player.posX, player.posY, player.posZ, getSpraySound(sprayCan), SoundCategory.PLAYERS,
                    1.0f, 1.0f);
        }
        return result;
    }

    protected @NotNull EnumActionResult spray(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                              @NotNull EnumFacing facing, @NotNull ItemStack sprayCan) {
        if (!canSpray(sprayCan)) {
            return EnumActionResult.PASS;
        } else if (!player.canPlayerEdit(pos, facing, sprayCan)) {
            return EnumActionResult.FAIL;
        }

        if (player.isSneaking()) {
            if (world.getBlockState(pos).getBlock() instanceof BlockPipe<?, ?, ?>blockPipe) {
                RayTraceResult hitResult = blockPipe.getServerCollisionRayTrace(player, pos, world);
                if (hitResult != null) {
                    EnumFacing hitSide = CoverRayTracer.determineGridSideHit(hitResult);
                    IPipeTile<?, ?> firstPipe = blockPipe.getPipeTileEntity(world, pos);
                    int color = getColorInt(sprayCan);
                    if (hitSide != null && firstPipe != null && firstPipe.isConnected(hitSide) &&
                            (firstPipe.isPainted() ? firstPipe.getPaintingColor() != color : color != -1)) {
                        if (world.isRemote) return EnumActionResult.SUCCESS;
                        traversePipes(firstPipe, hitSide, player, sprayCan, color);
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }

        ColoredBlockContainer colorContainer = ColoredBlockContainer.getContainer(world, pos, facing, player);
        //TODO: reimplement spraying according to the mode of the spray can
    }

    public abstract @NotNull AbstractSprayBehavior.ColorMode getColorMode(@NotNull ItemStack sprayCan);

    protected void traversePipes(@NotNull IPipeTile<?, ?> pipeTile, @NotNull EnumFacing facing,
                                 @NotNull EntityPlayer player, @NotNull ItemStack sprayCan, int color) {
        if (canPipeBePainted(pipeTile, color) && pipeTile.getNeighbor(facing) instanceof IPipeTile<?, ?>nextPipe) {
            pipeTile.setPaintingColor(color);
            onSpray(player, sprayCan);
            pipeTile = nextPipe;
        } else {
            return;
        }

        for (int count = 1; count < getMaximumSprayLength(sprayCan) && canSpray(sprayCan); count++) {
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

    public enum ColorMode {
        DYE_ONLY,
        ARGB_ONLY,
        EITHER
    }
}
