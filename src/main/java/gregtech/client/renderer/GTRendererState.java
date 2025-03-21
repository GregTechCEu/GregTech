package gregtech.client.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;

import java.util.EnumMap;

import javax.vecmath.Vector3f;

public class GTRendererState {

    public static ThreadLocal<GTRendererState> renderState = ThreadLocal.withInitial(GTRendererState::new);

    public static GTRendererState getCurrentState() {
        return renderState.get();
    }

    // render information
    private BufferBuilder buf;
    private VertexFormat fmt;
    private TextureAtlasSprite sprite;
    private float[] bounds;

    // state information
    private IBlockState state;
    private IBlockAccess world;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public GTRendererState setBuffer(BufferBuilder buf) {
        this.buf = buf;
        this.fmt = buf.getVertexFormat();
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

    public GTRendererState setBounds(Vector3f size) {
        return setBounds(
                0,
                0,
                0,
                size.x,
                size.y,
                size.z);
    }

    public GTRendererState setBounds(float w, float h, float l) {
        return setBounds(
                0,
                0,
                0,
                w,
                h,
                l);
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

    private GTRendererState setBounds(float... bounds) {
        this.bounds = bounds;
        return this;
    }

    public GTRendererState quad(EnumFacing side) {
        BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
        if (renderLayer == null || !this.state.getBlock().canRenderInLayer(this.state, renderLayer)) return this;
        if (!this.state.shouldSideBeRendered(world, pos, side)) return this;

        Quad quad = Quad.fillQuad(this.bounds, side, sprite);
        // if (checkQuad(quad, side)) {
        // GTLog.logger.warn("invalid quad: {}", quad);
        // return this;
        // }
        buf.addVertexData(quad.toArray());

        // todo pipeline
        buf.putBrightness4(0xFF, 0xFF, 0xFF, 0xFF);
        buf.putColorMultiplier(1, 1, 1, 4);
        buf.putColorMultiplier(1, 1, 1, 3);
        buf.putColorMultiplier(1, 1, 1, 2);
        buf.putColorMultiplier(1, 1, 1, 1);
        buf.putPosition(pos.getX(), pos.getY(), pos.getZ());
        return this;
    }

    private boolean checkQuad(Quad quad, EnumFacing facing) {
        boolean fail = false;
        float minU = 0, minV = 0;
        float maxU = 1, maxV = 1;
        for (Vertex v : quad.vertices) {
            switch (facing.getAxis()) {
                case X -> {
                    minU = Math.max(minU, v.pos.y);
                    minV = Math.max(minV, v.pos.z);
                    maxU = Math.min(maxU, v.pos.y);
                    maxV = Math.min(maxV, v.pos.z);
                }
                case Y -> {
                    minU = Math.max(minU, v.pos.x);
                    minV = Math.max(minV, v.pos.z);
                    maxU = Math.min(maxU, v.pos.x);
                    maxV = Math.min(maxV, v.pos.z);
                }
                case Z -> {
                    minU = Math.max(minU, v.pos.x);
                    minV = Math.max(minV, v.pos.y);
                    maxU = Math.min(maxU, v.pos.x);
                    maxV = Math.min(maxV, v.pos.y);
                }
            }
            if (minU == maxU || minV == maxV) {
                fail = true;
            }
        }
        return fail;
    }

    private static class UV {

        final float[] uvs;

        private UV(float... uvs) {
            if (uvs == null || uvs.length != 4)
                throw new IllegalArgumentException();

            this.uvs = uvs;
        }

        private UV() {
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

    public static class Quad {

        static final EnumMap<EnumFacing, int[][]> INDEX_MAP = new EnumMap<>(EnumFacing.class);

        static {
            INDEX_MAP.put(EnumFacing.UP, new int[][] {
                    new int[] { 0, 4, 2 },
                    new int[] { 0, 4, 5 },
                    new int[] { 3, 4, 5 },
                    new int[] { 3, 4, 2 }
            });
            INDEX_MAP.put(EnumFacing.DOWN, new int[][] {
                    new int[] { 3, 1, 5 },
                    new int[] { 3, 1, 2 },
                    new int[] { 0, 1, 2 },
                    new int[] { 0, 1, 5 }
            });
            INDEX_MAP.put(EnumFacing.WEST, new int[][] {
                    new int[] { 3, 1, 2 },
                    new int[] { 3, 1, 5 },
                    new int[] { 3, 4, 5 },
                    new int[] { 3, 4, 2 }
            });
            INDEX_MAP.put(EnumFacing.EAST, new int[][] {
                    new int[] { 0, 4, 5 },
                    new int[] { 0, 4, 2 },
                    new int[] { 0, 1, 2 },
                    new int[] { 0, 1, 5 }
            });
            INDEX_MAP.put(EnumFacing.NORTH, new int[][] {
                    new int[] { 0, 1, 5 },
                    new int[] { 3, 1, 5 },
                    new int[] { 3, 4, 5 },
                    new int[] { 0, 4, 5 }
            });
            INDEX_MAP.put(EnumFacing.SOUTH, new int[][] {
                    new int[] { 0, 4, 2 },
                    new int[] { 3, 4, 2 },
                    new int[] { 3, 1, 2 },
                    new int[] { 0, 1, 2 }
            });
        }

        final Vertex[] vertices = new Vertex[] {
                new Vertex(0),
                new Vertex(1),
                new Vertex(2),
                new Vertex(3),
        };

        TextureAtlasSprite sprite;

        public static Quad fillQuad(float[] bounds, EnumFacing side, TextureAtlasSprite sprite) {
            var quad = new Quad();
            quad.sprite = sprite;
            for (var vertex : quad.vertices) {
                int[] info = INDEX_MAP.get(side)[vertex.index];
                vertex.pos.set(bounds[info[0]], bounds[info[1]], bounds[info[2]]);
                vertex.uvs.setUV(0, 0, 16, 16);
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
            var b = new StringBuilder(getClass().getSimpleName());
            b.append('[').append('\n').append('\t');
            for (Vertex vertex : vertices) {
                b.append(vertex.toString());
                if (vertex.index != 3)
                    b.append(", ").append("\n\t");
            }
            b.append("\n]");
            return b.toString();
        }
    }

    public static class Vertex {

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
            vData[i + 3] = colour;
            vData[i + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(uvs.getVertexU(index)));
            vData[i + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV(uvs.getVertexV(index)));
        }

        @Override
        public String toString() {
            return "Vertex{" +
                    "index=" + index +
                    ", pos=" + pos +
                    '}';
        }
    }
}
