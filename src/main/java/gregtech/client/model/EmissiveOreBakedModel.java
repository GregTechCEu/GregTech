package gregtech.client.model;

import gregtech.api.unification.ore.StoneType;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.client.FMLClientHandler;

import javax.annotation.Nullable;
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
        } else if (layer == BloomEffectUtil.getRealBloomLayer()) {
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
                quads.set(i, transform(quads.get(i)));
            }
            return this.overlayQuads[index] = quads;
        }
        return this.overlayQuads[index];
    }

    private static BakedQuad transform(BakedQuad quad) {
        if (FMLClientHandler.instance().hasOptifine()) return quad;
        VertexFormat format = quad.getFormat();
        if (!format.getElements().contains(DefaultVertexFormats.TEX_2S)) {
            format = new VertexFormat(quad.getFormat());
            format.addElement(DefaultVertexFormats.TEX_2S);
        }
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format) {
            @Override
            public void put(int element, float... data) {
                if (this.getVertexFormat().getElement(element) == DefaultVertexFormats.TEX_2S)
                    super.put(element, 480.0f / 0xFFFF, 480.0f / 0xFFFF);
                else super.put(element, data);
            }
        };
        quad.pipe(builder);
        builder.setApplyDiffuseLighting(false);
        return builder.build();
    }
}
