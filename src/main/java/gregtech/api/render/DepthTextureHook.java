package gregtech.api.render;

import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.glGetInteger;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/11
 * @Description: You'll need it when you need to get deep textures to do more cool things.
 * The default FBO is used, unfortunately, sometimes we have to abandon native way to create a new fbo. But generally not.
 */
@SideOnly(Side.CLIENT)
public class DepthTextureHook {
    public static int framebufferObject;
    public static int framebufferDepthTexture;
    private static boolean useDefaultFBO = true;

    private static boolean shouldRenderDepthTexture() {
        return ConfigHolder.U.clientConfig.hookDepthTexture;
    }

    public static void onPreWorldRender(final TickEvent.RenderTickEvent event) {
        if (shouldRenderDepthTexture() && event.phase == TickEvent.Phase.START && Minecraft.getMinecraft().world != null && OpenGlHelper.isFramebufferEnabled()) {
            if (useDefaultFBO && GL11.glGetError() != 0) { // if we can't use the vanilla fbo.... okay, why not create our own fbo?
                useDefaultFBO = false;
                if (framebufferDepthTexture != 0) {
                    disposeDepthTexture();
                    createDepthTexture();
                }
            }
            if (framebufferDepthTexture == 0) {
                createDepthTexture();
            }
        }
    }

    public static void createDepthTexture() {
        int lastFBO = glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
        boolean stencil = framebuffer.isStencilEnabled() && useDefaultFBO;

        if (useDefaultFBO) {
            framebufferObject = framebuffer.framebufferObject;
        } else {
            framebufferObject = OpenGlHelper.glGenFramebuffers();
        }

        framebufferDepthTexture = TextureUtil.glGenTextures(); // gen texture
        GlStateManager.bindTexture(framebufferDepthTexture);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_LUMINANCE);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LEQUAL);
        GlStateManager.glTexImage2D(GL11.GL_TEXTURE_2D, 0,
                stencil ? GL30.GL_DEPTH24_STENCIL8 : GL14.GL_DEPTH_COMPONENT24,
                framebuffer.framebufferTextureWidth,
                framebuffer.framebufferTextureHeight, 0,
                stencil ? GL30.GL_DEPTH_STENCIL : GL11.GL_DEPTH_COMPONENT,
                stencil ? GL30.GL_UNSIGNED_INT_24_8 : GL11.GL_UNSIGNED_INT, null);
        GlStateManager.bindTexture(0);

        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebufferObject); // bind buffer then bind depth texture
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, stencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : OpenGlHelper.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, framebufferDepthTexture, 0);

        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastFBO);
    }

    public static void disposeDepthTexture() {
        if (framebufferDepthTexture != 0 || framebufferObject != 0) {
            if (useDefaultFBO) {
                Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
                if (framebuffer.isStencilEnabled()) {
                    OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebufferObject);
                    OpenGlHelper.glFramebufferRenderbuffer(OpenGlHelper.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, OpenGlHelper.GL_RENDERBUFFER, framebuffer.depthBuffer);
                    OpenGlHelper.glFramebufferRenderbuffer(OpenGlHelper.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, OpenGlHelper.GL_RENDERBUFFER, framebuffer.depthBuffer);
                } else {
                    OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebufferObject);
                    OpenGlHelper.glFramebufferRenderbuffer(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_DEPTH_ATTACHMENT, OpenGlHelper.GL_RENDERBUFFER, framebuffer.depthBuffer);
                }
            } else {
                OpenGlHelper.glDeleteFramebuffers(framebufferObject);
            }
            TextureUtil.deleteTexture(framebufferDepthTexture);
            framebufferObject = 0;
            framebufferDepthTexture = 0;
        }
    }
}
