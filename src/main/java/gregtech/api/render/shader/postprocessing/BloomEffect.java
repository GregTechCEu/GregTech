package gregtech.api.render.shader.postprocessing;

import codechicken.lib.render.shader.ShaderProgram;
import gregtech.api.render.MetaTileEntityRenderer;
import gregtech.api.render.shader.Shaders;
import gregtech.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/10/02
 * @Description:
 */
@SideOnly(Side.CLIENT)
public class BloomEffect {
    private static final Framebuffer BUFFER_PRE;
    private static final Framebuffer BUFFER_A;
    private static final Framebuffer BUFFER_B;
    private static final Framebuffer BUFFER_C;
    private static final List<IPostRender> PIPELINE = new LinkedList<>();
    static {
        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();
        int lastWidth = fbo.framebufferWidth;
        int lastHeight = fbo.framebufferHeight;
        BUFFER_PRE = new Framebuffer(lastWidth, lastHeight, false);
        BUFFER_A = new Framebuffer(lastWidth, lastHeight, false);
        BUFFER_B = new Framebuffer(lastWidth, lastHeight, false);
        BUFFER_C = new Framebuffer(lastWidth, lastHeight, false);
        BUFFER_PRE.setFramebufferColor(0,0,0,0);
        BUFFER_A.setFramebufferColor(0,0,0,0);
        BUFFER_B.setFramebufferColor(0,0,0,0);
        BUFFER_C.setFramebufferColor(0,0,0,0);
        RenderUtil.hookDepthBuffer(BUFFER_PRE, fbo);
    }

    public static void renderBloom(IPostRender renderer) {
        if (renderer != null) {
            PIPELINE.add(renderer);
        }
    }

    private static void drawBloomScene(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) return;
        final double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        final double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        final double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();

        GlStateManager.translate(-posX, -posY, -posZ);

        for (IPostRender renderer : PIPELINE) {
            renderer.render(posX, posY, posZ, partialTicks);
        }
        PIPELINE.clear();

        MetaTileEntityRenderer.renderBloom();

        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
    }

    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();
        if (BUFFER_PRE.framebufferWidth != fbo.framebufferWidth || BUFFER_PRE.framebufferHeight != fbo.framebufferHeight) {
            int lastWidth = fbo.framebufferWidth;
            int lastHeight = fbo.framebufferHeight;
            BUFFER_PRE.createBindFramebuffer(lastWidth, lastHeight);
            BUFFER_A.createBindFramebuffer(lastWidth, lastHeight);
            BUFFER_B.createBindFramebuffer(lastWidth, lastHeight);
            BUFFER_C.createBindFramebuffer(lastWidth, lastHeight);
            RenderUtil.hookDepthBuffer(BUFFER_PRE, fbo);
        }
        BUFFER_PRE.framebufferClear();
        BUFFER_A.framebufferClear();
        BUFFER_B.framebufferClear();
        BUFFER_C.framebufferClear();
        BUFFER_PRE.bindFramebuffer(true);
        drawBloomScene(event.getPartialTicks());

        ShaderProgram

        BUFFER_PRE.bindFramebufferTexture();
        Shaders.renderFullImageInFBO(BUFFER_A, Shaders.BLOOM_BUFFER_A, null).bindFramebufferTexture();
        Shaders.renderFullImageInFBO(BUFFER_B, Shaders.BLOOM_BUFFER_B, null);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.enableTexture2D();
        BUFFER_A.bindFramebufferTexture();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.enableTexture2D();
        BUFFER_B.bindFramebufferTexture();


        int lastOP = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        Shaders.renderFullImageInFBO(BUFFER_C, Shaders.BLOOM_BUFFER_C, uniformCache -> {
            uniformCache.glUniform1I("buffer_a", 0);
            uniformCache.glUniform1I("buffer_b", 1);
        });

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, lastOP);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.bindTexture(0);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);

        GlStateManager.depthMask(false);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        BUFFER_C.bindFramebufferTexture();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();

        BUFFER_PRE.bindFramebufferTexture();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        GlStateManager.depthMask(true);
    }

}
