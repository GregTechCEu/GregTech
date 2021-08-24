package gregtech.api.render.scene;

import gregtech.api.util.GTLog;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.EXTFramebufferObject;

import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glGetInteger;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/23/18:52
 * @Description: It looks similar to {@link GuiWorldSceneRenderer}, but totally different.
 * It uses FBO and is more universality and efficient.
 * FBO can be rendered anywhere more flexibly, not just in the GUI.
 * If you have scene rendering needs, you will love this FBO renderer.
 */
@SideOnly(Side.CLIENT)
public class FBOWorldSceneRenderer extends WorldSceneRenderer {
    private int width = 1080;
    private int height = 1080;
    private Framebuffer fbo = new Framebuffer(1080, 1080, true);

    public FBOWorldSceneRenderer(int resolutionWidth, int resolutionHeight) {
        setFBOSize(resolutionWidth, resolutionHeight);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /***
     * This will modify the size of the FBO. You'd better know what you're doing before you call it.
     */
    public void setFBOSize(int width, int height) {
        this.width = width;
        this.height = height;
        try {
            if (fbo != null) {
                fbo.deleteFramebuffer();
            }
            fbo = new Framebuffer(width, height, true);
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
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
        setupCamera(0, 0, width, height);

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
        setupCamera(0, 0, this.width, this.height);

        drawWorld();
        Vector3f winPos = project(pos, depth);

        resetCamera();
        unbindFBO(lastID);

        return winPos;
    }

    public void render(float x, float y, float width, float height, int mouseX, int mouseY) {
        mouseX = (int) (this.width * mouseX / width);
        mouseY = (int) (this.height * (1 - mouseY / height));
        // bind to FBO
        int lastID = bindFBO();
        super.render(x, y, width, height, mouseX, mouseY);
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

    @Override
    protected void setupCamera(int x, int y, int width, int height) {
        super.setupCamera(0, 0, this.width, this.height);
    }

    private int bindFBO(){
        int lastID = glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        fbo.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        fbo.framebufferClear();
        fbo.bindFramebuffer(true);
        GlStateManager.pushMatrix();
        return lastID;
    }

    private void unbindFBO(int lastID){
        GlStateManager.popMatrix();
        fbo.unbindFramebufferTexture();
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
    }
}
