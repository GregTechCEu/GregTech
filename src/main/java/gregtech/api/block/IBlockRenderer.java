package gregtech.api.block;

import gregtech.api.util.GTLog;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IBlockRenderer {

    default boolean renderBlockSafe(IBlockState state, IBlockAccess world, BlockPos pos, BufferBuilder buffer) {
        try {
            renderBlock(state, world, pos, buffer);
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
        return true;
    }

    void renderBlock(IBlockState state, IBlockAccess world, BlockPos pos, BufferBuilder buffer);
}
