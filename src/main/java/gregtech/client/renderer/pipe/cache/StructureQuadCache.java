package gregtech.client.renderer.pipe.cache;

import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.OverlayLayerDefinition;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.quad.RecolorableBakedQuad;
import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.EnumMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public class StructureQuadCache {

    public static final float OVERLAY_DIST_1 = 0.003f;
    public static final float OVERLAY_DIST_2 = 0.006f;

    protected final PipeQuadHelper helper;

    protected ColorQuadCache cache;

    protected final EnumMap<EnumFacing, SubListAddress> tubeCoords = new EnumMap<>(EnumFacing.class);

    protected final EnumMap<EnumFacing, SubListAddress> coreCoords = new EnumMap<>(EnumFacing.class);
    protected final EnumMap<EnumFacing, SubListAddress> capperCoords = new EnumMap<>(EnumFacing.class);
    protected final EnumMap<EnumFacing, SubListAddress> capperClosedCoords = new EnumMap<>(EnumFacing.class);

    protected final SpriteInformation endTex;
    protected final SpriteInformation sideTex;

    protected StructureQuadCache(PipeQuadHelper helper, SpriteInformation endTex, SpriteInformation sideTex) {
        this.helper = helper;
        this.endTex = endTex;
        this.sideTex = sideTex;
        if (helper.getLayerCount() < 1) throw new IllegalStateException("Cannot create an SQC without at least one layer present on the helper!");
    }

    public static @NotNull StructureQuadCache create(PipeQuadHelper helper, SpriteInformation endTex,
                                                     SpriteInformation sideTex) {
        StructureQuadCache cache = new StructureQuadCache(helper.initialize(), endTex, sideTex);
        cache.buildPrototype();
        return cache;
    }

    protected void buildPrototype() {
        this.cache = new ColorQuadCache(this.buildPrototypeInternal());
    }

    protected List<RecolorableBakedQuad> buildPrototypeInternal() {
        List<RecolorableBakedQuad> quads = new ObjectArrayList<>();
        buildTube(quads);
        buildCore(quads);
        buildCapper(quads);
        buildCapperClosed(quads);
        return quads;
    }

    protected void buildTube(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(sideTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.addAll(helper.visitTube(facing));
            tubeCoords.put(facing, new SubListAddress(start, list.size()));
        }
    }

    protected void buildCore(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(sideTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.add(helper.visitCore(facing));
            coreCoords.put(facing, new SubListAddress(start, start + 1));
        }
    }

    protected void buildCapper(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(endTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.add(helper.visitCapper(facing));
            capperCoords.put(facing, new SubListAddress(start, start + 1));
        }
    }

    protected void buildCapperClosed(List<RecolorableBakedQuad> list) {
        helper.setTargetSprite(sideTex);
        for (EnumFacing facing : EnumFacing.VALUES) {
            int start = list.size();
            list.add(helper.visitCapper(facing));
            capperClosedCoords.put(facing, new SubListAddress(start, start + 1));
        }
    }

    public void addToList(List<BakedQuad> list, byte connectionMask, byte closedMask, byte blockedMask, ColorData data) {
        List<BakedQuad> quads = cache.getQuads(data);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (GTUtility.evalMask(facing, connectionMask)) {
                list.addAll(tubeCoords.get(facing).getSublist(quads));
                if (GTUtility.evalMask(facing, closedMask)) {
                    list.addAll(capperClosedCoords.get(facing).getSublist(quads));
                } else {
                    list.addAll(capperCoords.get(facing).getSublist(quads));
                }
            } else {
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }

    @Desugar
    protected record SubListAddress(int startInclusive, int endExclusive) {

        public <T> @NotNull List<T> getSublist(@NotNull List<T> list) {
            return list.subList(startInclusive, endExclusive);
        }
    }

    public static ImmutablePair<Vector3f, Vector3f> capOverlay(@Nullable EnumFacing facing, float x1, float y1, float z1, float x2, float y2, float z2, float g) {
        if (facing == null) return PipeQuadHelper.pair(x1 - g, y1 - g, z1 - g, x2 + g, y2 + g, z2 + g);
        return switch (facing.getAxis()) {
            case X -> PipeQuadHelper.pair(x1 - g, y1, z1, x2 + g, y2, z2);
            case Y -> PipeQuadHelper.pair(x1, y1 - g, z1, x2, y2 + g, z2);
            case Z -> PipeQuadHelper.pair(x1, y1, z1 - g, x2, y2, z2 + g);
        };
    }

    public static ImmutablePair<Vector3f, Vector3f> tubeOverlay(@Nullable EnumFacing facing, float x1, float y1, float z1, float x2, float y2, float z2, float g) {
        if (facing == null) return PipeQuadHelper.pair(x1, y1, z1, x2, y2, z2);
        return switch (facing.getAxis()) {
            case X -> PipeQuadHelper.pair(x1, y1 - g, z1 - g, x2, y2 + g, z2 + g);
            case Y -> PipeQuadHelper.pair(x1 - g, y1, z1 - g, x2 + g, y2, z2 + g);
            case Z -> PipeQuadHelper.pair(x1 - g, y1 - g, z1, x2 + g, y2 + g, z2);
        };
    }

    public static ImmutablePair<Vector3f, Vector3f> fullOverlay(@Nullable EnumFacing facing, float x1, float y1, float z1, float x2, float y2, float z2, float g) {
        return PipeQuadHelper.pair(x1 - g, y1 - g, z1 - g, x2 + g, y2 + g, z2 + g);
    }
}
