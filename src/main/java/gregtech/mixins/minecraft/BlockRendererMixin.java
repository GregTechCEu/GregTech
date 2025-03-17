package gregtech.mixins.minecraft;

import gregtech.api.block.IBlockRenderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockRendererDispatcher.class)
public class BlockRendererMixin {

    @Redirect(method = "renderBlock",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/BlockModelRenderer;renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;Z)Z"))
    private boolean customBlockRenderer(BlockModelRenderer instance, IBlockAccess blockAccess, IBakedModel model,
                                        IBlockState state, BlockPos pos, BufferBuilder buffer, boolean checkSides) {
        if (state.getBlock() instanceof IBlockRenderer blockRenderer) {
            return blockRenderer.renderBlock(instance, blockAccess, model, state, pos, buffer, checkSides);
        }
        return instance.renderModel(blockAccess, model, state, pos, buffer, checkSides);
    }
}
