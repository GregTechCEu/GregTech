package gregtech.api.render.scene;

import codechicken.lib.render.BlockRenderer.BlockFace;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.world.DummyWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class WorldSceneRenderer {

    private static final FloatBuffer MODELVIEW_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final FloatBuffer PROJECTION_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    private static final FloatBuffer PIXEL_DEPTH_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final FloatBuffer OBJECT_POS_BUFFER = ByteBuffer.allocateDirect(3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    public final TrackedDummyWorld world = new TrackedDummyWorld();
    private final List<BlockPos> renderedBlocks = new ArrayList<>();
    private SceneRenderCallback renderCallback;
    private Predicate<BlockPos> renderFilter;
    private BlockPos lastHitBlock;

    public WorldSceneRenderer(Map<BlockPos, BlockInfo> renderedBlocks) {
        for (Entry<BlockPos, BlockInfo> renderEntry : renderedBlocks.entrySet()) {
            BlockPos pos = renderEntry.getKey();
            BlockInfo blockInfo = renderEntry.getValue();
            if (blockInfo.getBlockState().getBlock() == Blocks.AIR)
                continue; //do not render air blocks
            this.renderedBlocks.add(pos);
            blockInfo.apply(world, pos);
        }
    }

    public void setRenderCallback(SceneRenderCallback callback) {
        this.renderCallback = callback;
    }

    public void setRenderFilter(Predicate<BlockPos> filter) {
        this.renderFilter = filter;
    }

    public Vector3f getSize() {
        return world.getSize();
    }

    public BlockPos getLastHitBlock() {
        return lastHitBlock;
    }

    /**
     * Renders scene on given coordinates with given width and height, and RGB background color
     * Note that this will ignore any transformations applied currently to projection/view matrix,
     * so specified coordinates are scaled MC gui coordinates.
     * It will return matrices of projection and view in previous state after rendering
     */
    public void render(int x, int y, int width, int height, int backgroundColor) {
        Vec2f mousePosition = setupCamera(x, y, width, height, backgroundColor);
        if (renderCallback != null) {
            renderCallback.preRenderScene(this);
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRendererDispatcher dispatcher = minecraft.getBlockRendererDispatcher();
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        for (BlockPos pos : renderedBlocks) {
            if (renderFilter != null && !renderFilter.test(pos))
                continue; //do not render if position is skipped
            IBlockState blockState = world.getBlockState(pos);
            for(BlockRenderLayer renderLayer : BlockRenderLayer.values()) {
                if (!blockState.getBlock().canRenderInLayer(blockState, renderLayer)) continue;
                ForgeHooksClient.setRenderLayer(renderLayer);
                dispatcher.renderBlock(blockState, pos, world, bufferBuilder);
            }
        }
        tessellator.draw();
        ForgeHooksClient.setRenderLayer(oldRenderLayer);

        if (mousePosition != null) {
            this.lastHitBlock = handleMouseHit(mousePosition);
        } else {
            this.lastHitBlock = null;
        }

        if (lastHitBlock != null) {
            GlStateManager.disableTexture2D();
            CCRenderState renderState = CCRenderState.instance();
            renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR, tessellator.getBuffer());
            ColourMultiplier multiplier = new ColourMultiplier(0);
            renderState.setPipeline(new Translation(lastHitBlock), multiplier);
            BlockFace blockFace = new BlockFace();
            renderState.setModel(blockFace);
            for (EnumFacing renderSide : EnumFacing.VALUES) {
                float diffuse = LightUtil.diffuseLight(renderSide);
                int color = (int) (255 * diffuse);
                multiplier.colour = RenderUtil.packColor(color, color, color, 100);
                blockFace.loadCuboidFace(Cuboid6.full, renderSide.getIndex());
                renderState.render();
            }
            renderState.draw();
            GlStateManager.enableTexture2D();
        }

        resetCamera();
    }

    private BlockPos handleMouseHit(Vec2f mousePosition) {
        //read depth of pixel under mouse
        GL11.glReadPixels((int) mousePosition.x, (int) mousePosition.y, 1, 1,
            GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, PIXEL_DEPTH_BUFFER);

        //rewind buffer after write by glReadPixels
        PIXEL_DEPTH_BUFFER.rewind();

        //retrieve depth from buffer (0.0-1.0f)
        float pixelDepth = PIXEL_DEPTH_BUFFER.get();

        //rewind buffer after read
        PIXEL_DEPTH_BUFFER.rewind();

        //read current rendering parameters
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        //rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();

        //call gluUnProject with retrieved parameters
        GLU.gluUnProject(mousePosition.x, mousePosition.y, pixelDepth,
            MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

        //rewind buffers after read by gluUnProject
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();

        //rewind buffer after write by gluUnProject
        OBJECT_POS_BUFFER.rewind();

        //obtain absolute position in world
        float posX = OBJECT_POS_BUFFER.get();
        float posY = OBJECT_POS_BUFFER.get();
        float posZ = OBJECT_POS_BUFFER.get();

        //rewind buffer after read
        OBJECT_POS_BUFFER.rewind();

        //if we didn't hit anything, just return null
        //also return null if hit is too far from us
        if (posY < -100.0f) {
            return null; //stop execution at that point
        }

        BlockPos pos = new BlockPos(posX, posY, posZ);
        if (world.isAirBlock(pos)) {
            //if block is air, then search for nearest adjacent block
            //this can happen under extreme rotation angles
            for (EnumFacing offset : EnumFacing.VALUES) {
                BlockPos relative = pos.offset(offset);
                if (world.isAirBlock(relative)) continue;
                pos = relative;
                break;
            }
        }
        if (world.isAirBlock(pos)) {
            //if we didn't found any other block, return null
            return null;
        }
        return pos;
    }

    public static Vec2f setupCamera(int x, int y, int width, int height, int skyColor) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);

        GL11.glPushAttrib(GL11.GL_TRANSFORM_BIT);
        mc.entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        //compute window size from scaled width & height
        int windowWidth = (int) (width / (resolution.getScaledWidth() * 1.0) * mc.displayWidth);
        int windowHeight = (int) (height / (resolution.getScaledHeight() * 1.0) * mc.displayHeight);

        //translate gui coordinates to window's ones (y is inverted)
        int windowX = (int) (x / (resolution.getScaledWidth() * 1.0) * mc.displayWidth);
        int windowY = mc.displayHeight - (int) (y / (resolution.getScaledHeight() * 1.0) * mc.displayHeight) - windowHeight;

        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        Vec2f mousePosition = null;
        //compute mouse position only if inside viewport
        if (mouseX >= windowX && mouseY >= windowY && mouseX - windowX < windowWidth && mouseY - windowY < windowHeight) {
            mousePosition = new Vec2f(mouseX, mouseY);
        }

        //setup viewport and clear GL buffers
        GlStateManager.viewport(windowX, windowY, windowWidth, windowHeight);

        if (skyColor >= 0) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(windowX, windowY, windowWidth, windowHeight);
            RenderUtil.setGlClearColorFromInt(skyColor, 255);
            GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        //setup projection matrix to perspective
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        float aspectRatio = width / (height * 1.0f);
        GLU.gluPerspective(60.0f, aspectRatio, 0.1f, 10000.0f);

        //setup modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GLU.gluLookAt(0.0f, 0.0f, -10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        return mousePosition;
    }

    public static void resetCamera() {
        //reset viewport
        Minecraft minecraft = Minecraft.getMinecraft();
        GlStateManager.viewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);

        //reset projection matrix
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();

        //reset modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        //Re-enable disabled states
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();

        //Reset Attributes
        GL11.glPopAttrib();
    }

    public class TrackedDummyWorld extends DummyWorld {

        private final Vector3f minPos = new Vector3f(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        private final Vector3f maxPos = new Vector3f(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

        @Override
        public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
            if (newState.getBlock() == Blocks.AIR) {
                renderedBlocks.remove(pos);
            } else {
                renderedBlocks.add(pos);
            }
            minPos.setX(Math.min(minPos.getX(), pos.getX()));
            minPos.setY(Math.min(minPos.getY(), pos.getY()));
            minPos.setZ(Math.min(minPos.getZ(), pos.getZ()));
            maxPos.setX(Math.max(maxPos.getX(), pos.getX()));
            maxPos.setY(Math.max(maxPos.getY(), pos.getY()));
            maxPos.setZ(Math.max(maxPos.getZ(), pos.getZ()));
            return super.setBlockState(pos, newState, flags);
        }

        @Override
        public IBlockState getBlockState(BlockPos pos) {
            if (renderFilter != null && !renderFilter.test(pos))
                return Blocks.AIR.getDefaultState(); //return air if not rendering this block
            return super.getBlockState(pos);
        }

        public Vector3f getSize() {
            Vector3f result = new Vector3f();
            result.setX(maxPos.getX() - minPos.getX() + 1);
            result.setY(maxPos.getY() - minPos.getY() + 1);
            result.setZ(maxPos.getZ() - minPos.getZ() + 1);
            return result;
        }

        public Vector3f getMinPos() {
            return minPos;
        }

        public Vector3f getMaxPos() {
            return maxPos;
        }
    }

}
