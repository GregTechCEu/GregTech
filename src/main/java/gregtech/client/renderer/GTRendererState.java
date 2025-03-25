package gregtech.client.renderer;

import gregtech.client.renderer.texture.RenderContext;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

import java.util.EnumMap;

import javax.vecmath.Vector3f;

public class GTRendererState {

    private static final ThreadLocal<GTRendererState> renderState = ThreadLocal.withInitial(GTRendererState::new);

    public static GTRendererState getCurrentState() {
        return renderState.get();
    }

    // render information
    private VertexBufferConsumer consumer;
    private VertexLighterSmoothAo smoothAo;
    private VertexLighterFlat flat;
    private VertexFormat fmt;
    public TextureAtlasSprite sprite;
    private boolean shade;
    public final float[] bounds = new float[6];
    private final EnumMap<BlockRenderLayer, ReferenceSet<RenderOperation>> operations = new EnumMap<>(
            BlockRenderLayer.class);

    {
        for (BlockRenderLayer value : BlockRenderLayer.values()) {
            operations.put(value, new ReferenceArraySet<>());
        }
    }

    // state information
    RenderContext context;

    public GTRendererState setBuffer(BufferBuilder buf) {
        this.consumer = new VertexBufferConsumer(buf);
        this.fmt = buf.getVertexFormat();
        return this;
    }

    public GTRendererState updateLighting(BlockColors colors) {
        this.flat = new VertexLighterFlat(colors);
        this.smoothAo = new VertexLighterSmoothAo(colors);
        return this;
    }

    public GTRendererState updateState(RenderContext context) {
        this.context = context;
        return this;
    }

    public GTRendererState setTexture(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        return this;
    }

    public GTRendererState setShading(boolean shade) {
        this.shade = shade;
        return this;
    }

    public GTRendererState fullBlock() {
        return setBounds(16, 16, 16);
    }

    public GTRendererState setBounds(Vector3f size) {
        return setBounds(size.x, size.y, size.z);
    }

    public GTRendererState setBounds(float w, float h, float l) {
        return setBounds(0, 0, 0, w, h, l);
    }

    public GTRendererState setBounds(AxisAlignedBB bounds) {
        return setBounds(
                (float) bounds.minX,
                (float) bounds.minY,
                (float) bounds.minZ,
                (float) bounds.maxX,
                (float) bounds.maxY,
                (float) bounds.maxZ);
    }

    public GTRendererState setBounds(float... bounds) {
        if (bounds == null || bounds.length != 6)
            throw new IllegalArgumentException("Bounds must be [min x, min y, min z, max x, max y, max z]!");
        // mc expects the bounds to be in a VERY specific order
        this.bounds[EnumFaceDirection.Constants.WEST_INDEX] = bounds[0] / 16.0F;
        this.bounds[EnumFaceDirection.Constants.DOWN_INDEX] = bounds[1] / 16.0F;
        this.bounds[EnumFaceDirection.Constants.NORTH_INDEX] = bounds[2] / 16.0F;
        this.bounds[EnumFaceDirection.Constants.EAST_INDEX] = bounds[3] / 16.0F;
        this.bounds[EnumFaceDirection.Constants.UP_INDEX] = bounds[4] / 16.0F;
        this.bounds[EnumFaceDirection.Constants.SOUTH_INDEX] = bounds[5] / 16.0F;
        return this;
    }

    public GTRendererState quad(EnumFacing side) {
        if (!this.context.canRender(side)) return this;

        int k = this.shade ? getFaceShadeColor(side) : -1;
        Quad quad = Quad.fillQuad(this.bounds, EnumFaceDirection.getFacing(side), sprite, k);

        VertexLighterFlat lighter;
        if (this.context.useAo()) {
            lighter = this.smoothAo;
        } else {
            lighter = this.flat;
        }

        this.context.updateLighter(lighter, this.consumer);

        // todo pipeline?
        putBakedQuad(lighter, quad, side);

        return this;
    }

    public GTRendererState pushQuad(EnumFacing side, BlockRenderLayer renderLayer, TextureAtlasSprite sprite) {
        operations.get(renderLayer).add(state -> state.setTexture(sprite).quad(side));
        return this;
    }

    public void render(BlockRenderLayer layer) {
        for (RenderOperation op : operations.get(layer)) {
            op.operate(this);
        }
    }

    public void putBakedQuad(IVertexConsumer lighter, Quad quad, EnumFacing side) {
        lighter.setTexture(quad.sprite);
        lighter.setQuadOrientation(side);
        float[] data = new float[4];

        VertexFormat formatFrom = lighter.getVertexFormat();
        VertexFormat formatTo = this.fmt;

        int countFrom = formatFrom.getElementCount();
        int countTo = formatTo.getElementCount();
        int[] eMap = LightUtil.mapFormats(formatFrom, formatTo);
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < countFrom; e++) {
                if (eMap[e] != countTo) {
                    LightUtil.unpack(quad.toArray(), data, formatTo, v, eMap[e]);
                    lighter.put(e, data);
                } else {
                    lighter.put(e);
                }
            }
        }
    }

    private static int getFaceShadeColor(EnumFacing facing) {
        float f = getFaceBrightness(facing);
        int i = (int) (f * 255.0F);
        return 0xFF000000 | i << 16 | i << 8 | i;
    }

    private static float getFaceBrightness(EnumFacing facing) {
        return switch (facing) {
            case DOWN -> 0.5F;
            case UP -> 1.0F;
            case NORTH, SOUTH -> 0.8F;
            case WEST, EAST -> 0.6F;
        };
    }

    public final static class UV {

        final float[] uvs;

        public UV(float... uvs) {
            if (uvs == null || uvs.length != 4)
                throw new IllegalArgumentException();

            this.uvs = uvs;
        }

        public UV() {
            this(new float[4]);
        }

        public void setUV(float minU, float minV, float maxU, float maxV) {
            uvs[0] = Math.max(0, minU);
            uvs[1] = Math.max(0, minV);
            uvs[2] = Math.min(16, maxU);
            uvs[3] = Math.min(16, maxV);
        }

        public float getVertexU(int vIndex) {
            return vIndex != 0 && vIndex != 1 ? this.uvs[2] : this.uvs[0];
        }

        public float getVertexV(int vIndex) {
            return vIndex != 0 && vIndex != 3 ? this.uvs[3] : this.uvs[1];
        }
    }

    public interface RenderOperation {

        void operate(GTRendererState state);
    }

    public final static class Quad {

        final Vertex[] vertices = new Vertex[] {
                new Vertex(0),
                new Vertex(1),
                new Vertex(2),
                new Vertex(3),
        };

        TextureAtlasSprite sprite;

        public static Quad fillQuad(float[] bounds, EnumFaceDirection direction, TextureAtlasSprite sprite, int color) {
            var quad = new Quad();
            quad.sprite = sprite;
            for (var vertex : quad.vertices) {
                var info = direction.getVertexInformation(vertex.index);
                float x = bounds[info.xIndex];
                float y = bounds[info.yIndex];
                float z = bounds[info.zIndex];
                vertex.pos.set(x, y, z);
                vertex.uvs.setUV(0, 0, 16, 16);
                vertex.colour = color;
            }
            return quad;
        }

        public int[] toArray() {
            int[] data = new int[vertices.length * 7];
            writeVertex(data);
            return data;
        }

        public void writeVertex(int[] vdata) {
            for (Vertex vertex : vertices) {
                vertex.fillData(vdata, sprite);
            }
        }

        @Override
        public String toString() {
            return toString(false);
        }

        public String toString(boolean newline) {
            var b = new StringBuilder(getClass().getSimpleName());
            b.append('[');
            if (newline) b.append('\n').append('\t');
            for (Vertex vertex : vertices) {
                b.append(vertex.toString());
                if (vertex.index != 3) {
                    b.append(", ");
                    if (newline) b.append("\n\t");
                }
            }
            if (newline) b.append("\n");
            return b.append(']').toString();
        }
    }

    public final static class Vertex {

        final Vector3f pos = new Vector3f();
        final UV uvs = new UV();
        final int index;
        int colour = -1;

        private Vertex(int index) {
            this.index = index;
        }

        private void fillData(int[] vData, TextureAtlasSprite sprite) {
            int i = index * 7;

            vData[i] = Float.floatToRawIntBits(pos.x);
            vData[i + 1] = Float.floatToRawIntBits(pos.y);
            vData[i + 2] = Float.floatToRawIntBits(pos.z);
            vData[i + 3] = colour; // does this color actually do anything?
            vData[i + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(uvs.getVertexU(index)));
            vData[i + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV(uvs.getVertexV(index)));
        }

        @Override
        public String toString() {
            return "Vertex_" + index + "{pos=" + pos + '}';
        }
    }
}
