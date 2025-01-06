package gregtech.client.renderer.pipe.cache;

import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.quad.QuadHelper;
import gregtech.client.renderer.pipe.quad.RecolorableBakedQuad;
import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

public class ExtraBlockableCoreRestrictiveSQC extends ExtraBlockableSQC {

    protected final EnumMap<EnumFacing, SubListAddress> restrictiveCoords = new EnumMap<>(EnumFacing.class);

    private final SpriteInformation restrictiveTex;

    protected ExtraBlockableCoreRestrictiveSQC(PipeQuadHelper helper, SpriteInformation endTex,
                                               SpriteInformation sideTex,
                                               SpriteInformation blockedSideTex, SpriteInformation blockedEndTex,
                                               SpriteInformation restrictiveTex) {
        super(helper, endTex, sideTex, blockedSideTex, blockedEndTex);
        this.restrictiveTex = restrictiveTex;
        if (helper.getLayerCount() < 4) throw new IllegalStateException(
                "Cannot create an ExtraBlockableSQC without 4 or more layers present on the helper!");
    }

    public static @NotNull ExtraBlockableCoreRestrictiveSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                                                   SpriteInformation sideTex,
                                                                   SpriteInformation blockedSideTex,
                                                                   SpriteInformation blockedEndTex,
                                                                   SpriteInformation restrictiveTex) {
        helper.initialize(
                (facing, x1, y1, z1, x2, y2, z2) -> minLengthTube(facing, x1, y1, z1, x2, y2, z2,
                        OVERLAY_DIST_1, 4),
                (facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.capOverlay(facing, x1, y1, z1, x2, y2, z2,
                        OVERLAY_DIST_1),
                (facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.fullOverlay(facing, x1, y1, z1, x2, y2, z2,
                        OVERLAY_DIST_1));
        ExtraBlockableCoreRestrictiveSQC sqc = new ExtraBlockableCoreRestrictiveSQC(helper, endTex, sideTex,
                blockedSideTex, blockedEndTex, restrictiveTex);
        sqc.buildPrototype();
        return sqc;
    }

    @Override
    protected List<RecolorableBakedQuad> buildPrototypeInternal() {
        List<RecolorableBakedQuad> quads = super.buildPrototypeInternal();
        buildCoreRestrictive(quads);
        return quads;
    }

    protected void buildCoreRestrictive(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(restrictiveTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.add(helper.visitCore(facing, 3));
            restrictiveCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    @Override
    public void addToList(List<BakedQuad> list, byte connectionMask, byte closedMask, byte blockedMask, ColorData data,
                          byte coverMask) {
        List<BakedQuad> quads = cache.getQuads(data);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (GTUtility.evalMask(facing, connectionMask)) {
                boolean blocked = GTUtility.evalMask(facing, blockedMask);
                list.addAll(tubeCoords.get(facing).getSublist(quads));
                if (!GTUtility.evalMask(facing, coverMask)) {
                    if (GTUtility.evalMask(facing, closedMask)) {
                        list.addAll(capperClosedCoords.get(facing).getSublist(quads));
                    } else {
                        list.addAll(capperCoords.get(facing).getSublist(quads));
                    }
                    if (blocked) {
                        list.addAll(blockedEndCoords.get(facing).getSublist(quads));
                    }
                }
                if (blocked) {
                    list.addAll(blockedCoords.get(facing).getSublist(quads));
                }
            } else {
                list.addAll(restrictiveCoords.get(facing).getSublist(quads));
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }
}
