package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.List;

public class HoeBehaviour implements IItemBehaviour {

    private final int cost;

    public HoeBehaviour(int costs) {
        this.cost = costs;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.canPlayerEdit(blockPos, facing, stack) && !world.isAirBlock(blockPos)) {
            IBlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock() == Blocks.GRASS || blockState.getBlock() == Blocks.DIRT) {
                if (blockState.getBlock() == Blocks.GRASS && player.isSneaking()) {
                    if (GTUtility.doDamageItem(stack, this.cost, false)) {
                        if (world.rand.nextInt(3) == 0) {
                            ItemStack grassSeed = ForgeHooks.getGrassSeed(world.rand, 0);
                            Block.spawnAsEntity(world, blockPos.up(), grassSeed);
                        }
                        world.playSound(null, blockPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        world.setBlockState(blockPos, Blocks.DIRT.getDefaultState()
                                .withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT));
                        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
                    }
                } else if (blockState.getBlock() == Blocks.GRASS
                        || blockState.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT
                        || blockState.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.COARSE_DIRT) {
                    if (GTUtility.doDamageItem(stack, this.cost, false)) {
                        world.playSound(null, blockPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        world.setBlockState(blockPos, Blocks.FARMLAND.getDefaultState());
                        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
                    }
                }
            }
        }
        return ActionResult.newResult(EnumActionResult.FAIL, stack);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behaviour.hoe"));
    }
}
