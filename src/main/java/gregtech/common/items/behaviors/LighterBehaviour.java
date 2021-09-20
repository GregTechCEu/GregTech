package gregtech.common.items.behaviors;

import gregtech.api.GTValues;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
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
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        pos = pos.offset(side);
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos, side, stack)) {
            return EnumActionResult.FAIL;
        } else {
            if (world.isAirBlock(pos)) {
                world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, GTValues.RNG.nextFloat() * 0.4f + 0.8f);
                world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 11);
            }
            useItemDurability(player, hand, stack, ItemStack.EMPTY);
            return EnumActionResult.SUCCESS;
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behaviour.lighter.tooltip"));
        if (totalUses > 1) {
            //do not add total uses amount for single-use items like matches
            int fuelAmount = getUsesLeft(itemStack);
            lines.add(I18n.format("behaviour.lighter.uses", fuelAmount));
        }
    }
}
