package gregtech.mixins.vintigium;

import gregtech.api.util.VintugiumMapperAccessor;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.BloomEffectVintagiumUtil;

import net.minecraft.util.BlockRenderLayer;

import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRenderPassManager.class)
public abstract class BlockRenderManagerMixin implements VintugiumMapperAccessor {

    @Shadow(remap = false)
    protected abstract void addMapping(BlockRenderLayer layer, BlockRenderPass type);

    @Unique
    @Inject(method = "createDefaultMappings",
            at = @At(value = "RETURN",
                     target = "Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPassManager;addMapping(Lnet/minecraft/util/BlockRenderLayer;Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass;)V"),
            remap = false)
    private static void gregtech$addMapping(CallbackInfoReturnable<BlockRenderPassManager> cir,
                                            @Local BlockRenderPassManager mapper) {
        if (mapper instanceof VintugiumMapperAccessor accessor)
            accessor.gregTech$addMapping(BloomEffectUtil.getBloomLayer(), BloomEffectVintagiumUtil.getBloomPass());
    }

    @Override
    public void gregTech$addMapping(BlockRenderLayer layer, BlockRenderPass type) {
        addMapping(layer, type);
    }
}
