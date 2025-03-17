package gregtech.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public interface IBlockRenderer {

    default boolean renderBlock(BlockModelRenderer renderer, IBlockAccess world, IBakedModel bakedModel,
                                IBlockState state, BlockPos pos,
                                BufferBuilder buffer, boolean checkSides) {
        return renderBlock(renderer, world, bakedModel, state, pos, buffer, checkSides,
                MathHelper.getPositionRandom(pos));
    }

    boolean renderBlock(BlockModelRenderer renderer, IBlockAccess world, IBakedModel bakedModel, IBlockState state,
                        BlockPos pos,
                        BufferBuilder buffer, boolean checkSides, long rand);
}
