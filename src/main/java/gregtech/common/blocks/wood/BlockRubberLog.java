package gregtech.common.blocks.wood;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.common.ConfigHolder;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.items.MetaItems;

import net.minecraft.block.BlockLog;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockRubberLog extends BlockLog {

    public static final PropertyBool NATURAL = PropertyBool.create("natural");
    public static final PropertyBool TAPPABLE = PropertyBool.create("tappable");

    public BlockRubberLog() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(LOG_AXIS, BlockLog.EnumAxis.Y)
                .withProperty(NATURAL, false)
                .withProperty(TAPPABLE, false));
        setTranslationKey("rubber_log");
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH);
        setHarvestLevel(ToolClasses.AXE, 0);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOG_AXIS, NATURAL, TAPPABLE);
    }

    /*
     * Rubber Log metadata format is complicated due to attempting to maintain compatibility with previous logs.
     * Block metadata is represented by 4 bits.
     *
     * Format:
     * - The left-most bit represents whether this log is tree-tappable or not, and will not change.
     *
     * If the left-most bit is set (is tree-tappable):
     * - The middle two bits represent the side the tree-tapping spot is on.
     * - The right-most bit represents if this spot is filled or empty (currently able to be tapped or not).
     *
     * If the left-most bit is not set (not tree-tappable):
     * - The middle two bits represent the axis of the log.
     * - The right-most bit represents if this log was naturally placed by worldgen.
     *
     * In order to properly test if a log was placed by worldgen, you must test both the right-most and the
     * left-most bits. If either are set, then it is a natural log.
     */

    @NotNull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(NATURAL, (meta & 1) == 1)
                .withProperty(LOG_AXIS, EnumAxis.values()[(meta >> 1) & 0b11])
                .withProperty(TAPPABLE, (meta >> 3) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(NATURAL) ? 1 : 0) | (state.getValue(LOG_AXIS).ordinal() << 1) |
                (state.getValue(TAPPABLE) ? 0b1000 : 0);
    }

    /**
     * @return The side a Treetap can be used on, or null if none.
     */
    @Nullable
    public EnumFacing getTreeTapSide(IBlockState state) {
        if (!state.getValue(NATURAL) || !state.getValue(TAPPABLE)) {
            return null;
        }
        EnumAxis treeTapState = state.getValue(LOG_AXIS);
        return switch (treeTapState) {
            case X -> EnumFacing.NORTH;
            case Y -> EnumFacing.EAST;
            case Z -> EnumFacing.SOUTH;
            case NONE -> EnumFacing.WEST;
        };
    }

    /**
     * @return Whether this log has a tapping spot, and if so, if it can be tapped.
     */
    public boolean isTapDepleted(IBlockState state) {
        if (state.getValue(TAPPABLE)) {
            return !state.getValue(NATURAL);
        }
        return false;
    }

    /**
     * @return Whether this is a Log placed by worldgen, or placed by a player.
     */
    public boolean isNaturalLog(IBlockState state) {
        return state.getValue(TAPPABLE) || state.getValue(NATURAL);
    }

    /**
     * Set a Rubber Log block state to have a Treetapping spot.
     * Will set the NATURAL property to true if it was not already set.
     *
     * @param state The state to set the Treetapping property onto.
     * @param side  The side for the Treetapping spot. UP/DOWN will map to WEST.
     *
     * @return The modified state
     */
    public IBlockState setTreeTapSide(IBlockState state, EnumFacing side) {
        return state.withProperty(NATURAL, true)
                .withProperty(TAPPABLE, true)
                .withProperty(LOG_AXIS, switch (side) {
                case NORTH -> EnumAxis.X;
                case EAST -> EnumAxis.Y;
                case SOUTH -> EnumAxis.Z;
                default -> EnumAxis.NONE; // WEST
                });
    }

    @Override
    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                         @NotNull IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;
        if (ConfigHolder.worldgen.requireTreeTapForRubberTrees) {
            // With the requireTreetap config, only drop Sticky Resin with chance from a tappable log
            if (state.getValue(TAPPABLE)) {
                if (rand.nextDouble() <= 0.15D) {
                    drops.add(MetaItems.STICKY_RESIN.getStackForm());
                }
            }
        } else {
            // Without the config, drop Sticky Resin with chance from any natural log
            if (isNaturalLog(state)) {
                if (rand.nextDouble() <= 0.85D) {
                    drops.add(MetaItems.STICKY_RESIN.getStackForm());
                }
            }
        }
        drops.add(new ItemStack(this));
    }

    @Override
    public void randomTick(@NotNull World world, @NotNull BlockPos pos,
                           @NotNull IBlockState state, @NotNull Random random) {
        super.randomTick(world, pos, state, random);
        if (isTapDepleted(state)) {
            if (random.nextInt(7) == 0) {
                world.setBlockState(pos, state.withProperty(NATURAL, true));
            }
        }
    }

    @Override
    public @NotNull EnumPushReaction getPushReaction(@NotNull IBlockState state) {
        if (state.getValue(TAPPABLE)) {
            return EnumPushReaction.BLOCK;
        }
        return EnumPushReaction.NORMAL;
    }

    @Override
    public @NotNull IBlockState withRotation(@NotNull IBlockState state, @NotNull Rotation rot) {
        if (state.getValue(TAPPABLE)) {
            return state;
        }
        return super.withRotation(state, rot);
    }

    @Override
    public boolean rotateBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing axis) {
        IBlockState state = world.getBlockState(pos);
        // state is not passed, so we can't guarantee this property
        // is present, and if it isn't, checking directly will crash.
        for (IProperty<?> prop : state.getProperties().keySet()) {
            if (prop.equals(TAPPABLE)) {
                if (state.getValue(TAPPABLE)) {
                    return false;
                }
                break;
            }
        }
        return super.rotateBlock(world, pos, axis);
    }
}
