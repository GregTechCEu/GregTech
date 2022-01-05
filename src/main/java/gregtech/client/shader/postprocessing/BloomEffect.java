package gregtech.client.shader.postprocessing;

import gregtech.client.shader.PingPongBuffer;
import gregtech.client.shader.Shaders;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

@SideOnly(Side.CLIENT)
public class BloomEffect {
    private static Framebuffer[] BUFFERS_D;
    private static Framebuffer[] BUFFERS_U;
    public static float strength = (float) ConfigHolder.client.shader.strength;
    public static float baseBrightness = (float) ConfigHolder.client.shader.baseBrightness;
    public static float highBrightnessThreshold = (float) ConfigHolder.client.shader.highBrightnessThreshold;
    public static float lowBrightnessThreshold = (float) ConfigHolder.client.shader.lowBrightnessThreshold;
    public static float step = (float) ConfigHolder.client.shader.step;

    private static void blend(Framebuffer bloom, Framebuffer backgroundFBO) {
        // bind main fbo
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.enableTexture2D();
        backgroundFBO.bindFramebufferTexture();

        // bind blur fbo
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.enableTexture2D();
        bloom.bindFramebufferTexture();

        // blend shader
        Shaders.renderFullImageInFBO(PingPongBuffer.swap(), Shaders.BLOOM_COMBINE, uniformCache -> {
            uniformCache.glUniform1I("buffer_a", 0);
            uniformCache.glUniform1I("buffer_b", 1);
            uniformCache.glUniform1F("intensive", strength);
            uniformCache.glUniform1F("base", baseBrightness);
            uniformCache.glUniform1F("threshold_up", highBrightnessThreshold);
            uniformCache.glUniform1F("threshold_down", lowBrightnessThreshold);
        });

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.bindTexture(0);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);

        PingPongBuffer.bindFramebufferTexture();
    }

    private static void cleanUP(int lastWidth, int lastHeight) {
        if (BUFFERS_D == null || BUFFERS_D.length != ConfigHolder.client.shader.nMips) {
            if (BUFFERS_D != null) {
                for (int i = 0; i < BUFFERS_D.length; i++) {
                    BUFFERS_D[i].deleteFramebuffer();
                    BUFFERS_U[i].deleteFramebuffer();
                }
            }

            BUFFERS_D = new Framebuffer[ConfigHolder.client.shader.nMips];
            BUFFERS_U = new Framebuffer[ConfigHolder.client.shader.nMips];

            int resX = lastWidth / 2;
            int resY = lastHeight / 2;

            for (int i = 0; i < ConfigHolder.client.shader.nMips; i++) {
                BUFFERS_D[i] = new Framebuffer(resX, resY, false);
                BUFFERS_U[i] = new Framebuffer(resX, resY, false);
                BUFFERS_D[i].setFramebufferColor(0, 0, 0, 0);
                BUFFERS_U[i].setFramebufferColor(0, 0, 0, 0);
                BUFFERS_D[i].setFramebufferFilter(GL11.GL_LINEAR);
                BUFFERS_U[i].setFramebufferFilter(GL11.GL_LINEAR);
                resX /= 2;
                resY /= 2;
            }
        } else if (RenderUtil.updateFBOSize(BUFFERS_D[0], lastWidth / 2, lastHeight / 2)) {
            int resX = lastWidth / 2;
            int resY = lastHeight / 2;
            for (int i = 0; i < ConfigHolder.client.shader.nMips; i++) {
                RenderUtil.updateFBOSize(BUFFERS_D[i], resX, resY);
                RenderUtil.updateFBOSize(BUFFERS_U[i], resX, resY);
                BUFFERS_D[i].setFramebufferFilter(GL11.GL_LINEAR);
                BUFFERS_U[i].setFramebufferFilter(GL11.GL_LINEAR);
                resX /= 2;
                resY /= 2;
            }
        }
        PingPongBuffer.updateSize(lastWidth, lastHeight);
    }

    public static void renderLOG(Framebuffer highLightFBO, Framebuffer backgroundFBO) {
        PingPongBuffer.updateSize(backgroundFBO.framebufferWidth, backgroundFBO.framebufferHeight);
        BlurEffect.updateSize(backgroundFBO.framebufferWidth, backgroundFBO.framebufferHeight);
        highLightFBO.bindFramebufferTexture();
        blend(BlurEffect.renderBlur1(step), backgroundFBO);
    }

    public static void renderUnity(Framebuffer highLightFBO, Framebuffer backgroundFBO) {
        cleanUP(backgroundFBO.framebufferWidth, backgroundFBO.framebufferHeight);

        renderDownSampling(highLightFBO, BUFFERS_D[0]);
        for (int i = 0; i < BUFFERS_D.length - 1; i++) {
            renderDownSampling(BUFFERS_D[i], BUFFERS_D[i + 1]);
        }

        renderUpSampling(BUFFERS_D[BUFFERS_D.length - 1], BUFFERS_D[BUFFERS_D.length - 2], BUFFERS_U[BUFFERS_D.length - 2]);
        for (int i = BUFFERS_U.length - 2; i > 0; i--) {
            renderUpSampling(BUFFERS_U[i], BUFFERS_D[i - 1], BUFFERS_U[i-1]);
        }
        renderUpSampling(BUFFERS_U[0], highLightFBO, PingPongBuffer.swap());

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.bindTexture(0);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);

        blend(PingPongBuffer.getCurrentBuffer(false), backgroundFBO);
    }

    private static void renderDownSampling(Framebuffer U, Framebuffer D) {
        U.bindFramebufferTexture();
        Shaders.renderFullImageInFBO(D, Shaders.DOWN_SAMPLING, uniformCache -> uniformCache.glUniform2F("u_resolution2", U.framebufferWidth, U.framebufferHeight));
    }

    private static void renderUpSampling(Framebuffer U, Framebuffer D, Framebuffer T) {
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.enableTexture2D();
        U.bindFramebufferTexture();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.enableTexture2D();
        D.bindFramebufferTexture();

        Shaders.renderFullImageInFBO(T, Shaders.UP_SAMPLING, uniformCache -> {
            uniformCache.glUniform1I("upTexture", 0);
            uniformCache.glUniform1I("downTexture", 1);
            uniformCache.glUniform2F("u_resolution2", U.framebufferWidth, U.framebufferHeight);
        });
    }

    public static void renderUnreal(Framebuffer highLightFBO, Framebuffer backgroundFBO) {
        cleanUP(backgroundFBO.framebufferWidth, backgroundFBO.framebufferHeight);

        // blur all mips
        int[] kernelSizeArray = new int[]{3, 5, 7, 9, 11};
        highLightFBO.bindFramebufferTexture();
        for (int i = 0; i < BUFFERS_D.length; i++) {
            Framebuffer buffer_h = BUFFERS_D[i];
            int kernel = kernelSizeArray[i];
            Shaders.renderFullImageInFBO(buffer_h, Shaders.S_BLUR, uniformCache -> {
                uniformCache.glUniform2F("texSize", buffer_h.framebufferWidth, buffer_h.framebufferHeight);
                uniformCache.glUniform2F("blurDir", step, 0);
                uniformCache.glUniform1I("kernel_radius", kernel);
            }).bindFramebufferTexture();

            Framebuffer buffer_v = BUFFERS_U[i];
            Shaders.renderFullImageInFBO(buffer_v, Shaders.S_BLUR, uniformCache -> {
                uniformCache.glUniform2F("texSize", buffer_v.framebufferWidth, buffer_v.framebufferHeight);
                uniformCache.glUniform2F("blurDir", 0, step);
                uniformCache.glUniform1I("kernel_radius", kernel);
            }).bindFramebufferTexture();
        }

        // composite all mips
        for (int i = 0; i < BUFFERS_D.length; i++) {
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + i);
            GlStateManager.enableTexture2D();
            BUFFERS_U[i].bindFramebufferTexture();
        }

        Shaders.renderFullImageInFBO(BUFFERS_D[0], Shaders.COMPOSITE, uniformCache -> {
            uniformCache.glUniform1I("blurTexture1", 0);
            uniformCache.glUniform1I("blurTexture2", 1);
            uniformCache.glUniform1I("blurTexture3", 2);
            uniformCache.glUniform1I("blurTexture4", 3);
            uniformCache.glUniform1I("blurTexture5", 4);
            uniformCache.glUniform1F("bloomStrength", strength);
            uniformCache.glUniform1F("bloomRadius", 1);
        });

        for (int i = BUFFERS_D.length - 1; i >= 0; i--) {
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + i);
            GlStateManager.bindTexture(0);
        }

        blend(BUFFERS_D[0], backgroundFBO);
    }

}
