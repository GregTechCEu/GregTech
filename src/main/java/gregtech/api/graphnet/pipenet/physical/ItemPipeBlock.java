package gregtech.api.graphnet.pipenet.physical;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public class ItemPipeBlock extends ItemBlock {

    public ItemPipeBlock(PipeBlock block) {
        super(block);
    }

    @Override
    public @NotNull PipeBlock getBlock() {
        return (PipeBlock) super.getBlock();
    }

    @Override
    public boolean placeBlockAt(@NotNull ItemStack stack, @NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing side,
                                float hitX, float hitY, float hitZ, @NotNull IBlockState newState) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            PipeTileEntity tile = getBlock().getTileEntity(world, pos);
            // TODO set pipe color based on offhand here
            if (tile != null) {
                getBlock().doPlacementLogic(tile, side.getOpposite());
            }
            return true;
        } else return false;
    }
}
