package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
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
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
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
    public ActionResult<ItemStack> onItemUse(@Nonnull EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // prevent spraying blocks with CF sprayer when in offhand
        if (hand == EnumHand.OFF_HAND && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof MetaItem) {
            for (IItemBehaviour behaviour : ((MetaItem<?>) player.getHeldItemOffhand().getItem()).getItem(player.getHeldItem(EnumHand.MAIN_HAND)).getBehaviours()) {
                if (behaviour instanceof FoamSprayerBehavior)
                    return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
            }
        }

        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos, facing, stack)) {
            return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));
        }
        if (!tryPaintBlock(world, pos, facing)) {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }
        useItemDurability(player, hand, stack, empty.copy());
        world.playSound(null, player.posX, player.posY, player.posZ, GTSounds.SPRAY_CAN_TOOL, SoundCategory.PLAYERS, 100, 0);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private boolean tryPaintBlock(World world, BlockPos pos, EnumFacing side) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        return block.recolorBlock(world, pos, side, this.color) || tryPaintSpecialBlock(world, pos, block);
    }

    private boolean tryPaintSpecialBlock(World world, BlockPos pos, Block block) {
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
        return false;
    }

    public EnumDyeColor getColor() {
        return this.color;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int remainingUses = getUsesLeft(itemStack);
        lines.add(I18n.format("behaviour.paintspray." + this.color.getTranslationKey() + ".tooltip"));
        lines.add(I18n.format("behaviour.paintspray.uses", remainingUses));
    }
}
