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
public class ExtraCappedSQC extends StandardSQC {

    protected final EnumMap<EnumFacing, SubListAddress> extraCapperCoords = new EnumMap<>(EnumFacing.class);

    protected final SpriteInformation extraEndTex;

    protected ExtraCappedSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                             SpriteInformation extraEndTex) {
        super(helper, endTex, sideTex);
        this.extraEndTex = extraEndTex;
        if (helper.getLayerCount() < 2) throw new IllegalStateException(
                "Cannot create an ExtraCappedSQC without 2 or more layers present on the helper!");
    }

    public static @NotNull ExtraCappedSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                                 SpriteInformation sideTex, SpriteInformation extraEndTex) {
        helper.initialize((facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.capOverlay(facing, x1, y1, z1, x2, y2, z2,
                OVERLAY_DIST_1));
        ExtraCappedSQC cache = new ExtraCappedSQC(helper, endTex, sideTex, extraEndTex);
        cache.buildPrototype();
        return cache;
    }

    @Override
    protected List<RecolorableBakedQuad> buildPrototypeInternal() {
        List<RecolorableBakedQuad> quads = super.buildPrototypeInternal();
        buildExtraCapper(quads);
        return quads;
    }

    protected void buildExtraCapper(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(extraEndTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.add(helper.visitCapper(facing, 1));
            extraCapperCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    @Override
    public void addToList(List<BakedQuad> list, byte connectionMask, byte closedMask, byte blockedMask, ColorData data,
                          byte coverMask) {
        List<BakedQuad> quads = cache.getQuads(data);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (GTUtility.evalMask(facing, connectionMask)) {
                list.addAll(tubeCoords.get(facing).getSublist(quads));
                if (!GTUtility.evalMask(facing, coverMask)) {
                    if (GTUtility.evalMask(facing, closedMask)) {
                        list.addAll(capperClosedCoords.get(facing).getSublist(quads));
                    } else {
                        list.addAll(capperCoords.get(facing).getSublist(quads));
                        list.addAll(extraCapperCoords.get(facing).getSublist(quads));
                    }
                }
            } else {
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }
}
