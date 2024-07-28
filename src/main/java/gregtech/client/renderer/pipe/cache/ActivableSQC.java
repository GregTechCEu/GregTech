package gregtech.client.renderer.pipe.cache;

import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.quad.RecolorableBakedQuad;
import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

public class ActivableSQC extends StructureQuadCache {

    protected final EnumMap<EnumFacing, SubListAddress> overlayCoords = new EnumMap<>(EnumFacing.class);
    protected final EnumMap<EnumFacing, SubListAddress> overlayActiveCoords = new EnumMap<>(EnumFacing.class);

    protected final SpriteInformation overlayTex;
    protected final SpriteInformation overlayActiveTex;

    protected ActivableSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                           SpriteInformation overlayTex, SpriteInformation overlayActiveTex) {
        super(helper, endTex, sideTex);
        this.overlayTex = overlayTex;
        this.overlayActiveTex = overlayActiveTex;
    }

    public static @NotNull ActivableSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                               SpriteInformation sideTex, SpriteInformation overlayTex,
                                               SpriteInformation overlayActiveTex) {
        ActivableSQC cache = new ActivableSQC(helper, endTex, sideTex, overlayTex, overlayActiveTex);
        cache.buildPrototype();
        return cache;
    }

    @Override
    protected List<RecolorableBakedQuad> buildPrototypeInternal() {
        List<RecolorableBakedQuad> quads = super.buildPrototypeInternal();
        buildOverlay(quads);
        buildOverlayActive(quads);
        return quads;
    }

    protected void buildOverlay(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(overlayTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.addAll(helper.visitTube(facing));
            overlayCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    protected void buildOverlayActive(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(overlayActiveTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.addAll(helper.visitTube(facing));
            overlayActiveCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    public void addOverlay(List<BakedQuad> list, byte overlayMask, int argb, boolean active) {
        List<BakedQuad> quads = cache.getQuads(argb);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (GTUtility.evalMask(facing, overlayMask)) {
                if (active) {
                    list.addAll(overlayActiveCoords.get(facing).getSublist(quads));
                } else {
                    list.addAll(overlayCoords.get(facing).getSublist(quads));
                }
            }
        }
    }
}
