package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FertilizerBehavior implements IItemBehaviour {

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, heldItem)) {
            return ActionResult.newResult(EnumActionResult.FAIL, heldItem);
        } else if (ItemDye.applyBonemeal(heldItem, world, pos, player, hand)) {
            if (!world.isRemote) {
                world.playEvent(2005, pos, 0); // bonemeal particles
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
        }
        return ActionResult.newResult(EnumActionResult.PASS, heldItem);
    }
}
