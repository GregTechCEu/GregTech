package gregtech.mixins.minecraft;

import gregtech.api.block.IBlockRenderer;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.client.renderer.GTRendererState;
import gregtech.client.renderer.texture.RenderContext;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.MinecraftForgeClient;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderChunk.class)
public class RenderChunkMixin {

    @Shadow
    private ChunkCache worldView;

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
        if (state.getBlock() instanceof IBlockRenderer) {
            // if (world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
            // state = state.getActualState(world, pos);
            // }

            if (state.getRenderType() == EnumBlockRenderType.MODEL) {
                // state = state.getBlock().getExtendedState(state, world, pos);
                // RenderContext context = RenderContext.getContext();
                // context.state = state;
                // context.pos = pos;
                // context.world = world;
                GTRendererState.getCurrentState().setBuffer(bufferBuilder)
                        .render(MinecraftForgeClient.getRenderLayer());
                return true;
                // render custom block
                // return renderer.renderBlockSafe(GTRendererState.getCurrentState()
                // .updateState(context).setBuffer(bufferBuilder).fullBlock(), context);
            }
        }
        return original.call(instance, state, pos, world, bufferBuilder);
    }

    @ModifyExpressionValue(method = "rebuildChunk",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;"))
    public Block inject(Block original, @Local IBlockState state, @Local BlockPos.MutableBlockPos pos) {
        if (original instanceof IBlockRenderer renderer) {
            IBlockAccess world = this.worldView;
            if (world.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                state = state.getActualState(world, pos);
            }

            if (state.getRenderType() == EnumBlockRenderType.MODEL) {
                state = state.getBlock().getExtendedState(state, world, pos);
                RenderContext context = RenderContext.getContext(state, world, pos);
                // render custom block
                renderer.renderBlockSafe(GTRendererState.getCurrentState()
                        .updateState(context).fullBlock(), context);
            }
        }
        return original;
    }
}
