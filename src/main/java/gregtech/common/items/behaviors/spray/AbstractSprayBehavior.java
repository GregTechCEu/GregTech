package gregtech.common.items.behaviors.spray;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.Mods;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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

import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.util.AEColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class AbstractSprayBehavior implements IItemBehaviour {

    /**
     * Get the color of the spray can. {@code null} = solvent
     */
    public @Nullable EnumDyeColor getColor() {
        return getColor(ItemStack.EMPTY);
    }

    /**
     * Get the color of the spray can. {@code null} = solvent
     */
    public abstract @Nullable EnumDyeColor getColor(@NotNull ItemStack stack);

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
        if (!player.canPlayerEdit(pos, facing, sprayCan)) {
            return EnumActionResult.FAIL;
        } else if (!tryPaintBlock(player, world, pos, facing, getColor(sprayCan))) {
            return EnumActionResult.PASS;
        } else {
            world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                    SoundCategory.PLAYERS, 1.0f, 1.0f);
            return EnumActionResult.SUCCESS;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean tryPaintBlock(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                    @NotNull EnumFacing side, @Nullable EnumDyeColor color) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        if (color == null) {
            return tryStripBlockColor(player, world, pos, block, side);
        }

        return block.recolorBlock(world, pos, side, color) ||
                tryPaintSpecialBlock(player, world, pos, block, side, color);
    }

    private boolean tryPaintSpecialBlock(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                         @NotNull Block block, @NotNull EnumFacing side, @NotNull EnumDyeColor color) {
        if (block == Blocks.GLASS) {
            // noinspection DataFlowIssue
            IBlockState newBlockState = Blocks.STAINED_GLASS.getDefaultState()
                    .withProperty(BlockStainedGlass.COLOR, color);
            world.setBlockState(pos, newBlockState);
            return true;
        } else if (block == Blocks.GLASS_PANE) {
            // noinspection DataFlowIssue
            IBlockState newBlockState = Blocks.STAINED_GLASS_PANE.getDefaultState()
                    .withProperty(BlockStainedGlassPane.COLOR, color);
            world.setBlockState(pos, newBlockState);
            return true;
        } else if (block == Blocks.HARDENED_CLAY) {
            // noinspection DataFlowIssue
            IBlockState newBlockState = Blocks.STAINED_HARDENED_CLAY.getDefaultState()
                    .withProperty(BlockColored.COLOR, color);
            world.setBlockState(pos, newBlockState);
            return true;
        } else if (Mods.AppliedEnergistics2.isModLoaded()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IColorableTile colorableTE) {
                // Do not try to recolor if it already is this color
                if (colorableTE.getColor().ordinal() != color.ordinal()) {
                    colorableTE.recolourBlock(side, AEColor.values()[color.ordinal()], player);
                    return true;
                }
            }
        }

        return false;
    }

    protected static boolean tryStripBlockColor(@NotNull EntityPlayer player, @NotNull World world,
                                                @NotNull BlockPos pos, @NotNull Block block, @NotNull EnumFacing side) {
        // MC special cases
        if (block == Blocks.STAINED_GLASS) {
            world.setBlockState(pos, Blocks.GLASS.getDefaultState());
            return true;
        } else if (block == Blocks.STAINED_GLASS_PANE) {
            world.setBlockState(pos, Blocks.GLASS_PANE.getDefaultState());
            return true;
        } else if (block == Blocks.STAINED_HARDENED_CLAY) {
            world.setBlockState(pos, Blocks.HARDENED_CLAY.getDefaultState());
            return true;
        }

        // MTE special case
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IGregTechTileEntity gtte) {
            MetaTileEntity mte = gtte.getMetaTileEntity();
            if (mte != null) {
                if (mte.isPainted()) {
                    mte.setPaintingColor(-1);
                    return true;
                } else return false;
            }
        }

        // TileEntityPipeBase special case
        if (te instanceof IPipeTile<?, ?>pipe) {
            if (pipe.isPainted()) {
                pipe.setPaintingColor(-1);
                return true;
            } else {
                return false;
            }
        }

        // AE2 cable special case
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            if (te instanceof IColorableTile colorableTE) {
                // Do not try to strip color if it is already colorless
                if (colorableTE.getColor() != AEColor.TRANSPARENT) {
                    colorableTE.recolourBlock(side, AEColor.TRANSPARENT, player);
                    return true;
                } else return false;
            }
        }

        // General case
        IBlockState state = world.getBlockState(pos);
        for (IProperty<?> prop : state.getPropertyKeys()) {
            if (prop.getName().equals("color") && prop.getValueClass() == EnumDyeColor.class) {
                IBlockState defaultState = block.getDefaultState();
                EnumDyeColor defaultColor = EnumDyeColor.WHITE;
                try {
                    // try to read the default color value from the default state instead of just
                    // blindly setting it to default state, and potentially resetting other values
                    defaultColor = (EnumDyeColor) defaultState.getValue(prop);
                } catch (IllegalArgumentException ignored) {
                    // no default color, we may have to fallback to WHITE here
                    // other mods that have custom behavior can be done as
                    // special cases above on a case-by-case basis
                }
                block.recolorBlock(world, pos, side, defaultColor);
                return true;
            }
        }

        return false;
    }
}
