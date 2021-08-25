package gregtech.api.render.scene;

import codechicken.lib.vec.Vector3;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.util.Position;
import gregtech.api.util.PositionedRect;
import gregtech.api.util.Size;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/23
 * @Description: Abstract class, and extend a lot of features compared with the original one.
 */
public abstract class WorldSceneRenderer {
    protected static final FloatBuffer MODELVIEW_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer PROJECTION_MATRIX_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final IntBuffer VIEWPORT_BUFFER = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    protected static final FloatBuffer PIXEL_DEPTH_BUFFER = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected static final FloatBuffer OBJECT_POS_BUFFER = ByteBuffer.allocateDirect(3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    public final World world;
    public final Map<Collection<BlockPos>, ISceneRenderHook> renderedBlocksMap;
    private Runnable beforeRender;
    private Runnable afterRender;
    private Consumer<RayTraceResult> onLookingAt;
    private int clearColor;
    private RayTraceResult lastTraceResult;
    private Vector3f eyePos = new Vector3f(0, 0, 10f);
    private Vector3f lookAt = new Vector3f(0, 0, 0);
    private Vector3f worldUp = new Vector3f(0, 1, 0);

    public WorldSceneRenderer(World world) {
        this.world = world;
        renderedBlocksMap = new LinkedHashMap<>();
    }

    public WorldSceneRenderer setBeforeWorldRender(Runnable callback) {
        this.beforeRender = callback;
        return this;
    }

    public WorldSceneRenderer setAfterWorldRender(Runnable callback) {
        this.afterRender = callback;
        return this;
    }

    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks, ISceneRenderHook renderHook) {
        if (blocks != null) {
            this.renderedBlocksMap.put(blocks, renderHook);
        }
        return this;
    }

    public WorldSceneRenderer setOnLookingAt(Consumer<RayTraceResult> onLookingAt) {
        this.onLookingAt = onLookingAt;
        return this;
    }

    public void setClearColor(int clearColor) {
        this.clearColor = clearColor;
    }

    public RayTraceResult getLastTraceResult() {
        return lastTraceResult;
    }

    public void render(float x, float y, float width, float height, int mouseX, int mouseY) {
        // setupCamera
        PositionedRect positionedRect = getPositionedRect((int)x, (int)y, (int)width, (int)height);
        setupCamera(positionedRect);
        // render TrackedDummyWorld
        drawWorld();
        // check lookingAt
        Vector3f lastHitPos = null;
        if (mouseX > positionedRect.position.x && mouseX < positionedRect.position.x + positionedRect.size.width
                && mouseY > positionedRect.position.y && mouseY < positionedRect.position.y + positionedRect.size.height) {
            lastHitPos = unProject(mouseX, mouseY);
        }
        this.lastTraceResult = null;
        if (lastHitPos != null) {
            RayTraceResult result = rayTrace(lastHitPos);
            if (result != null) {
                this.lastTraceResult = result;
                if(onLookingAt != null) {
                    onLookingAt.accept(result);
                }
            }
        }
        // resetCamera
        resetCamera();
    }

    public Vector3f getEyePos() {
        return eyePos;
    }

    public Vector3f getLookAt() {
        return lookAt;
    }

    public Vector3f getWorldUp() {
        return worldUp;
    }

    public void setCameraLookAt(Vector3f eyePos, Vector3f lookAt, Vector3f worldUp) {
        this.eyePos = eyePos;
        this.lookAt = lookAt;
        this.worldUp = worldUp;
    }

    public void setCameraLookAt(Vector3f lookAt, double radius, double rotationPitch, double rotationYaw) {
        this.lookAt = lookAt;
        Vector3 vecX = new Vector3(Math.cos(rotationPitch), 0, Math.sin(rotationPitch));
        Vector3 vecY = new Vector3(0, Math.tan(rotationYaw) * vecX.mag(),0);
        Vector3 pos = vecX.copy().add(vecY).normalize().multiply(radius);
        this.eyePos = pos.add(lookAt.x, lookAt.y, lookAt.z).vector3f();
    }

    protected PositionedRect getPositionedRect(int x, int y, int width, int height) {
        return new PositionedRect(new Position(x, y), new Size(width, height));
    }

    protected void setupCamera(PositionedRect positionedRect) {
        int x = positionedRect.getPosition().x;
        int y = positionedRect.getPosition().y;
        int width = positionedRect.getSize().width;
        int height = positionedRect.getSize().height;

        GlStateManager.pushAttrib();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        //setup viewport and clear GL buffers
        GlStateManager.viewport(x, y, width, height);

        clearView(x, y, width, height);

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
        GLU.gluLookAt(eyePos.x, eyePos.y, eyePos.z, lookAt.x, lookAt.y, lookAt.z, worldUp.x, worldUp.y, worldUp.z);
    }

    protected void clearView(int x, int y, int width, int height) {
        RenderUtil.setGlClearColorFromInt(clearColor, clearColor >> 24);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    protected void resetCamera() {
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

    protected void drawWorld() {
        if (beforeRender != null) {
            beforeRender.run();
        }

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        com.enderio.core.client.render.RenderUtil.bindBlockTexture();
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();

        try { // render block in each layer
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                ForgeHooksClient.setRenderLayer(layer);
                int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;

                renderedBlocksMap.forEach((renderedBlocks, hook)->{
                    if (hook != null) {
                        if (!hook.apply(false, pass, layer)) {
                            return;
                        }
                    } else {
                        setDefaultPassRenderState(pass);
                    }

                    BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                    BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();

                    for (BlockPos pos : renderedBlocks) {
                        IBlockState state = world.getBlockState(pos);
                        Block block = state.getBlock();
                        if (block == Blocks.AIR) continue;
                        state = state.getActualState(world, pos);
                        if (block.canRenderInLayer(state, layer)) {
                            blockrendererdispatcher.renderBlock(state, pos, world, buffer);
                        }
                    }

                    Tessellator.getInstance().draw();
                    Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
                });
            }
        } finally {
            ForgeHooksClient.setRenderLayer(oldRenderLayer);
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();

        // render TESR
        for (int pass = 0; pass < 2; pass++) {
            ForgeHooksClient.setRenderPass(pass);
            int finalPass = pass;
            renderedBlocksMap.forEach((renderedBlocks, hook)->{
                if (hook != null) {
                    if (!hook.apply(true, finalPass, null)) {
                        return;
                    }
                } else {
                    setDefaultPassRenderState(finalPass);
                }
                for (BlockPos pos : renderedBlocks) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile != null) {
                        if (tile.shouldRenderInPass(finalPass)) {
                            TileEntityRendererDispatcher.instance.render(tile, pos.getX(), pos.getY(), pos.getZ(), 0);
                        }
                    }
                }
            });
        }
        ForgeHooksClient.setRenderPass(-1);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        if (afterRender != null) {
            afterRender.run();
        }
    }

    public static void setDefaultPassRenderState(int pass) {
        GlStateManager.color(1, 1, 1, 1);
        if (pass == 0) { // SOLID
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        } else { // TRANSLUCENT
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
        }
    }

    public RayTraceResult rayTrace(Vector3f pos) {
        Vec3d vec3d = new Vec3d(eyePos.x, eyePos.y, eyePos.z);
        Vec3d vec3d1 = new Vec3d(pos.getX() - eyePos.x, pos.getY() - eyePos.y, pos.getZ() - eyePos.z);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * 1.5, vec3d1.y * 1.5, vec3d1.z * 1.5);
        return this.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
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

    public Vector3f unProject(int mouseX, int mouseY) {
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

        return new Vector3f(posX, posY, posZ);
    }

    /***
     * For better performance, You'd better handle the event {@link #setOnLookingAt(Consumer)} or {@link #getLastTraceResult()}
     * @param mouseX xPos in Texture
     * @param mouseY yPos in Texture
     * @return BlockPosFace Hit
     */
    protected BlockPosFace screenPos2BlockPosFace(int mouseX, int mouseY, int x, int y, int width, int height) {
        // render a frame
        GlStateManager.enableDepth();
        setupCamera(getPositionedRect(x, y, width, height));

        drawWorld();

        Vector3f looking = unProject(mouseX, mouseY);
        float posX = looking.x;
        float posY = looking.y;
        float posZ = looking.z;
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
        EnumFacing.Axis axis = EnumFacing.Axis.X;
        double min = Math.abs(Math.round(posX) - posX);
        double tmp = Math.abs(Math.round(posY) - posY);
        if (min > tmp) {
            min = tmp;
            axis = EnumFacing.Axis.Y;
        }
        tmp = Math.abs(Math.round(posZ) - posZ);
        if (min > tmp) {
            axis = EnumFacing.Axis.Z;
        }
        EnumFacing facing = EnumFacing.UP;
        if (axis == EnumFacing.Axis.Y && (posY - pos.getY()) < 0.5) {
            facing = EnumFacing.DOWN;
        } else if (axis == EnumFacing.Axis.X) {
            if ((posX - pos.getX()) < 0.5)
                facing = EnumFacing.WEST;
            else
                facing = EnumFacing.EAST;
        } else if (axis == EnumFacing.Axis.Z) {
            if ((posZ - pos.getZ()) < 0.5)
                facing = EnumFacing.NORTH;
            else
                facing = EnumFacing.SOUTH;
        }

        resetCamera();

        return new BlockPosFace(pos, facing);
    }

    /***
     * For better performance, You'd better do project in {@link #setAfterWorldRender(Runnable)}
     * @param pos BlockPos
     * @param depth should pass Depth Test
     * @return x, y, z
     */
    protected Vector3f blockPos2ScreenPos(BlockPos pos, boolean depth, int x, int y, int width, int height){
        // render a frame
        GlStateManager.enableDepth();
        setupCamera(getPositionedRect(x, y, width, height));

        drawWorld();
        Vector3f winPos = project(pos, depth);

        resetCamera();

        return winPos;
    }

    public static class BlockPosFace extends BlockPos {
        public final EnumFacing facing;

        public BlockPosFace(BlockPos pos, EnumFacing facing) {
            super(pos);
            this.facing = facing;
        }

    }
}
