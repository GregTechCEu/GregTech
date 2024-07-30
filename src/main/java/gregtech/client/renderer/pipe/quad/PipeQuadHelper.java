package gregtech.client.renderer.pipe.quad;

import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.List;

@SideOnly(Side.CLIENT)
public final class PipeQuadHelper {

    private static final VertexFormatElement NORMAL_4F = new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.NORMAL, 4);
    private static final VertexFormat FORMAT = new VertexFormat(DefaultVertexFormats.BLOCK).addElement(NORMAL_4F);
    private static final TRSRTransformation INVERTER;
    static {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.mul(-1);
        INVERTER = new TRSRTransformation(matrix4f);
    }

    private final float thickness;
    private final float x;
    private final float y;
    private final float z;
    private final int argb;
    private final float small;
    private final float large;
    private final UVMapper squareMapper;
    private final UVMapper tallMapper;
    private final UVMapper wideMapper;

    private IVertexConsumer building;

    private SpriteInformation targetSprite;

    public PipeQuadHelper(float thickness, float x, float y, float z, int argb, float small, float large,
                          UVMapper squareMapper, UVMapper tallMapper, UVMapper wideMapper) {
        this.thickness = thickness;
        this.x = x;
        this.y = y;
        this.z = z;
        this.argb = argb;
        this.small = small;
        this.large = large;
        this.squareMapper = squareMapper;
        this.tallMapper = tallMapper;
        this.wideMapper = wideMapper;
    }

    public static PipeQuadHelper create(float thickness, double x, double y, double z,
                                        int argb) {
        float small = 0.5f - thickness / 2;
        float large = 0.5f + thickness / 2;
        float small16 = small * 16;
        float large16 = large * 16;
        UVMapper squareMapper = createMapper(small16, small16, large16, large16);
        UVMapper tallMapper = createMapper(large16, small16, 16, large16);
        UVMapper wideMapper = createRotatedMapper(large16, small16, 16, large16);
        return new PipeQuadHelper(thickness, (float) x, (float) y, (float) z, argb, small, large, squareMapper,
                tallMapper, wideMapper);
    }

    public static PipeQuadHelper create(float thickness) {
        return create(thickness, 0, 0, 0, 0xFFFFFFFF);
    }

    public void setTargetSprite(SpriteInformation sprite) {
        this.targetSprite = sprite;
    }

    public RecolorableBakedQuad visitCore(EnumFacing facing) {
        return switch (facing.getAxis()) {
            case X -> visitQuad(facing, 0.5f + thickness / 2 * facing.getAxisDirection().getOffset(), 0, y + small,
                    y + large, z + small, z + large, squareMapper);
            case Y -> visitQuad(facing, x + small, x + large,
                    0.5f + thickness / 2 * facing.getAxisDirection().getOffset(), 0, z + small, z + large,
                    squareMapper);
            case Z -> visitQuad(facing, x + small, x + large, y + small, y + large,
                    0.5f + thickness / 2 * facing.getAxisDirection().getOffset(), 0, squareMapper);
        };
    }

    public List<RecolorableBakedQuad> visitTube(EnumFacing facing) {
        List<RecolorableBakedQuad> list = new ObjectArrayList<>();
        switch (facing.getAxis()) {
            case X -> {
                float x1;
                float x2;
                if (facing == EnumFacing.EAST) {
                    x1 = this.x + large;
                    x2 = 1;
                } else {
                    x1 = this.x + small;
                    x2 = 0;
                }
                list.add(visitQuad(EnumFacing.UP, x1, x2, y + large, 0, z + small, z + large, wideMapper));
                list.add(visitQuad(EnumFacing.DOWN, x1, x2, y + small, 0, z + small, z + large, wideMapper));
                list.add(visitQuad(EnumFacing.SOUTH, x1, x2, y + small, y + large, z + large, 0, tallMapper));
                list.add(visitQuad(EnumFacing.NORTH, x1, x2, y + small, y + large, z + small, 0, tallMapper));
            }
            case Y -> {
                float y1;
                float y2;
                if (facing == EnumFacing.UP) {
                    y1 = this.y + large;
                    y2 = 1;
                } else {
                    y1 = this.y + small;
                    y2 = 0;
                }
                list.add(visitQuad(EnumFacing.EAST, x + large, 0, y1, y2, z + small, z + large, wideMapper));
                list.add(visitQuad(EnumFacing.WEST, x + small, 0, y1, y2, z + small, z + large, wideMapper));
                list.add(visitQuad(EnumFacing.SOUTH, x + small, x + large, y1, y2, z + large, 0, wideMapper));
                list.add(visitQuad(EnumFacing.NORTH, x + small, x + large, y1, y2, z + small, 0, wideMapper));
            }
            case Z -> {
                float z1;
                float z2;
                if (facing == EnumFacing.SOUTH) {
                    z1 = this.z + large;
                    z2 = 1;
                } else {
                    z1 = this.z + small;
                    z2 = 0;
                }
                list.add(visitQuad(EnumFacing.UP, x + small, x + large, y + large, 0, z1, z2, tallMapper));
                list.add(visitQuad(EnumFacing.DOWN, x + small, x + large, y + small, 0, z1, z2, tallMapper));
                list.add(visitQuad(EnumFacing.EAST, x + large, 0, y + small, y + large, z1, z2, tallMapper));
                list.add(visitQuad(EnumFacing.WEST, x + small, 0, y + small, y + large, z1, z2, tallMapper));
            }
        }
        return list;
    }

    public RecolorableBakedQuad visitCapper(EnumFacing facing) {
        return switch (facing.getAxis()) {
            case X -> visitQuad(facing, 0.5f + 0.5f * facing.getAxisDirection().getOffset(), 0, y + small, y + large,
                    z + small, z + large, squareMapper);
            case Y -> visitQuad(facing, x + small, x + large, 0.5f + 0.5f * facing.getAxisDirection().getOffset(), 0,
                    z + small, z + large, squareMapper);
            case Z -> visitQuad(facing, x + small, x + large, y + small, y + large,
                    0.5f + 0.5f * facing.getAxisDirection().getOffset(), 0, squareMapper);
        };
    }

    public RecolorableBakedQuad visitQuad(EnumFacing normal, float x1, float x2, float y1, float y2, float z1, float z2,
                                          UVMapper mapper) {
        RecolorableBakedQuad.Builder builder = new RecolorableBakedQuad.Builder(FORMAT);
        building = builder;
        UVCorner[] array = UVCorner.VALUES;
        switch (normal.getAxis()) {
            case X -> {
                visitVertex(normal, x1, y1, z1, mapper.map(array[0], targetSprite.sprite()));
                visitVertex(normal, x1, y1, z2, mapper.map(array[1], targetSprite.sprite()));
                visitVertex(normal, x1, y2, z2, mapper.map(array[2], targetSprite.sprite()));
                visitVertex(normal, x1, y2, z1, mapper.map(array[3], targetSprite.sprite()));
            }
            case Y -> {
                visitVertex(normal, x1, y1, z1, mapper.map(array[0], targetSprite.sprite()));
                visitVertex(normal, x1, y1, z2, mapper.map(array[1], targetSprite.sprite()));
                visitVertex(normal, x2, y1, z2, mapper.map(array[2], targetSprite.sprite()));
                visitVertex(normal, x2, y1, z1, mapper.map(array[3], targetSprite.sprite()));
            }
            case Z -> {
                visitVertex(normal, x1, y1, z1, mapper.map(array[0], targetSprite.sprite()));
                visitVertex(normal, x2, y1, z1, mapper.map(array[1], targetSprite.sprite()));
                visitVertex(normal, x2, y2, z1, mapper.map(array[2], targetSprite.sprite()));
                visitVertex(normal, x1, y2, z1, mapper.map(array[3], targetSprite.sprite()));
            }
        }
        building = null;
        builder.setQuadOrientation(normal);
        builder.setTexture(targetSprite);
        return builder.build();
    }

    private void visitVertex(EnumFacing normal, float x, float y, float z, UVMapper.UVPair pair) {
        putVertex(building, FORMAT, normal, x, y, z, pair.u(), pair.v(), argb);
    }

    public static UVMapper createMapper(double u1, double v1, double u2, double v2) {
        return (corner, sprite) -> switch (corner) {
            case UL -> UVMapper.uvPair(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1));
            case UR -> UVMapper.uvPair(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v1));
            case DR -> UVMapper.uvPair(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v2));
            case DL -> UVMapper.uvPair(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v2));
        };
    }

    public static UVMapper createRotatedMapper(double u1, double v1, double u2, double v2) {
        return (corner, sprite) -> switch (corner) {
            case UR -> UVMapper.uvPair(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1));
            case DR -> UVMapper.uvPair(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v1));
            case DL -> UVMapper.uvPair(sprite.getInterpolatedU(u2), sprite.getInterpolatedV(v2));
            case UL -> UVMapper.uvPair(sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v2));
        };
    }

    @SuppressWarnings("SameParameterValue")
    private static void putVertex(IVertexConsumer consumer, VertexFormat format, EnumFacing normal,
                                  float x, float y, float z, float u, float v, int argb) {
        for (int e = 0; e < format.getElementCount(); e++) {
            switch (format.getElement(e).getUsage()) {
                case POSITION:
                    consumer.put(e, x, y, z, 1f);
                    break;
                case COLOR:
                    float a = ((argb >> 24) & 0xFF) / 255f; // alpha
                    float r = ((argb >> 16) & 0xFF) / 255f; // red
                    float g = ((argb >> 8) & 0xFF) / 255f; // green
                    float b = ((argb) & 0xFF) / 255f; // blue
                    consumer.put(e, r, g, b, a);
                    break;
                case NORMAL:
                    float offX = (float) normal.getXOffset();
                    float offY = (float) normal.getYOffset();
                    float offZ = (float) normal.getZOffset();
                    consumer.put(e, offX, offY, offZ, -1f);
                    break;
                case UV:
                    if (format.getElement(e).getIndex() == 0) {
                        consumer.put(e, u, v, 0f, 1f);
                        break;
                    }
                    // else fallthrough to default
                default:
                    consumer.put(e);
                    break;
            }
        }
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
}
