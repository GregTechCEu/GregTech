package gregtech.api.render.shader.postprocessing;

import gregtech.api.render.shader.PingPongBuffer;
import gregtech.api.render.shader.Shaders;
import gregtech.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlurEffect {
    private static final Framebuffer BLUR_H;
    private static final Framebuffer BLUR_W;
    private static final Framebuffer BLUR_H2;
    private static final Framebuffer BLUR_W2;

    static {
        BLUR_H = new Framebuffer(10, 10, false);
        BLUR_H2 = new Framebuffer(10, 10, false);
        BLUR_W = new Framebuffer(10, 10, false);
        BLUR_W2 = new Framebuffer(10, 10, false);
        BLUR_H.setFramebufferColor(0, 0, 0, 0);
        BLUR_H2.setFramebufferColor(0, 0, 0, 0);
        BLUR_W.setFramebufferColor(0, 0, 0, 0);
        BLUR_W2.setFramebufferColor(0, 0, 0, 0);
    }

    private static void cleanUP() {
        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();
        int lastWidth = fbo.framebufferWidth;
        int lastHeight = fbo.framebufferHeight;
        RenderUtil.updateFBOSize(BLUR_H, lastWidth / 8, lastHeight / 8);
        RenderUtil.updateFBOSize(BLUR_H2, lastWidth / 4, lastHeight / 4);
        RenderUtil.updateFBOSize(BLUR_W, lastWidth / 8, lastHeight / 8);
        RenderUtil.updateFBOSize(BLUR_W2, lastWidth / 4, lastHeight / 4);
    }

    public static Framebuffer renderBlur1(float step) {
        cleanUP();

        int lastOP = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        Shaders.renderFullImageInFBO(BLUR_H2, Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", 0, step)).bindFramebufferTexture();
        Shaders.renderFullImageInFBO(BLUR_W2, Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", step, 0)).bindFramebufferTexture();
        Shaders.renderFullImageInFBO(BLUR_H, Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", 0, step)).bindFramebufferTexture();
        Shaders.renderFullImageInFBO(BLUR_W, Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", step, 0)).bindFramebufferTexture();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, lastOP);
        return BLUR_W;
    }

    public static Framebuffer renderBlur2() {
        Shaders.renderFullImageInFBO(PingPongBuffer.swap(), Shaders.BLOOM_BUFFER_A, null).bindFramebufferTexture();
        Shaders.renderFullImageInFBO(PingPongBuffer.swap(), Shaders.BLOOM_BUFFER_B, null).bindFramebufferTexture();

        int lastOP = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        Shaders.renderFullImageInFBO(PingPongBuffer.swap(), Shaders.BLOOM_BUFFER_C, null).bindFramebufferTexture();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, lastOP);
        return PingPongBuffer.getCurrentBuffer(false);
    }

    public static Framebuffer renderBlur3(int loop, float step) {
        for (int i = 0; i < loop; i++) {
            Shaders.renderFullImageInFBO(PingPongBuffer.swap(true), Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", 0, step)).bindFramebufferTexture();
            Shaders.renderFullImageInFBO(PingPongBuffer.swap(), Shaders.BLUR, uniformCache -> uniformCache.glUniform2F("blurDir", step, 0)).bindFramebufferTexture();
        }
        return PingPongBuffer.getCurrentBuffer(false);
    }
}
