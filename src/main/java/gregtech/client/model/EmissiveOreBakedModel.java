package gregtech.client.model;

import gregtech.api.unification.ore.StoneType;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmissiveOreBakedModel extends OreBakedModel {

    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] overlayQuads = new List[7];

    public EmissiveOreBakedModel(StoneType stoneType, IBakedModel overlay) {
        super(stoneType, overlay);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        if (state == null || layer == null || !ConfigHolder.client.shader.useShader) {
            List<BakedQuad> quads = new ArrayList<>(getBaseModel().getQuads(null, side, 0));
            quads.addAll(getOverlayQuads(side, rand));
            return quads;
        } else if (layer == BlockRenderLayer.CUTOUT_MIPPED) {
            return getBaseModel().getQuads(null, side, 0);
        } else if (layer == BloomEffectUtil.getEffectiveBloomLayer()) {
            return getOverlayQuads(side, rand);
        } else {
            return Collections.emptyList();
        }
    }

    protected List<BakedQuad> getOverlayQuads(@Nullable EnumFacing side, long rand) {
        int index = side == null ? 6 : side.getIndex();
        if (this.overlayQuads[index] == null) {
            List<BakedQuad> quads = new ArrayList<>(this.overlay.getQuads(null, side, rand));
            for (int i = 0; i < quads.size(); i++) {
                quads.set(i, RenderUtil.makeEmissive(quads.get(i)));
            }
            return this.overlayQuads[index] = quads;
        }
        return this.overlayQuads[index];
    }
}
