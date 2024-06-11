package gregtech.api.util;

import net.minecraft.util.BlockRenderLayer;

import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;

public interface VintugiumMapperAccessor {

    void gregTech$addMapping(BlockRenderLayer layer, BlockRenderPass type);
}
