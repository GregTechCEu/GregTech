package gregtech.common.items.behaviors;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.toolitem.IToolStats;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrowbarBehaviour implements IItemBehaviour {

    private final int cost;

    public CrowbarBehaviour(int cost) {
        this.cost = cost;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos blockPos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        IBlockState blockState = world.getBlockState(blockPos);
        if (GTUtility.doDamageItem(stack, cost, true)) {
            if (blockState.getBlock() instanceof BlockRailBase) {
                if (world.isRemote) {
                    //always return success on client side
                    return EnumActionResult.SUCCESS;
                } else if (player.isSneaking()) {
                    if (tryBreakRailBlock(blockState, world, blockPos, player)) {
                        GTUtility.doDamageItem(stack, cost, false);
                        return EnumActionResult.SUCCESS;
                    }
                    return EnumActionResult.FAIL;
                } else {
                    if (tryRotateRailBlock(blockState, world, blockPos)) {
                        GTUtility.doDamageItem(stack, cost, false);
                        IToolStats.onOtherUse(stack, world, blockPos);
                        return EnumActionResult.SUCCESS;
                    }
                    return EnumActionResult.FAIL;
                }
            }
            TileEntity tileEntity = world.getTileEntity(blockPos);
            ICoverable coverable = tileEntity == null ? null : tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
            EnumFacing coverSide = coverable == null ? null : ICoverable.rayTraceCoverableSide(coverable, player);
            if (coverSide != null && coverable.getCoverAtSide(coverSide) != null) {
                if (world.isRemote) {
                    //always return success on client side
                    return EnumActionResult.SUCCESS;
                }
                boolean result = coverable.removeCover(coverSide);
                GTUtility.doDamageItem(stack, cost, false);
                IToolStats.onOtherUse(stack, world, blockPos);
                return result ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
            }
            if (tileEntity instanceof TileEntityPipeBase && ((IPipeTile<?, ?>) tileEntity).getFrameMaterial() != null) {
                Material frameMaterial = ((TileEntityPipeBase<?, ?>) tileEntity).getFrameMaterial();
                ((TileEntityPipeBase<?, ?>) tileEntity).setFrameMaterial(null);
                BlockFrame blockFrame = MetaBlocks.FRAMES.get(frameMaterial);
                if (blockFrame != null) {
                    Block.spawnAsEntity(world, blockPos, blockFrame.getItem(frameMaterial));
                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.FAIL;
            }
        }
        return EnumActionResult.PASS;
    }

    private boolean tryBreakRailBlock(IBlockState blockState, World world, BlockPos blockPos, EntityPlayer player) {
        if (world.canMineBlockBody(player, blockPos) && blockState.getBlock().canHarvestBlock(world, blockPos, player)) {
            NonNullList<ItemStack> drops = NonNullList.create();
            blockState.getBlock().getDrops(drops, world, blockPos, blockState, 0);
            for (ItemStack drop : drops) {
                Block.spawnAsEntity(world, blockPos, drop);
            }
            blockState.getBlock().onPlayerDestroy(world, blockPos, blockState);
            blockState.getBlock().onBlockHarvested(world, blockPos, blockState, player);
            blockState.getBlock().breakBlock(world, blockPos, blockState);
            world.setBlockToAir(blockPos);
            return true;
        }
        return false;
    }

    private boolean tryRotateRailBlock(IBlockState blockState, World world, BlockPos blockPos) {
        BlockRailBase blockRailBase = (BlockRailBase) blockState.getBlock();
        int rotation = blockState.getValue(blockRailBase.getShapeProperty()).ordinal() + 1;
        if (rotation >= BlockRailBase.EnumRailDirection.values().length) {
            rotation = 0;
        }
        if (rotation >= 6 && !blockRailBase.isFlexibleRail(world, blockPos)) {
            rotation = 0;
        }
        return world.setBlockState(blockPos, blockState.withProperty(blockRailBase.getShapeProperty(),
                BlockRailBase.EnumRailDirection.values()[rotation]));
    }
}
