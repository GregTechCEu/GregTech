package gregtech.mixins.vintagium;

import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.BloomEffectVintagiumUtil;

import net.minecraft.util.BlockRenderLayer;

import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRenderPassManager.class)
public abstract class BlockRenderManagerMixin {

    @Shadow(remap = false)
    protected abstract void addMapping(BlockRenderLayer layer, BlockRenderPass type);

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "createDefaultMappings",
            at = @At(value = "RETURN"),
            remap = false)
    private static void gregtech$addMapping(CallbackInfoReturnable<BlockRenderPassManager> cir,
                                            @Local BlockRenderPassManager mapper) {
        ((BlockRenderManagerMixin) (Object) mapper).addMapping(BloomEffectUtil.getBloomLayer(),
                BloomEffectVintagiumUtil.getBloomPass());
    }
}
