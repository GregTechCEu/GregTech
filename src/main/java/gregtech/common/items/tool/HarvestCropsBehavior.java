package gregtech.common.items.tool;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class HarvestCropsBehavior implements IToolBehavior {

    public static final HarvestCropsBehavior INSTANCE = new HarvestCropsBehavior();

    protected HarvestCropsBehavior() {/**/}

    @NotNull
    @Override
    public EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
                                      @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }
        ItemStack stack = player.getHeldItem(hand);

        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        Set<BlockPos> blocks;

        if (aoeDefinition == AoESymmetrical.none()) {
            blocks = ImmutableSet.of(pos);
        } else {
            RayTraceResult rayTraceResult = ToolHelper.getPlayerDefaultRaytrace(player);

            if (rayTraceResult == null) return EnumActionResult.PASS;
            if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) return EnumActionResult.PASS;
            if (rayTraceResult.sideHit == null) return EnumActionResult.PASS;

            blocks = ToolHelper.iterateAoE(stack, aoeDefinition, player.world, player, rayTraceResult,
                    HarvestCropsBehavior::isBlockCrops);
            if (isBlockCrops(stack, world, player, rayTraceResult.getBlockPos(), null)) {
                blocks.add(rayTraceResult.getBlockPos());
            }
        }

        boolean harvested = false;
        for (BlockPos blockPos : blocks) {
            if (harvestBlockRoutine(stack, blockPos, player)) {
                harvested = true;
            }
        }

        return harvested ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    private static boolean isBlockCrops(ItemStack stack, World world, EntityPlayer player, BlockPos pos,
                                        @Nullable BlockPos hitBlockPos) {
        if (world.isAirBlock(pos.up())) {
            Block block = world.getBlockState(pos).getBlock();
            return block instanceof BlockCrops;
        }
        return false;
    }

    private static boolean harvestBlockRoutine(ItemStack stack, BlockPos pos, EntityPlayer player) {
        IBlockState blockState = player.world.getBlockState(pos);
        Block block = blockState.getBlock();
        BlockCrops blockCrops = (BlockCrops) block;
        if (blockCrops.isMaxAge(blockState)) {
            NonNullList<ItemStack> drops = NonNullList.create();
            blockCrops.getDrops(drops, player.world, pos, blockState, 0);
            dropListOfItems(player.world, pos, drops);
            player.world.playEvent(2001, pos, Block.getStateId(blockState));
            player.world.setBlockState(pos, blockCrops.withAge(0));
            if (!player.isCreative()) {
                ToolHelper.damageItem(stack, player);
            }
            return true;
        }

        return false;
    }

    private static void dropListOfItems(World world, BlockPos pos, List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            float f = 0.7F;
            double offX = (GTValues.RNG.nextFloat() * f) + (1.0F - f) * 0.5D;
            double offY = (GTValues.RNG.nextFloat() * f) + (1.0F - f) * 0.5D;
            double offZ = (GTValues.RNG.nextFloat() * f) + (1.0F - f) * 0.5D;
            EntityItem entityItem = new EntityItem(world, pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ,
                    stack);
            entityItem.setDefaultPickupDelay();
            world.spawnEntity(entityItem);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.crop_harvesting"));
    }
}
