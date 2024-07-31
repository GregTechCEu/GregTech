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
import org.jetbrains.annotations.Range;
import org.lwjgl.util.vector.Vector3f;

import java.util.EnumMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public final class PipeQuadHelper {

    private static final FaceBakery BAKERY = new FaceBakery();

    private SpriteInformation targetSprite;

    private final Pair<Vector3f, Vector3f> coreBox;
    private final EnumMap<EnumFacing, Pair<Vector3f, Vector3f>> sideBoxes = new EnumMap<>(EnumFacing.class);

    public PipeQuadHelper(float x, float y, float z, int argb, float small, float large) {
        float xS = (x + small) * 16;
        float xL = (x + large) * 16;
        float yS = (y + small) * 16;
        float yL = (y + large) * 16;
        float zS = (z + small) * 16;
        float zL = (z + large) * 16;
        coreBox = ImmutablePair.of(new Vector3f(xS, yS, zS), new Vector3f(xL, yL, zL));
        sideBoxes.put(EnumFacing.DOWN, ImmutablePair.of(new Vector3f(xS, 0, zS), new Vector3f(xL, yS, zL)));
        sideBoxes.put(EnumFacing.UP, ImmutablePair.of(new Vector3f(xS, yL, zS), new Vector3f(xL, 16, zL)));
        sideBoxes.put(EnumFacing.NORTH, ImmutablePair.of(new Vector3f(xS, yS, 0), new Vector3f(xL, yL, zS)));
        sideBoxes.put(EnumFacing.SOUTH, ImmutablePair.of(new Vector3f(xS, yS, zL), new Vector3f(xL, yL, 16)));
        sideBoxes.put(EnumFacing.WEST, ImmutablePair.of(new Vector3f(0, yS, zS), new Vector3f(xS, yL, zL)));
        sideBoxes.put(EnumFacing.EAST, ImmutablePair.of(new Vector3f(xL, yS, zS), new Vector3f(16, yL, zL)));
    }

    public static PipeQuadHelper create(float thickness, double x, double y, double z, int argb) {
        float small = 0.5f - thickness / 2;
        float large = 0.5f + thickness / 2;
        return new PipeQuadHelper((float) x, (float) y, (float) z, argb, small, large);
    }

    public static PipeQuadHelper create(float thickness) {
        return create(thickness, 0, 0, 0, 0xFFFFFFFF);
    }

    public void setTargetSprite(SpriteInformation sprite) {
        this.targetSprite = sprite;
    }

    public RecolorableBakedQuad visitCore(EnumFacing facing) {
        return visitQuad(facing, coreBox, uvMapper(0));
    }

    public List<RecolorableBakedQuad> visitTube(EnumFacing facing) {
        List<RecolorableBakedQuad> list = new ObjectArrayList<>();
        Pair<Vector3f, Vector3f> box = sideBoxes.get(facing);
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
        return visitQuad(facing, sideBoxes.get(facing), uvMapper(0));
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
        helper.setTargetSprite(new SpriteInformation(sprite, true));
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
