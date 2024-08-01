package gregtech.client.renderer.pipe.quad;

import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.lwjgl.util.vector.Vector3f;

import java.util.EnumMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public final class PipeQuadHelper {

    private static final FaceBakery BAKERY = new FaceBakery();

    private SpriteInformation targetSprite;

    private final List<Pair<Vector3f, Vector3f>> coreBoxList = new ObjectArrayList<>();
    private final List<EnumMap<EnumFacing, Pair<Vector3f, Vector3f>>> sideBoxesList = new ObjectArrayList<>();

    private float[] definition;

    public static ImmutablePair<Vector3f, Vector3f> pair(float x1, float y1, float z1, float x2, float y2, float z2) {
        return ImmutablePair.of(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2));
    }

    public PipeQuadHelper(float x, float y, float z, float small, float large) {
        float xS = (x + small) * 16;
        float xL = (x + large) * 16;
        float yS = (y + small) * 16;
        float yL = (y + large) * 16;
        float zS = (z + small) * 16;
        float zL = (z + large) * 16;
        definition = new float[] {xS, xL, yS, yL, zS, zL};
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
            generateBox(xS, xL, yS, yL, zS, zL, (facing, x1, y1, z1, x2, y2, z2) ->
                    ImmutablePair.of(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2)));
            for (OverlayLayerDefinition definition : overlayLayers) {
                generateBox(xS, xL, yS, yL, zS, zL, definition);
            }
        }
        return this;
    }

    public int getLayerCount() {
        return coreBoxList.size();
    }

    private void generateBox(float xS, float xL, float yS, float yL, float zS, float zL, OverlayLayerDefinition definition) {
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

    public static PipeQuadHelper create(float thickness, double x, double y, double z) {
        float small = 0.5f - thickness / 2;
        float large = 0.5f + thickness / 2;
        return new PipeQuadHelper((float) x, (float) y, (float) z, small, large);
    }

    public static PipeQuadHelper create(float thickness) {
        return create(thickness, 0, 0, 0);
    }

    public void setTargetSprite(SpriteInformation sprite) {
        this.targetSprite = sprite;
    }

    public RecolorableBakedQuad visitCore(EnumFacing facing) {
        return visitCore(facing, 0);
    }

    public RecolorableBakedQuad visitCore(EnumFacing facing, int overlayLayer) {
        return visitQuad(facing, coreBoxList.get(overlayLayer), uvMapper(0));
    }

    public List<RecolorableBakedQuad> visitTube(EnumFacing facing) {
        return visitTube(facing, 0);
    }

    public List<RecolorableBakedQuad> visitTube(EnumFacing facing, int overlayLayer) {
        List<RecolorableBakedQuad> list = new ObjectArrayList<>();
        Pair<Vector3f, Vector3f> box = sideBoxesList.get(overlayLayer).get(facing);
        switch (facing.getAxis()) {
            case X -> {
                UVMapper mapper = uvMapper(0);
                list.add(visitQuad(EnumFacing.UP, box, mapper));
                list.add(visitQuad(EnumFacing.DOWN, box, mapper));
                list.add(visitQuad(EnumFacing.SOUTH, box, mapper));
                list.add(visitQuad(EnumFacing.NORTH, box, uvMapper(180)));
            }
            case Y -> {
                UVMapper mapper = uvMapper(0);
                list.add(visitQuad(EnumFacing.EAST, box, uvMapper(270)));
                list.add(visitQuad(EnumFacing.WEST, box, uvMapper(270)));
                list.add(visitQuad(EnumFacing.SOUTH, box, mapper));
                list.add(visitQuad(EnumFacing.NORTH, box, mapper));
            }
            case Z -> {
                list.add(visitQuad(EnumFacing.UP, box, uvMapper(180)));
                list.add(visitQuad(EnumFacing.DOWN, box, uvMapper(0)));
                list.add(visitQuad(EnumFacing.EAST, box, uvMapper(270)));
                list.add(visitQuad(EnumFacing.WEST, box, uvMapper(90)));
            }
        }
        return list;
    }

    public RecolorableBakedQuad visitCapper(EnumFacing facing) {
        return visitCapper(facing, 0);
    }

    public RecolorableBakedQuad visitCapper(EnumFacing facing, int overlayLayer) {
        return visitQuad(facing, sideBoxesList.get(overlayLayer).get(facing), uvMapper(0));
    }

    public RecolorableBakedQuad visitQuad(EnumFacing normal, Pair<Vector3f, Vector3f> box, UVMapper uv) {
        BlockPartFace face = new BlockPartFace(null, -1, targetSprite.sprite().getIconName(), uv.map(normal, box));
        BakedQuad quad = BAKERY.makeBakedQuad(box.getLeft(), box.getRight(), face, targetSprite.sprite(), normal, ModelRotation.X0_Y0, null, false, true);
        RecolorableBakedQuad.Builder builder = new RecolorableBakedQuad.Builder(quad.getFormat());
        builder.setTexture(targetSprite);
        quad.pipe(builder);
        return builder.build();
    }

    private static UVMapper uvMapper(int rot) {
        return (normal, box) -> uv(normal, box, rot);
    }

    private static BlockFaceUV uv(EnumFacing normal, Pair<Vector3f, Vector3f> box, int rot) {
        Vector3f small = box.getLeft();
        Vector3f large = box.getRight();
        return switch (normal.getAxis()) {
            case X -> new BlockFaceUV(new float[] {small.y, large.z, large.y, small.z}, rot);
            case Y -> new BlockFaceUV(new float[] {small.x, large.z, large.x, small.z}, rot);
            case Z -> new BlockFaceUV(new float[] {small.x, large.y, large.x, small.y}, rot);
        };
    }

    public static List<RecolorableBakedQuad> createFrame(TextureAtlasSprite sprite) {
        PipeQuadHelper helper = PipeQuadHelper.create(0.998f);
        helper.setTargetSprite(new SpriteInformation(sprite, 0));
        List<RecolorableBakedQuad> list = new ObjectArrayList<>();
        for (EnumFacing facing : EnumFacing.VALUES) {
            list.add(helper.visitCore(facing));
        }
        return list;
    }

    @FunctionalInterface
    public interface UVMapper {

        BlockFaceUV map(EnumFacing normal, Pair<Vector3f, Vector3f> box);
    }
}
