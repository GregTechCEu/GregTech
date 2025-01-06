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
public class ExtraBlockableSQC extends BlockableSQC {

    protected final EnumMap<EnumFacing, SubListAddress> blockedEndCoords = new EnumMap<>(EnumFacing.class);

    private final SpriteInformation blockedEndTex;

    protected ExtraBlockableSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                                SpriteInformation blockedSideTex, SpriteInformation blockedEndTex) {
        super(helper, endTex, sideTex, blockedSideTex);
        this.blockedEndTex = blockedEndTex;
        if (helper.getLayerCount() < 3) throw new IllegalStateException(
                "Cannot create an ExtraBlockableSQC without 3 or more layers present on the helper!");
    }

    public static @NotNull ExtraBlockableSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                                    SpriteInformation sideTex, SpriteInformation blockedSideTex,
                                                    SpriteInformation blockedEndTex) {
        helper.initialize(
                (facing, x1, y1, z1, x2, y2, z2) -> minLengthTube(facing, x1, y1, z1, x2, y2, z2,
                        OVERLAY_DIST_1, 4),
                (facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.capOverlay(facing, x1, y1, z1, x2, y2, z2,
                        OVERLAY_DIST_1));
        ExtraBlockableSQC sqc = new ExtraBlockableSQC(helper, endTex, sideTex, blockedSideTex, blockedEndTex);
        sqc.buildPrototype();
        return sqc;
    }

    @Override
    protected List<RecolorableBakedQuad> buildPrototypeInternal() {
        List<RecolorableBakedQuad> quads = super.buildPrototypeInternal();
        buildExtraBlocked(quads);
        return quads;
    }

    protected void buildExtraBlocked(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(blockedEndTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.add(helper.visitCapper(facing, 2));
            blockedEndCoords.put(facing, new SubListAddress(start, list.size()));
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
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }
}
