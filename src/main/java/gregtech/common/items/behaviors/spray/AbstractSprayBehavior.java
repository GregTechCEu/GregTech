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

import appeng.api.util.AEColor;
import appeng.tile.networking.TileCableBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSprayBehavior implements IItemBehaviour {

    /**
     * Get the color of the spray can. {@code null} = solvent
     */
    public abstract @Nullable EnumDyeColor getColor();

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
        ItemStack stack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos, facing, stack)) {
            return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));
        }

        if (!tryPaintBlock(player, world, pos, facing)) {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }

        world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    public EnumActionResult useFromToolbelt(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                            @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                            @NotNull ItemStack sprayCan) {
        if (!player.canPlayerEdit(pos, facing, sprayCan)) {
            return EnumActionResult.FAIL;
        }

        if (!tryPaintBlock(player, world, pos, facing)) {
            return EnumActionResult.PASS;
        }

        world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        return EnumActionResult.SUCCESS;
    }

    public static void handleAutomaticSpray(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos) {
        ItemStack offHand = player.getHeldItem(EnumHand.OFF_HAND);
        AbstractSprayBehavior sprayBehavior = getSprayCanBehavior(offHand);
        if (sprayBehavior == null) return;
        sprayBehavior.onItemUse(player, world, pos, EnumHand.OFF_HAND, EnumFacing.UP, 0, 0, 0);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean tryPaintBlock(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                    @NotNull EnumFacing side) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        EnumDyeColor color = getColor();
        if (color == null) {
            return tryStripBlockColor(player, world, pos, block, side);
        }

        return block.recolorBlock(world, pos, side, color) || tryPaintSpecialBlock(player, world, pos, block, color);
    }

    private boolean tryPaintSpecialBlock(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                         @NotNull Block block, @NotNull EnumDyeColor color) {
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
            if (te instanceof TileCableBus cable) {
                // do not try to recolor if it already is this color
                if (cable.getColor().ordinal() != color.ordinal()) {
                    cable.recolourBlock(null, AEColor.values()[color.ordinal()], player);
                    return true;
                }
            }
        }

        return false;
    }

    protected static boolean tryStripBlockColor(EntityPlayer player, World world, BlockPos pos, Block block,
                                                EnumFacing side) {
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
        if (te instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
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
            if (te instanceof TileCableBus cable) {
                // do not try to strip color if it is already colorless
                if (cable.getColor() != AEColor.TRANSPARENT) {
                    cable.recolourBlock(null, AEColor.TRANSPARENT, player);
                    return true;
                } else return false;
            }
        }

        // General case
        IBlockState state = world.getBlockState(pos);
        for (IProperty<?> prop : state.getProperties().keySet()) {
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
