package gregtech.client.renderer.pipe.cache;

import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.quad.*;
import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

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
        if (helper.getLayerCount() < 1)
            throw new IllegalStateException("Cannot create an SQC without at least one layer present on the helper!");
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
            } else {
                list.addAll(coreCoords.get(facing).getSublist(quads));
            }
        }
    }
}
