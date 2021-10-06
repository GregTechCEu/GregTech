package gregtech.api.render.shader.postprocessing;

import gregtech.api.render.shader.PingPongBuffer;
import gregtech.api.render.shader.Shaders;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL13;

@SideOnly(Side.CLIENT)
public class BloomEffect {

    public static void render(Framebuffer highLightFBO, Framebuffer backgroundFBO) {
        PingPongBuffer.updateSize(backgroundFBO.framebufferWidth, backgroundFBO.framebufferHeight);
        highLightFBO.bindFramebufferTexture();
        Framebuffer blurFBO = BlurEffect.renderBlur1(1);

        // bind main fbo
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.enableTexture2D();
        backgroundFBO.bindFramebufferTexture();
        // bind blur fbo
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.enableTexture2D();
        blurFBO.bindFramebufferTexture();

        // blend shader
        Shaders.renderFullImageInFBO(PingPongBuffer.swap(), Shaders.BLOOM_COMBINE, uniformCache -> {
            uniformCache.glUniform1I("buffer_a", 0);
            uniformCache.glUniform1I("buffer_b", 1);
            uniformCache.glUniform1F("intensive", 3);
        });

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.bindTexture(0);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(0);

        PingPongBuffer.bindFramebufferTexture();
    }

}
