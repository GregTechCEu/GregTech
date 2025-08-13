package gregtech.common.items.tool;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import gregtech.common.blocks.explosive.BlockGTExplosive;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class FlintAndSteelToolBehavior implements IToolBehavior {

    public static final FlintAndSteelToolBehavior INSTANCE = new FlintAndSteelToolBehavior();

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos,
                                           @NotNull EnumFacing facing, float hitX, float hitY, float hitZ,
                                           @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // 播放打火石使用的声音
        world.playSound(null, player.getPosition(), SoundEvents.ITEM_FLINTANDSTEEL_USE,
                SoundCategory.PLAYERS, 1.0F, GTValues.RNG.nextFloat() * 0.4F + 0.8F);

        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        // 如果是TNT方块，则引爆它
        if (block instanceof BlockTNT) {
            ((BlockTNT) block).explode(world, pos, blockState.withProperty(BlockTNT.EXPLODE, true), player);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            return EnumActionResult.SUCCESS;
        }
        // 如果是GregTech的爆炸方块，则引爆它
        else if (block instanceof BlockGTExplosive) {
            ((BlockGTExplosive) block).explode(world, pos, player);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            return EnumActionResult.SUCCESS;
        } else {
            // 其他情况，在点击面的相邻位置放置火方块
            BlockPos offset = pos.offset(facing);
            if (world.isAirBlock(offset)) {
                world.setBlockState(offset, Blocks.FIRE.getDefaultState(), 11);
                if (!world.isRemote) {
                    // 触发放置方块的成就
                    CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, offset, stack);
                }
            }
            return EnumActionResult.SUCCESS;
        }
    }
}
