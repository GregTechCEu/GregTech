package gregtech.client.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;

import javax.vecmath.Vector3f;

public class GTRendererState {

    private static final ThreadLocal<GTRendererState> renderState = ThreadLocal.withInitial(GTRendererState::new);

    public static GTRendererState getCurrentState() {
        return renderState.get();
    }

    // render information
    public BufferBuilder buf;
    private VertexBufferConsumer consumer;
    private VertexLighterSmoothAo smoothAo;
    private VertexLighterFlat flat;
    private VertexFormat fmt;
    public TextureAtlasSprite sprite;
    public final float[] bounds = new float[6];

    // state information
    public IBlockState state;
    public IBlockAccess world;
    public final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public GTRendererState setBuffer(BufferBuilder buf) {
        this.consumer = new VertexBufferConsumer(buf);
        this.buf = buf;
        this.fmt = buf.getVertexFormat();
        return this;
    }

    public GTRendererState updateLighting(BlockColors colors) {
        this.flat = new VertexLighterFlat(colors);
        this.smoothAo = new VertexLighterSmoothAo(colors);
        return this;
    }

    public GTRendererState updateState(IBlockState state, IBlockAccess world, BlockPos pos) {
        this.state = state;
        this.world = world;
        this.pos.setPos(pos);
        return this;
    }

    public GTRendererState setTexture(TextureAtlasSprite sprite) {
        this.sprite = sprite;
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
        return quad(side, MinecraftForgeClient.getRenderLayer());
    }

    public GTRendererState quad(EnumFacing side, BlockRenderLayer renderLayer) {
        // we currently render on every valid layer
        // this should be deferred to the Texture
        if (renderLayer == null || !this.state.getBlock().canRenderInLayer(this.state, renderLayer)) return this;
        if (renderLayer != MinecraftForgeClient.getRenderLayer()) return this;
        if (!this.state.shouldSideBeRendered(world, pos, side)) return this;

        Quad quad = Quad.fillQuad(this.bounds, EnumFaceDirection.getFacing(side), sprite, -1);
        // the buffer probably expects vertex data to be in the int buffer?
        // that's odd, since all the buffers are just the byte buf
        // quads are also darker than expected, this might be because AO and world lighting are not handled
        // buf.addVertexData(quad.toArray());

        VertexLighterFlat lighter;
        if (Minecraft.isAmbientOcclusionEnabled() && this.state.getLightValue(this.world, this.pos) == 0) {
            lighter = this.smoothAo;
        } else {
            lighter = this.flat;
        }

        this.consumer.setOffset(pos);
        lighter.setParent(this.consumer);
        lighter.setWorld(this.world);
        lighter.setState(this.state);
        lighter.setBlockPos(this.pos);
        lighter.updateBlockInfo();
        // todo pipeline?
        putBakedQuad(lighter, quad, side);

        return this;
    }

    public void putBakedQuad(IVertexConsumer consumer, Quad quad, EnumFacing side) {
        consumer.setTexture(quad.sprite);
        consumer.setQuadOrientation(side);
        float[] data = new float[4];

        VertexFormat formatFrom = consumer.getVertexFormat();
        VertexFormat formatTo = this.fmt;

        int countFrom = formatFrom.getElementCount();
        int countTo = formatTo.getElementCount();
        int[] eMap = LightUtil.mapFormats(formatFrom, formatTo);
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < countFrom; e++) {
                if (eMap[e] != countTo) {
                    LightUtil.unpack(quad.toArray(), data, formatTo, v, eMap[e]);
                    consumer.put(e, data);
                } else {
                    consumer.put(e);
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
