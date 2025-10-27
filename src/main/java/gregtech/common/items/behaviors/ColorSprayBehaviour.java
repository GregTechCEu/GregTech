package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.GradientUtil;
import gregtech.api.util.Mods;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
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

import java.awt.*;
import java.util.List;

public class ColorSprayBehaviour extends AbstractUsableBehaviour implements IItemDurabilityManager {

    private final ItemStack empty;
    private final EnumDyeColor color;
    private final long durabilityBarColors;

    public ColorSprayBehaviour(ItemStack empty, int totalUses, int color) {
        super(totalUses);
        this.empty = empty;
        EnumDyeColor[] colors = EnumDyeColor.values();
        this.color = color >= colors.length || color < 0 ? null : colors[color];
        // default to a gray color if this.color is null (like for solvent spray)
        int colorValue = this.color == null ? 0x969696 : this.color.colorValue;
        this.durabilityBarColors = GradientUtil.getGradient(colorValue, 10);
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
        useItemDurability(player, hand, stack, empty.copy());
        world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Nullable
    public static ColorSprayBehaviour getBehavior(ItemStack spraycan) {
        if (!(spraycan.getItem() instanceof MetaItem<?>meta)) return null;
        for (IItemBehaviour behaviour : meta.getBehaviours(spraycan)) {
            if (behaviour instanceof ColorSprayBehaviour spray) return spray;
        }
        return null;
    }

    public EnumActionResult useFromToolbelt(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                            EnumFacing facing, float hitX, float hitY, float hitZ, ItemStack spraycan) {
        if (!player.canPlayerEdit(pos, facing, spraycan)) {
            return EnumActionResult.FAIL;
        }
        if (!tryPaintBlock(player, world, pos, facing)) {
            return EnumActionResult.PASS;
        }
        useItemDurability(player, hand, spraycan, empty.copy());
        world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        return EnumActionResult.SUCCESS;
    }

    private boolean tryPaintBlock(EntityPlayer player, World world, BlockPos pos, EnumFacing side) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (color == null) {
            return tryStripBlockColor(player, world, pos, block, side);
        }
        return block.recolorBlock(world, pos, side, this.color) || tryPaintSpecialBlock(player, world, pos, block);
    }

    private boolean tryPaintSpecialBlock(EntityPlayer player, World world, BlockPos pos, Block block) {
        if (block == Blocks.GLASS) {
            IBlockState newBlockState = Blocks.STAINED_GLASS.getDefaultState()
                    .withProperty(BlockStainedGlass.COLOR, this.color);
            world.setBlockState(pos, newBlockState);
            return true;
        }
        if (block == Blocks.GLASS_PANE) {
            IBlockState newBlockState = Blocks.STAINED_GLASS_PANE.getDefaultState()
                    .withProperty(BlockStainedGlassPane.COLOR, this.color);
            world.setBlockState(pos, newBlockState);
            return true;
        }
        if (block == Blocks.HARDENED_CLAY) {
            IBlockState newBlockState = Blocks.STAINED_HARDENED_CLAY.getDefaultState()
                    .withProperty(BlockColored.COLOR, this.color);
            world.setBlockState(pos, newBlockState);
            return true;
        }
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileCableBus) {
                TileCableBus cable = (TileCableBus) te;
                // do not try to recolor if it already is this color
                if (cable.getColor().ordinal() != color.ordinal()) {
                    cable.recolourBlock(null, AEColor.values()[color.ordinal()], player);
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked, rawtypes")
    private static boolean tryStripBlockColor(EntityPlayer player, World world, BlockPos pos, Block block,
                                              EnumFacing side) {
        // MC special cases
        if (block == Blocks.STAINED_GLASS) {
            world.setBlockState(pos, Blocks.GLASS.getDefaultState());
            return true;
        }
        if (block == Blocks.STAINED_GLASS_PANE) {
            world.setBlockState(pos, Blocks.GLASS_PANE.getDefaultState());
            return true;
        }
        if (block == Blocks.STAINED_HARDENED_CLAY) {
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
        if (te instanceof IPipeTile) {
            IPipeTile<?, ?> pipe = (IPipeTile<?, ?>) te;
            if (pipe.isPainted()) {
                pipe.setPaintingColor(-1);
                return true;
            } else return false;
        }

        // AE2 cable special case
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            if (te instanceof TileCableBus) {
                TileCableBus cable = (TileCableBus) te;
                // do not try to strip color if it is already colorless
                if (cable.getColor() != AEColor.TRANSPARENT) {
                    cable.recolourBlock(null, AEColor.TRANSPARENT, player);
                    return true;
                } else return false;
            }
        }

        // General case
        IBlockState state = world.getBlockState(pos);
        for (IProperty prop : state.getProperties().keySet()) {
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

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int remainingUses = getUsesLeft(itemStack);
        if (color != null) {
            lines.add(I18n.format("behaviour.paintspray." + this.color.getTranslationKey() + ".tooltip"));
        } else {
            lines.add(I18n.format("behaviour.paintspray.solvent.tooltip"));
        }
        lines.add(I18n.format("behaviour.paintspray.uses", remainingUses));
        if (color != null) {
            lines.add(I18n.format("behaviour.paintspray.offhand"));
        }
    }

    @Override
    public double getDurabilityForDisplay(@NotNull ItemStack itemStack) {
        return (double) getUsesLeft(itemStack) / totalUses;
    }

    @Override
    public long getDurabilityColorsForDisplay(@NotNull ItemStack itemStack) {
        return durabilityBarColors;
    }

    @Override
    public boolean doDamagedStateColors(@NotNull ItemStack itemStack) {
        return false;
    }
}
