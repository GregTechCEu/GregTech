package gregtech.client.renderer.pipe.cache;

import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.quad.QuadHelper;
import gregtech.client.renderer.pipe.quad.RecolorableBakedQuad;
import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ActivableSQC extends StandardSQC {

    protected final EnumMap<EnumFacing, SubListAddress> overlayCoords = new EnumMap<>(EnumFacing.class);
    protected final EnumMap<EnumFacing, SubListAddress> overlayActiveCoords = new EnumMap<>(EnumFacing.class);

    protected final SpriteInformation overlayTex;
    protected final SpriteInformation overlayActiveTex;

    protected ActivableSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                           SpriteInformation overlayTex, SpriteInformation overlayActiveTex) {
        super(helper, endTex, sideTex);
        this.overlayTex = overlayTex;
        this.overlayActiveTex = overlayActiveTex;
        if (helper.getLayerCount() < 2) throw new IllegalStateException(
                "Cannot create an ActivableSQC without 2 or more layers present on the helper!");
    }

    public static @NotNull ActivableSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                               SpriteInformation sideTex, SpriteInformation overlayTex,
                                               SpriteInformation overlayActiveTex) {
        helper.initialize((facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.tubeOverlay(facing, x1, y1, z1, x2, y2, z2,
                OVERLAY_DIST_1));
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
            list.addAll(helper.visitTube(facing, 1));
            overlayCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    protected void buildOverlayActive(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(overlayActiveTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.addAll(helper.visitTube(facing, 1));
            overlayActiveCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    public void addOverlay(List<BakedQuad> list, byte overlayMask, ColorData data, boolean active) {
        List<BakedQuad> quads = cache.getQuads(data);
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
