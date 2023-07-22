package gregtech.mixins.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

//TODO, this one needs testing
@Mixin(RenderChunk.class)
public class RenderChunkMixin {

    @ModifyExpressionValue(method = "rebuildChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;"))
    public <T extends TileEntity> TileEntitySpecialRenderer<T> adjustMTERenderer(TileEntitySpecialRenderer<T> originalRenderer, @Nullable TileEntity tileEntityIn) {
        // TODO, adjust when implementing second part of IGregTileEntity
        if (tileEntityIn instanceof MetaTileEntityHolder && !((MetaTileEntityHolder) tileEntityIn).hasTESR()) {
            return null;
        }
        return originalRenderer;
    }
}
