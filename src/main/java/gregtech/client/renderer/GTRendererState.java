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

import static gregtech.client.renderer.GTRendererState.Axis.*;

public class GTRendererState {

    private static final ThreadLocal<GTRendererState> renderState = ThreadLocal.withInitial(GTRendererState::new);

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
        this.bounds = bounds;
        return this;
    }

    public GTRendererState quad(EnumFacing side) {
        BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
        if (renderLayer == null || !this.state.getBlock().canRenderInLayer(this.state, renderLayer)) return this;
        if (!this.state.shouldSideBeRendered(world, pos, side)) return this;

        Quad quad = Quad.fillQuad(this.bounds, side, sprite);
        // certain quads are rendering the wrong way
        // S, W, and U quads are correct
        // N, E, and D quads are not
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

    enum Axis {

        x,
        y,
        z;

        public final int min;
        public final int max;

        Axis() {
            this.min = ordinal();
            this.max = ordinal() + 3;
        }
    }

    public static class Quad {

        static final EnumMap<EnumFacing, int[][]> INDEX_MAP = new EnumMap<>(EnumFacing.class);

        static {
            fillMap();
        }

        static void fillMap() {
            /*
             * UP pos y
             * all max y [4]
             * max x, max z [3, 5]
             * max x, min z [3, 2]
             * min x, min z [0, 2]
             * min x, max z [0, 5]
             */
            INDEX_MAP.put(EnumFacing.UP, new int[][] {
                    new int[] { x.max, y.max, z.max },
                    new int[] { x.max, y.max, z.min },
                    new int[] { x.min, y.max, z.min },
                    new int[] { x.min, y.max, z.max }
            });
            /*
             * DOWN neg y
             * all min y [1]
             * min x, min z [0, 2]
             * min x, max z [0, 5]
             * max x, max z [3, 5]
             * max x, min z [3, 2]
             */
            INDEX_MAP.put(EnumFacing.DOWN, new int[][] {
                    new int[] { x.max, y.min, z.max },
                    new int[] { x.max, y.min, z.min },
                    new int[] { x.min, y.min, z.min },
                    new int[] { x.min, y.min, z.max }
            });
            /*
             * WEST neg x
             * all min x [0]
             * max z, min y [5, 1]
             * max z, max y [5, 4]
             * min z, max y [2, 4]
             * min z, min y [2, 1]
             */
            INDEX_MAP.put(EnumFacing.WEST, new int[][] {
                    new int[] { x.min, y.min, z.max },
                    new int[] { x.min, y.max, z.max },
                    new int[] { x.min, y.max, z.min },
                    new int[] { x.min, y.min, z.min }
            });
            /*
             * EAST pos x
             * all max x [3]
             * min z, max y [2, 4]
             * min z, min y [2, 1]
             * max z, min y [5, 1]
             * max z, max y [5, 4]
             */
            INDEX_MAP.put(EnumFacing.EAST, new int[][] {
                    new int[] { x.max, y.max, z.min },
                    new int[] { x.max, y.min, z.min },
                    new int[] { x.max, y.min, z.max },
                    new int[] { x.max, y.max, z.max }
            });
            /*
             * NORTH neg z
             * all min z [2]
             * max y, min x [4, 0]
             * min y, min x [1, 0]
             * min y, max x [1, 3]
             * max y, max x [4, 3]
             */
            INDEX_MAP.put(EnumFacing.NORTH, new int[][] {
                    new int[] { x.min, y.max, z.min },
                    new int[] { x.min, y.min, z.min },
                    new int[] { x.max, y.min, z.min },
                    new int[] { x.max, y.max, z.min }
            });
            /*
             * SOUTH pos z
             * all max z [5]
             * min y, max x [1, 3]
             * max y, max x [4, 3]
             * max y, min x [4, 0]
             * min y, min x [1, 0]
             */
            INDEX_MAP.put(EnumFacing.SOUTH, new int[][] {
                    new int[] { x.max, y.min, z.max },
                    new int[] { x.max, y.max, z.max },
                    new int[] { x.min, y.max, z.max },
                    new int[] { x.min, y.min, z.max }
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
            fillMap();
            quad.sprite = sprite;
            for (var vertex : quad.vertices) {
                int[] info = INDEX_MAP.get(side)[vertex.index];
                float x = bounds[info[0]];
                float y = bounds[info[1]];
                float z = bounds[info[2]];
                vertex.pos.set(x, y, z);
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
