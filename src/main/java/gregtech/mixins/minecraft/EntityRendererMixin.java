package gregtech.mixins.minecraft;

import gregtech.client.utils.BloomEffectUtil;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @WrapOperation(method = "renderWorldPass",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/RenderGlobal;renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DILnet/minecraft/entity/Entity;)I",
                            ordinal = 3))
    public int renderBloomBlockLayer(RenderGlobal instance, BlockRenderLayer layer, double partialTicks, int pass,
                                     Entity entity, Operation<Integer> original) {
        return BloomEffectUtil.renderBloomBlockLayer(instance, layer, partialTicks, pass, entity);
    }
}
