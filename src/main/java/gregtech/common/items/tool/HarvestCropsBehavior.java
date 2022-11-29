package gregtech.common.items.tool;

import com.google.common.collect.ImmutableSet;
import gregtech.api.GTValues;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class HarvestCropsBehavior implements IToolBehavior {

    @Override
    public void onBlockStartBreak(@Nonnull ItemStack stack, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {

        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        Set<BlockPos> blocks;

        if(aoeDefinition == AoESymmetrical.none()) {
            blocks = ImmutableSet.of(pos);
        } else {
            Vec3d lookPos = player.getPositionEyes(1F);
            Vec3d rotation = player.getLook(1);
            Vec3d realLookPos = lookPos.add(rotation.x * 5, rotation.y * 5, rotation.z * 5);
            RayTraceResult rayTraceResult = player.world.rayTraceBlocks(lookPos, realLookPos);

            if (rayTraceResult == null) return;
            if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) return;
            if (rayTraceResult.sideHit == null) return;

            blocks = ToolHelper.iterateAoE(stack, aoeDefinition, player.world, player, rayTraceResult, HarvestCropsBehavior::isBlockCrops);
            blocks.add(rayTraceResult.getBlockPos());

        }

        for (BlockPos blockPos : blocks) {
            harvestBlockRoutine(blockPos, player);
        }
    }

    private static boolean isBlockCrops(ItemStack stack, World world, EntityPlayer player, BlockPos pos, @Nullable BlockPos hitBlockPos) {
        if (world.isAirBlock(pos.up())) {
            Block block = world.getBlockState(pos).getBlock();
            return block instanceof BlockCrops;
        }
        return false;
    }

    private static void harvestBlockRoutine(BlockPos pos, EntityPlayer player) {
        IBlockState blockState = player.world.getBlockState(pos);
        Block block = blockState.getBlock();
        BlockCrops blockCrops = (BlockCrops) block;
        if (blockCrops.isMaxAge(blockState)) {
            NonNullList<ItemStack> drops = NonNullList.create();
            blockCrops.getDrops(drops, player.world, pos, blockState, 0);
            dropListOfItems(player.world, pos, drops);
            player.world.setBlockState(pos, blockCrops.withAge(0));
        }
    }

    private static void dropListOfItems(World world, BlockPos pos, List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            float f = 0.7F;
            double offX = (GTValues.RNG.nextFloat() * f) + (1.0F - f) * 0.5D;
            double offY = (GTValues.RNG.nextFloat() * f) + (1.0F - f) * 0.5D;
            double offZ = (GTValues.RNG.nextFloat() * f) + (1.0F - f) * 0.5D;
            EntityItem entityItem = new EntityItem(world, pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ, stack);
            entityItem.setDefaultPickupDelay();
            world.spawnEntity(entityItem);
        }
    }
}
