package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShovelBehavior implements IItemBehaviour {

    private final int cost;

    public ShovelBehavior(int cost) {
        this.cost = cost;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
            return ActionResult.newResult(EnumActionResult.FAIL, itemstack);
        } else {
            IBlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();
            if (facing != EnumFacing.DOWN && world.getBlockState(pos.up()).getMaterial() == Material.AIR && block == Blocks.GRASS) {
                IBlockState state = Blocks.GRASS_PATH.getDefaultState();
                world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!world.isRemote) {
                    world.setBlockState(pos, state, 11);
                    itemstack.damageItem(cost, player);
                }
                return ActionResult.newResult(EnumActionResult.SUCCESS, itemstack);
            } else {
                return ActionResult.newResult(EnumActionResult.PASS, itemstack);
            }
        }
    }
}
