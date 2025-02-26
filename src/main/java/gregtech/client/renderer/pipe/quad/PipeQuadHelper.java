package gregtech.client.renderer.pipe.quad;

import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

import javax.vecmath.Vector3f;

@SideOnly(Side.CLIENT)
public final class PipeQuadHelper {

    private SpriteInformation targetSprite;

    private final List<Pair<Vector3f, Vector3f>> coreBoxList = new ObjectArrayList<>();
    private final List<EnumMap<EnumFacing, Pair<Vector3f, Vector3f>>> sideBoxesList = new ObjectArrayList<>();

    private float[] definition;

    public PipeQuadHelper(float x, float y, float z, float small, float large) {
        float xS = (x + small) * 16;
        float xL = (x + large) * 16;
        float yS = (y + small) * 16;
        float yL = (y + large) * 16;
        float zS = (z + small) * 16;
        float zL = (z + large) * 16;
        definition = new float[] { xS, xL, yS, yL, zS, zL };
    }

    @Contract("_ -> this")
    public PipeQuadHelper initialize(OverlayLayerDefinition... overlayLayers) {
        if (definition != null) {
            float xS = definition[0];
            float xL = definition[1];
            float yS = definition[2];
            float yL = definition[3];
            float zS = definition[4];
            float zL = definition[5];
            definition = null;
            generateBox(xS, xL, yS, yL, zS, zL,
                    (facing, x1, y1, z1, x2, y2, z2) -> QuadHelper.toPair(x1, y1, z1, x2, y2, z2));
            for (OverlayLayerDefinition definition : overlayLayers) {
                generateBox(xS, xL, yS, yL, zS, zL, definition);
            }
        }
        return this;
    }

    public int getLayerCount() {
        return coreBoxList.size();
    }

    private void generateBox(float xS, float xL, float yS, float yL, float zS, float zL,
                             @NotNull OverlayLayerDefinition definition) {
        coreBoxList.add(definition.computeBox(null, xS, yS, zS, xL, yL, zL));
        EnumMap<EnumFacing, Pair<Vector3f, Vector3f>> sideBoxes = new EnumMap<>(EnumFacing.class);
        sideBoxes.put(EnumFacing.DOWN, definition.computeBox(EnumFacing.DOWN, xS, 0, zS, xL, yS, zL));
        sideBoxes.put(EnumFacing.UP, definition.computeBox(EnumFacing.UP, xS, yL, zS, xL, 16, zL));
        sideBoxes.put(EnumFacing.NORTH, definition.computeBox(EnumFacing.NORTH, xS, yS, 0, xL, yL, zS));
        sideBoxes.put(EnumFacing.SOUTH, definition.computeBox(EnumFacing.SOUTH, xS, yS, zL, xL, yL, 16));
        sideBoxes.put(EnumFacing.WEST, definition.computeBox(EnumFacing.WEST, 0, yS, zS, xS, yL, zL));
        sideBoxes.put(EnumFacing.EAST, definition.computeBox(EnumFacing.EAST, xL, yS, zS, 16, yL, zL));
        sideBoxesList.add(sideBoxes);
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull PipeQuadHelper create(float thickness, double x, double y, double z) {
        float small = 0.5f - thickness / 2;
        float large = 0.5f + thickness / 2;
        return new PipeQuadHelper((float) x, (float) y, (float) z, small, large);
    }

    @Contract("_ -> new")
    public static @NotNull PipeQuadHelper create(float thickness) {
        return create(thickness, 0, 0, 0);
    }

    public void setTargetSprite(SpriteInformation sprite) {
        this.targetSprite = sprite;
    }

    public @NotNull RecolorableBakedQuad visitCore(EnumFacing facing) {
        return visitCore(facing, 0);
    }

    public @NotNull RecolorableBakedQuad visitCore(EnumFacing facing, int overlayLayer) {
        return visitQuad(facing, coreBoxList.get(overlayLayer), UVMapper.standard(0));
    }

    public @NotNull List<RecolorableBakedQuad> visitTube(EnumFacing facing) {
        return visitTube(facing, 0);
    }

    public @NotNull List<RecolorableBakedQuad> visitTube(EnumFacing facing, int overlayLayer) {
        List<RecolorableBakedQuad> list = new ObjectArrayList<>();
        Pair<Vector3f, Vector3f> box = sideBoxesList.get(overlayLayer).get(facing);
        switch (facing.getAxis()) {
            case X -> {
                list.add(visitQuad(EnumFacing.UP, box, UVMapper.standard(0)));
                list.add(visitQuad(EnumFacing.DOWN, box, UVMapper.standard(0)));
                list.add(visitQuad(EnumFacing.SOUTH, box, UVMapper.standard(0)));
                list.add(visitQuad(EnumFacing.NORTH, box, UVMapper.flipped(0)));
            }
            case Y -> {
                list.add(visitQuad(EnumFacing.EAST, box, UVMapper.standard(0)));
                list.add(visitQuad(EnumFacing.WEST, box, UVMapper.standard(0)));
                list.add(visitQuad(EnumFacing.SOUTH, box, UVMapper.standard(0)));
                list.add(visitQuad(EnumFacing.NORTH, box, UVMapper.standard(0)));
            }
            case Z -> {
                list.add(visitQuad(EnumFacing.UP, box, UVMapper.flipped(0)));
                list.add(visitQuad(EnumFacing.DOWN, box, UVMapper.standard(0)));
                list.add(visitQuad(EnumFacing.EAST, box, UVMapper.flipped(0)));
                list.add(visitQuad(EnumFacing.WEST, box, UVMapper.standard(0)));
            }
        }
        return list;
    }

    public @NotNull RecolorableBakedQuad visitCapper(EnumFacing facing) {
        return visitCapper(facing, 0);
    }

    public @NotNull RecolorableBakedQuad visitCapper(EnumFacing facing, int overlayLayer) {
        return visitQuad(facing, sideBoxesList.get(overlayLayer).get(facing), UVMapper.standard(0));
    }

    public @NotNull RecolorableBakedQuad visitQuad(EnumFacing normal, Pair<Vector3f, Vector3f> box, UVMapper uv) {
        return QuadHelper.buildQuad(normal, box, uv, targetSprite);
    }

    public static @NotNull List<RecolorableBakedQuad> createFrame(TextureAtlasSprite sprite) {
        PipeQuadHelper helper = PipeQuadHelper.create(0.998f).initialize();
        helper.setTargetSprite(new SpriteInformation(sprite, 0));
        List<RecolorableBakedQuad> list = new ObjectArrayList<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
            list.add(helper.visitCore(facing));
        }
        return list;
    }
}
