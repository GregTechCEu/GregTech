package gregtech.common.items.behaviors;

import gregtech.api.GTValues;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class LighterBehaviour extends AbstractUsableBehaviour {

    public LighterBehaviour(int fuelAmount) {
        super(fuelAmount);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (entity instanceof EntityCreeper) {
            ((EntityCreeper) entity).ignite();
            useItemDurability(player, EnumHand.MAIN_HAND, stack, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        pos = pos.offset(facing);
        ItemStack itemstack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos, facing, itemstack))
            return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));

        if (world.isAirBlock(pos)) {
            world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, GTValues.RNG.nextFloat() * 0.4F + 0.8F);
            world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 11);
        }

        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemstack);
        }

        itemstack.damageItem(1, player);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(LocalizationUtils.format("behaviour.lighter.tooltip"));
        if (totalUses > 1) {
            //do not add total uses amount for single-use items like matches
            int fuelAmount = getUsesLeft(itemStack);
            lines.add(LocalizationUtils.format("behaviour.lighter.uses", fuelAmount));
        }
    }
}
