package gregtech.mixins.vintigium;

import gregtech.api.util.GTEnumHelper;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.BloomEffectVintagiumUtil;

import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.util.EnumHelper;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.util.BufferSizeUtil;
import me.jellysquid.mods.sodium.client.util.EnumUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockRenderPass.class)
public abstract class BlockRenderPassMixin {

    @ModifyExpressionValue(method = "<clinit>",
                           at = @At(value = "INVOKE",
                                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass;values()[Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass;"),
                           remap = false)
    private static BlockRenderPass[] addEnum(BlockRenderPass[] original) {
        EnumUtil.LAYERS = BlockRenderLayer.values();
        BufferSizeUtil.BUFFER_SIZES.put(BloomEffectUtil.getBloomLayer(), 131072);

        var params = new Class[] { BlockRenderLayer.class, boolean.class };
        var values = new Object[] { BloomEffectUtil.getBloomLayer(), true };
        var field = GTEnumHelper.getValuesField(BlockRenderPass.class);
        try {
            BloomEffectVintagiumUtil.bloom = GTEnumHelper.makeEnum(BlockRenderPass.class, "BLOOM",
                    original.length, params, values);
            var array = ArrayUtils.add(original, BloomEffectVintagiumUtil.getBloomPass());
            EnumHelper.setFailsafeFieldValue(field, null, array);
            GTEnumHelper.cleanEnumCache(BlockRenderPass.class);
            return array;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
