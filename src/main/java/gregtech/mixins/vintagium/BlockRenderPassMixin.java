package gregtech.mixins.vintagium;

import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.BloomEffectVintagiumUtil;

import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.util.EnumHelper;

import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.util.BufferSizeUtil;
import me.jellysquid.mods.sodium.client.util.EnumUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderPass.class)
public abstract class BlockRenderPassMixin {

    @Final
    @Mutable
    @Shadow(remap = false)
    public static BlockRenderPass[] VALUES;

    @Final
    @Mutable
    @Shadow(remap = false)
    public static int COUNT;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "<clinit>",
            at = @At(value = "TAIL"),
            remap = false)
    private static void init(CallbackInfo ci) {
        EnumUtil.LAYERS = BlockRenderLayer.values();
        BufferSizeUtil.BUFFER_SIZES.put(BloomEffectUtil.getBloomLayer(), 131072);

        var params = new Class[] { BlockRenderLayer.class, boolean.class };
        var values = new Object[] { BloomEffectUtil.getBloomLayer(), true };
        BloomEffectVintagiumUtil.bloom = EnumHelper.addEnum(BlockRenderPass.class, "BLOOM", params, values);
        VALUES = BlockRenderPass.values();
        COUNT = VALUES.length;
    }
}
