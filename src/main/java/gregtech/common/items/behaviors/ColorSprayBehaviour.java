package gregtech.common.items.behaviors;

import appeng.api.util.AEColor;
import appeng.tile.networking.TileCableBus;
import gregtech.api.GTValues;
import gregtech.api.sound.GTSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import java.util.List;

public class ColorSprayBehaviour extends AbstractUsableBehaviour {

    private final ItemStack empty;
    private final EnumDyeColor color;

    public ColorSprayBehaviour(ItemStack empty, int totalUses, int color) {
        super(totalUses);
        this.empty = empty;
        this.color = EnumDyeColor.values()[color];
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos, facing, stack)) {
            return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));
        }
        if (!tryPaintBlock(player, world, pos, facing)) {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }
        useItemDurability(player, hand, stack, empty.copy());
        world.playSound(null, player.posX, player.posY, player.posZ, GTSounds.SPRAY_CAN_TOOL, SoundCategory.PLAYERS, 100, 0);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private boolean tryPaintBlock(EntityPlayer player, World world, BlockPos pos, EnumFacing side) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
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
        if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
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

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int remainingUses = getUsesLeft(itemStack);
        lines.add(I18n.format("behaviour.paintspray." + this.color.getTranslationKey() + ".tooltip"));
        lines.add(I18n.format("behaviour.paintspray.uses", remainingUses));
    }
}
