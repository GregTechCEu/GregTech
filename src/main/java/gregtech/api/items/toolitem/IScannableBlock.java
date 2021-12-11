package gregtech.api.items.toolitem;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;

import java.util.List;

public interface IScannableBlock {

    /**
     * Called when block is analyzed with a magnifying glass
     * Return complete description of the block here
     */
    List<ITextComponent> getMagnifyResults(IBlockAccess world, BlockPos pos, IBlockState blockState, EntityPlayer player);


    default int getScanDuration(IBlockAccess world, BlockPos pos, IBlockState blockState, EntityPlayer player) {
        return 59;
    }
}
