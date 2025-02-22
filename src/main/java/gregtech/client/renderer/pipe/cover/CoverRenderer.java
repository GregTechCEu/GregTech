package gregtech.client.renderer.pipe.cover;

import gregtech.client.renderer.pipe.quad.ColorData;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import java.util.EnumSet;
import java.util.List;

@FunctionalInterface
public interface CoverRenderer {

    void addQuads(List<BakedQuad> quads, EnumFacing facing, EnumSet<EnumFacing> renderPlate, boolean renderBackside,
                  BlockRenderLayer renderLayer, ColorData data);
}
