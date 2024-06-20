package gregtech.mixins.minecraft;

import gregtech.client.utils.BloomEffectUtil;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegionRenderCacheBuilder.class)
public class RegionRenderCacheBuilderMixin {

    @Final
    @Shadow
    private BufferBuilder[] worldRenderers;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initBloom(CallbackInfo ci) {
        worldRenderers[BloomEffectUtil.getBloomLayer().ordinal()] = new BufferBuilder(131072);
    }
}
