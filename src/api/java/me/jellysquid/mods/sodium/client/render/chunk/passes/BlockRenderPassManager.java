package me.jellysquid.mods.sodium.client.render.chunk.passes;

import net.minecraft.util.BlockRenderLayer;

/**
 * Adapted and minimized from <a href="https://github.com/Asek3/sodium-1.12/blob/12.x/forge/src/main/java/me/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPassManager.java">BlockRenderPassManager.java</a>
 */
public class BlockRenderPassManager {

    private void addMapping(BlockRenderLayer layer, BlockRenderPass type) {}

    /**
     * Creates a set of render pass mappings to vanilla render layers which closely mirrors the rendering
     * behavior of vanilla.
     */
    public static BlockRenderPassManager createDefaultMappings() {
        return new BlockRenderPassManager();
    }
}
