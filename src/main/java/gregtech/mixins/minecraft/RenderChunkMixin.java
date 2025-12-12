package gregtech.mixins.minecraft;

import gregtech.api.metatileentity.GTBaseTileEntity;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

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
        if (tileentity instanceof GTBaseTileEntity && !((GTBaseTileEntity) tileentity).hasTESR()) {
            return null;
        }
        return originalRenderer.call(original, tileentity);
    }
}
