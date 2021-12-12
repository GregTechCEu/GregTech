package gregtech.client.renderer.scene;

import gregtech.api.util.GTLog;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/23
 * @Description: It looks similar to {@link ImmediateWorldSceneRenderer}, but totally different.
 * It uses FBO and is more universality and efficient(X).
 * FBO can be rendered anywhere more flexibly, not just in the GUI.
 * If you have scene rendering needs, you will love this FBO renderer.
 * TODO OP_LIST might be used in the future to further improve performance.
 */
@SideOnly(Side.CLIENT)
public class FBOWorldSceneRenderer extends WorldSceneRenderer {
    private int resolutionWidth = 1080;
    private int resolutionHeight = 1080;
    private Framebuffer fbo;

    public FBOWorldSceneRenderer(World world, int resolutionWidth, int resolutionHeight) {
        super(world);
        setFBOSize(resolutionWidth, resolutionHeight);
    }

    public FBOWorldSceneRenderer(World world, Framebuffer fbo) {
        super(world);
        this.fbo = fbo;
    }

    public int getResolutionWidth() {
        return resolutionWidth;
    }

    public int getResolutionHeight() {
        return resolutionHeight;
    }

    /***
     * This will modify the size of the FBO. You'd better know what you're doing before you call it.
     */
    public void setFBOSize(int resolutionWidth, int resolutionHeight) {
        this.resolutionWidth = resolutionWidth;
        this.resolutionHeight = resolutionHeight;
        releaseFBO();
        try {
            fbo = new Framebuffer(resolutionWidth, resolutionHeight, true);
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
    }

    public RayTraceResult screenPos2BlockPosFace(int mouseX, int mouseY) {
        int lastID = bindFBO();
        RayTraceResult looking = super.screenPos2BlockPosFace(mouseX, mouseY, 0, 0, this.resolutionWidth, this.resolutionHeight);
        unbindFBO(lastID);
        return looking;
    }

    public Vector3f blockPos2ScreenPos(BlockPos pos, boolean depth){
        int lastID = bindFBO();
        Vector3f winPos = super.blockPos2ScreenPos(pos, depth, 0, 0, this.resolutionWidth, this.resolutionHeight);
        unbindFBO(lastID);
        return winPos;
    }

    public void render(float x, float y, float width, float height, float mouseX, float mouseY) {
        // bind to FBO
        int lastID = bindFBO();
        super.render(0, 0, this.resolutionWidth, this.resolutionHeight, (int) (this.resolutionWidth * mouseX / width), (int) (this.resolutionHeight * (1 - mouseY / height)));
        // unbind FBO
        unbindFBO(lastID);

        // bind FBO as texture
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        lastID = GL11.glGetInteger(GL11.GL_TEXTURE_2D);
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

    public void render(float x, float y, float width, float height, int mouseX, int mouseY) {
        render(x, y, width, height, (float) mouseX, (float) mouseY);
    }

    private int bindFBO(){
        int lastID = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
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

    public void releaseFBO() {
        if (fbo != null) {
            fbo.deleteFramebuffer();
        }
        fbo = null;
    }
}
