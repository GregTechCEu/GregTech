package gregtech.api.util;

import net.minecraft.util.BlockRenderLayer;

import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;

public interface VintagiumMapperAccessor {

    void gregTech$addMapping(BlockRenderLayer layer, BlockRenderPass type);
}
