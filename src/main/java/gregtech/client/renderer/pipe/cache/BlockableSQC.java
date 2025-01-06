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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.EnumMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BlockableSQC extends StandardSQC {

    protected final EnumMap<EnumFacing, SubListAddress> blockedCoords = new EnumMap<>(EnumFacing.class);

    protected final SpriteInformation blockedTex;

    protected BlockableSQC(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex,
                           SpriteInformation blockedTex) {
        super(helper, endTex, sideTex);
        this.blockedTex = blockedTex;
        if (helper.getLayerCount() < 2) throw new IllegalStateException(
                "Cannot create a BlockableSQC without 2 or more layers present on the helper!");
    }

    public static @NotNull BlockableSQC create(PipeQuadHelper helper, SpriteInformation endTex,
                                               SpriteInformation sideTex, SpriteInformation blockedTex) {
        helper.initialize((facing, x1, y1, z1, x2, y2, z2) -> minLengthTube(facing, x1, y1, z1, x2, y2, z2,
                OVERLAY_DIST_1, 4));
        BlockableSQC cache = new BlockableSQC(helper, endTex, sideTex, blockedTex);
        cache.buildPrototype();
        return cache;
    }

    public static ImmutablePair<Vector3f, Vector3f> minLengthTube(@Nullable EnumFacing facing, float x1, float y1,
                                                                  float z1, float x2,
                                                                  float y2, float z2, float g, float minLength) {
        if (facing == null) return QuadHelper.tubeOverlay(facing, x1, y1, z1, x2, y2, z2, g);
        return switch (facing) {
            case UP -> QuadHelper.tubeOverlay(facing, x1, Math.min(y1, y2 - minLength), z1, x2, y2, z2, g);
            case DOWN -> QuadHelper.tubeOverlay(facing, x1, y1, z1, x2, Math.max(y2, y1 + minLength), z2, g);
            case EAST -> QuadHelper.tubeOverlay(facing, Math.min(x1, x2 - minLength), y1, z1, x2, y2, z2, g);
            case WEST -> QuadHelper.tubeOverlay(facing, x1, y1, z1, Math.max(x2, x1 + minLength), y2, z2, g);
            case SOUTH -> QuadHelper.tubeOverlay(facing, x1, y1, Math.min(z1, z2 - minLength), x2, y2, z2, g);
            case NORTH -> QuadHelper.tubeOverlay(facing, x1, y1, z1, x2, y2, Math.max(z2, z1 + minLength), g);
        };
    }

    @Override
    protected List<RecolorableBakedQuad> buildPrototypeInternal() {
        List<RecolorableBakedQuad> quads = super.buildPrototypeInternal();
        buildBlocked(quads);
        return quads;
    }

    protected void buildBlocked(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(blockedTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.addAll(helper.visitTube(facing, 1));
            blockedCoords.put(facing, new SubListAddress(start, list.size()));
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
                    }
                }
                if (GTUtility.evalMask(facing, blockedMask)) {
                    list.addAll(blockedCoords.get(facing).getSublist(quads));
                }
            } else {
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }
}
