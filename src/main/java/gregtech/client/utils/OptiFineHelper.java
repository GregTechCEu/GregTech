package gregtech.client.utils;

import gregtech.api.util.Mods;

import net.minecraft.util.BlockRenderLayer;
import net.optifine.shaders.ShadersRender;

public class OptiFineHelper {

    public static BlockRenderLayer getOFSafeLayer(BlockRenderLayer layer) {
        if (!Mods.ShadersMod.isModLoaded()) return layer;
        return layer == BloomEffectUtil.getBloomLayer() ? BloomEffectUtil.getEffectiveBloomLayer() : layer;
    }

    public static void preRenderChunkLayer(BlockRenderLayer layer) {
        if (Mods.ShadersMod.isModLoaded()) {
            ShadersRender.preRenderChunkLayer(getOFSafeLayer(layer));
        }
    }

    public static void postRenderChunkLayer(BlockRenderLayer layer) {
        if (Mods.ShadersMod.isModLoaded()) {
            ShadersRender.postRenderChunkLayer(getOFSafeLayer(layer));
        }
    }
}
