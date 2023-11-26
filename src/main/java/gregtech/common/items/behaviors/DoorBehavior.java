package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DoorBehavior implements IItemBehaviour {

    private final Block block;

    public DoorBehavior(Block block) {
        this.block = block;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        // Copied from ItemDoor
        ItemStack stack = player.getHeldItem(hand);
        if (facing != EnumFacing.UP) {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }

        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
            pos = pos.offset(facing);
        }

        if (player.canPlayerEdit(pos, facing, stack) && this.block.canPlaceBlockAt(world, pos)) {
            EnumFacing playerFacing = EnumFacing.fromAngle(player.rotationYaw);
            boolean isRightHinge = playerFacing.getXOffset() < 0 && hitZ < 0.5f ||
                    playerFacing.getXOffset() > 0 && hitZ > 0.5f ||
                    playerFacing.getZOffset() < 0 && hitX > 0.5f ||
                    playerFacing.getZOffset() > 0 && hitX < 0.5f;
            ItemDoor.placeDoor(world, pos, playerFacing, this.block, isRightHinge);
            SoundType soundType = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos,
                    player);
            world.playSound(player, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS,
                    (soundType.getVolume() + 1) / 2, soundType.getPitch() * 0.8f);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        } else {
            return ActionResult.newResult(EnumActionResult.FAIL, stack);
        }
    }
}
