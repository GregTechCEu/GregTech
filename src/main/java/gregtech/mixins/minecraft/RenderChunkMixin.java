package gregtech.mixins.minecraft;

import gregtech.api.block.IBlockRenderer;
import gregtech.api.metatileentity.MetaTileEntityHolder;

import gregtech.client.renderer.GTRendererState;

import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderChunk.class)
public class RenderChunkMixin {

    @WrapOperation(method = "rebuildChunk",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;"))
    public <T extends TileEntity> TileEntitySpecialRenderer<T> adjustMTERenderer(TileEntityRendererDispatcher original,
                                                                                 TileEntity tileentity,
                                                                                 Operation<TileEntitySpecialRenderer<T>> originalRenderer) {
        // TODO, adjust when implementing second part of IGregTileEntity
        if (tileentity instanceof MetaTileEntityHolder && !((MetaTileEntityHolder) tileentity).hasTESR()) {
            return null;
        }
        return originalRenderer.call(original, tileentity);
    }

    @WrapOperation(method = "rebuildChunk",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/BlockRendererDispatcher;renderBlock(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z"))
    public boolean wrapBlockRenderer(BlockRendererDispatcher instance, IBlockState state, BlockPos pos,
                                     IBlockAccess world, BufferBuilder bufferBuilder, Operation<Boolean> original) {
        if (state.getBlock() instanceof IBlockRenderer renderer) {
            // render custom block
            return renderer.renderBlockSafe(GTRendererState.getCurrentState()
                    .setBuffer(bufferBuilder)
                    .updateState(state, world, pos)
                    .setBounds(1, 1, 1));
        } else {
            return original.call(instance, state, pos, world, bufferBuilder);
        }
    }
}
