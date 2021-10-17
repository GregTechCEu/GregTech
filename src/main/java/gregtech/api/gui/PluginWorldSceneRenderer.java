package gregtech.api.gui;

import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTLog;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.world.DummyWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.annotation.Nonnull;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glGetInteger;

@SideOnly(Side.CLIENT)
public class PluginWorldSceneRenderer {
    private static final FloatBuffer MODELVIEW_MATRIX_BUFFER = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final FloatBuffer PROJECTION_MATRIX_BUFFER = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer();
    private static final FloatBuffer PIXEL_DEPTH_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static final FloatBuffer OBJECT_POS_BUFFER = ByteBuffer.allocateDirect(12).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private static int width = 1080;
    private static int height = 1080;
    private static Framebuffer fbo = new Framebuffer(1080, 1080, true);

    public final TrackedDummyWorld world = new TrackedDummyWorld();
    public final List<BlockPos> renderedBlocks = new ArrayList<>();
    private Runnable beforeRender;
    private Runnable afterRender;
    private Predicate<BlockPos> renderFilter;
    private Consumer<BlockPos> onLookingAt;

    public PluginWorldSceneRenderer(Map<BlockPos, BlockInfo> renderedBlocks) {
        for (Map.Entry<BlockPos, BlockInfo> renderEntry : renderedBlocks.entrySet()) {
            BlockPos pos = renderEntry.getKey();
            BlockInfo blockInfo = renderEntry.getValue();
            if (blockInfo.getBlockState().getBlock() == Blocks.AIR)
                continue; //do not render air blocks
            this.renderedBlocks.add(pos);
            blockInfo.apply(world, pos);
        }
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    /***
     * This will modify the size of the FBO. You'd better know what you're doing before you call it.
     */
    public static void setFBOSize(int width, int height) {
        PluginWorldSceneRenderer.width = width;
        PluginWorldSceneRenderer.height = height;
        try {
            fbo.deleteFramebuffer();
            fbo = new Framebuffer(width, height, true);
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
    }

    public void setBeforeWorldRender(Runnable callback) {
        this.beforeRender = callback;
    }

    public void setAfterWorldRender(Runnable callback) {
        this.afterRender = callback;
    }

    public void setRenderFilter(Predicate<BlockPos> filter) {
        this.renderFilter = filter;
    }

    public void setOnLookingAt(Consumer<BlockPos> onLookingAt) {
        this.onLookingAt = onLookingAt;
    }

    /***
     * You'd better do unProject in {@link #setAfterWorldRender(Runnable)}
     * @param mouseX xPos in Texture
     * @param mouseY yPos in Texture
     * @return BlockPos Hit
     */
    public BlockPos screenPos2BlockPos(int mouseX, int mouseY) {
        // render a frame
        GlStateManager.enableDepth();
        int lastID = bindFBO();
        setupCamera(0, 0, PluginWorldSceneRenderer.width, PluginWorldSceneRenderer.height);

        drawWorld();
        BlockPos looking = unProject(mouseX, mouseY);

        resetCamera();
        unbindFBO(lastID);

        return looking;
    }

    /***
     * You'd better do project in {@link #setAfterWorldRender(Runnable)}
     * @param pos BlockPos
     * @param depth should pass Depth Test
     * @return x, y, z
     */
    public Vector3f blockPos2ScreenPos(BlockPos pos, boolean depth){
        // render a frame
        GlStateManager.enableDepth();
        int lastID = bindFBO();
        setupCamera(0, 0, PluginWorldSceneRenderer.width, PluginWorldSceneRenderer.height);

        drawWorld();
        Vector3f winPos = project(pos, depth);

        resetCamera();
        unbindFBO(lastID);

        return winPos;
    }

    public Vector3f getSceneSize() {
        return this.world.getSize();
    }

    public void render(float x, float y, float width, float height, int mouseX, int mouseY) {
        // bind to FBO
        int lastID = bindFBO();
        // setupCamera
        setupCamera(0, 0, PluginWorldSceneRenderer.width, PluginWorldSceneRenderer.height);

        // render TrackedDummyWorld
        drawWorld();

        // render lookingAt
        BlockPos looking = unProject(mouseX, mouseY);
        if (looking != null) {
            if(onLookingAt != null)
                onLookingAt.accept(looking);
        }

        // resetCamera
        resetCamera();
        // unbind FBO
        unbindFBO(lastID);

        // bind FBO as texture
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        lastID = glGetInteger(GL_TEXTURE_2D);
        GlStateManager.bindTexture(fbo.framebufferTexture);
        GlStateManager.color(1,1,1,1);

        // render rect with FBO texture
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(x + width, y + height, 0).tex(1, 0).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex(1, 1).endVertex();
        bufferbuilder.pos(x, y, 0).tex(0, 1).endVertex();
        bufferbuilder.pos(x, y + height, 0).tex(0, 0).endVertex();
        tessellator.draw();

        GlStateManager.bindTexture(lastID);
    }

    private void drawWorld() {
        if (beforeRender != null) {
            beforeRender.run();
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

        if (afterRender != null) {
            afterRender.run();
        }
    }

    private static int bindFBO(){
        int lastID = glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        fbo.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        fbo.framebufferClear();
        fbo.bindFramebuffer(true);
        GlStateManager.pushMatrix();
        return lastID;
    }

    private static void unbindFBO(int lastID){
        GlStateManager.popMatrix();
        fbo.unbindFramebufferTexture();
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
    }

    public Vector3f project(BlockPos pos, boolean depth) {
        //read current rendering parameters
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        //rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();

        //call gluProject with retrieved parameters
        GLU.gluProject(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

        //rewind buffers after read by gluProject
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();

        //rewind buffer after write by gluProject
        OBJECT_POS_BUFFER.rewind();

        //obtain position in Screen
        float winX = OBJECT_POS_BUFFER.get();
        float winY = OBJECT_POS_BUFFER.get();
        float winZ = OBJECT_POS_BUFFER.get();

        //rewind buffer after read
        OBJECT_POS_BUFFER.rewind();

        //check whether pass depth test
        if (!depth || Objects.equals(unProject((int) winX, (int) winY), pos)) {
            return new Vector3f(winX, winY, winZ);
        }

        return null;
    }

    public BlockPos unProject(int mouseX, int mouseY) {
        //read depth of pixel under mouse
        GL11.glReadPixels(mouseX, mouseY, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, PIXEL_DEPTH_BUFFER);

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
        GLU.gluUnProject(mouseX, mouseY, pixelDepth, MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

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

        //if we didn't hit anything, just return null. also return null if hit is too far from us
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

    public static void setupCamera(int x, int y, int width, int height) {
        GlStateManager.pushAttrib();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        Vec2f mousePosition = null;

        //setup viewport and clear GL buffers
        GlStateManager.viewport(x, y, width, height);

        RenderUtil.setGlClearColorFromInt(0, 0);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

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

        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        //reset attributes
        GlStateManager.popAttrib();
    }

    public class TrackedDummyWorld extends DummyWorld {
        private final Vector3f minPos = new Vector3f(2.14748365E9F, 2.14748365E9F, 2.14748365E9F);
        private final Vector3f maxPos = new Vector3f(-2.14748365E9F, -2.14748365E9F, -2.14748365E9F);

        public TrackedDummyWorld() {
        }

        public boolean setBlockState(@Nonnull BlockPos pos, IBlockState newState, int flags) {
            if (newState.getBlock() == Blocks.AIR) {
                PluginWorldSceneRenderer.this.renderedBlocks.remove(pos);
            } else {
                PluginWorldSceneRenderer.this.renderedBlocks.add(pos);
            }

            this.minPos.setX(Math.min(this.minPos.getX(), (float)pos.getX()));
            this.minPos.setY(Math.min(this.minPos.getY(), (float)pos.getY()));
            this.minPos.setZ(Math.min(this.minPos.getZ(), (float)pos.getZ()));
            this.maxPos.setX(Math.max(this.maxPos.getX(), (float)pos.getX()));
            this.maxPos.setY(Math.max(this.maxPos.getY(), (float)pos.getY()));
            this.maxPos.setZ(Math.max(this.maxPos.getZ(), (float)pos.getZ()));
            return super.setBlockState(pos, newState, flags);
        }

        public @Nonnull IBlockState getBlockState(@Nonnull BlockPos pos) {
            return PluginWorldSceneRenderer.this.renderFilter != null && !PluginWorldSceneRenderer.this.renderFilter.test(pos) ? Blocks.AIR.getDefaultState() : super.getBlockState(pos);
        }

        public Vector3f getSize() {
            Vector3f result = new Vector3f();
            result.setX(this.maxPos.getX() - this.minPos.getX() + 1.0F);
            result.setY(this.maxPos.getY() - this.minPos.getY() + 1.0F);
            result.setZ(this.maxPos.getZ() - this.minPos.getZ() + 1.0F);
            return result;
        }

        public Vector3f getMinPos() {
            return this.minPos;
        }

        public Vector3f getMaxPos() {
            return this.maxPos;
        }
    }
}
