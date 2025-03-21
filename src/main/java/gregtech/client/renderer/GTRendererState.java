package gregtech.client.renderer;

import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.client.MinecraftForgeClient;

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
                size.z
        );
    }

    public GTRendererState setBounds(float w, float h, float l) {
        return setBounds(
                0,
                0,
                0,
                w,
                h,
                l
        );
    }

    public GTRendererState setBounds(AxisAlignedBB bounds) {
        return setBounds(
                (float) bounds.minX,
                (float) bounds.minY,
                (float) bounds.minZ,
                (float) bounds.maxX,
                (float) bounds.maxY,
                (float) bounds.maxZ
        );
    }

    private GTRendererState setBounds(float... bounds) {
        this.bounds = bounds;
        return this;
    }

    public GTRendererState quad(EnumFacing side) {
        BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
        if (renderLayer == null || !this.state.getBlock().canRenderInLayer(this.state, renderLayer)) return this;
        if (!this.state.shouldSideBeRendered(world, pos, side)) return this;

        Quad quad = Quad.fillQuad(this.bounds, EnumFaceDirection.getFacing(side), sprite);
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

    public static class Quad {
        final Vertex[] vertices = new Vertex[] {
                new Vertex(0),
                new Vertex(1),
                new Vertex(2),
                new Vertex(3),
        };
        TextureAtlasSprite sprite;

        public static Quad fillQuad(float[] bounds, EnumFaceDirection direction, TextureAtlasSprite sprite) {
            var quad = new Quad();
            quad.sprite = sprite;
            for (Vertex vertex : quad.vertices) {
                var info = direction.getVertexInformation(vertex.index);
                float x = bounds[info.xIndex];
                float y = bounds[info.yIndex];
                float z = bounds[info.zIndex];
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

            vData[i    ] = Float.floatToRawIntBits(pos.x);
            vData[i + 1] = Float.floatToRawIntBits(pos.y);
            vData[i + 2] = Float.floatToRawIntBits(pos.z);
            vData[i + 3] = colour;
            vData[i + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(uvs.getVertexU(index)));
            vData[i + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV(uvs.getVertexV(index)));
        }
    }
}
