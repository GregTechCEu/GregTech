package gregtech.common.tools;

import gregtech.api.items.toolitem.ToolMetaItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ToolSense extends ToolBase {

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 5.0F;
    }

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 4.0F;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        return block.getMaterial() == Material.PLANTS ||
                block.getMaterial() == Material.LEAVES ||
                block.getMaterial() == Material.VINE ||
                block.getBlock() instanceof BlockCrops;
    }

    @Override
    public boolean onBlockPreBreak(ItemStack stack, BlockPos blockPos, EntityPlayer player) {
        if (player.world.isRemote || player.capabilities.isCreativeMode) {
            return false;
        }
        ToolMetaItem<?> toolMetaItem = (ToolMetaItem<?>) stack.getItem();
        int damagePerBlockBreak = getToolDamagePerBlockBreak(stack);
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                for (int y = -5; y <= 5; y++) {
                    BlockPos offsetPos = blockPos.add(x, y, z);
                    IBlockState blockState = player.world.getBlockState(offsetPos);
                    if (player.world.isBlockModifiable(player, offsetPos) && toolMetaItem.isUsable(stack, damagePerBlockBreak)) {
                        if (blockState.getBlock() instanceof BlockCrops) {

                            player.world.playEvent(2001, offsetPos, Block.getStateId(blockState));
                            ToolUtility.applyHarvestBehavior(offsetPos, player);
                            toolMetaItem.damageItem(stack, player, damagePerBlockBreak, false);

                        } else if (blockState.getMaterial() == Material.PLANTS ||
                                blockState.getMaterial() == Material.LEAVES ||
                                blockState.getMaterial() == Material.VINE) {

                            player.world.destroyBlock(offsetPos, true);
                            toolMetaItem.damageItem(stack, player, damagePerBlockBreak, false);
                        }
                    }

                }
            }
        }
        return true;
    }
}
