package gregtech.common.tools;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ToolBranchCutter extends ToolBase {

    @Override
    public float getBaseDamage(ItemStack stack) {
        return 2.5F;
    }

    @Override
    public float getDigSpeedMultiplier(ItemStack stack) {
        return 0.25F;
    }

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 0.25F;
    }

    @Override
    public float getSaplingModifier(ItemStack stack, World world, EntityPlayer player, BlockPos pos) {
        return 1.0f;
    }

    @Override
    public boolean canMineBlock(IBlockState block, ItemStack stack) {
        return block.getMaterial() == Material.LEAVES;
    }

    @Override
    public boolean canPlayBreakingSound(ItemStack stack, IBlockState state) {
        return canMineBlock(state, stack);
    }
}
