package gregtech.client.utils;

import gregtech.client.shader.Shaders;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
 *               The default FBO is used, unfortunately, sometimes we have to abandon native way to create a new fbo.
 *               But generally not.
 */
@SideOnly(Side.CLIENT)
public class DepthTextureUtil {

    public static int framebufferObject;
    public static int framebufferDepthTexture;
    private static boolean useDefaultFBO = true;
    private static boolean lastBind;
    private static int lastWidth, lastHeight;

    private static boolean shouldRenderDepthTexture() {
        return lastBind && !Shaders.isOptiFineShaderPackLoaded() && ConfigHolder.client.hookDepthTexture &&
                OpenGlHelper.isFramebufferEnabled();
    }

    public static void onPreWorldRender(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase == TickEvent.Phase.START && mc.world != null) {
            if (shouldRenderDepthTexture()) {
                if (useDefaultFBO && GL11.glGetError() != 0) { // if we can't use the vanilla fbo.... okay, why not
                                                               // create our own fbo?
                    useDefaultFBO = false;
                    if (framebufferDepthTexture != 0) {
                        disposeDepthTexture();
                        createDepthTexture();
                    }
                }
                if (framebufferDepthTexture == 0) {
                    createDepthTexture();
                } else if (lastWidth != mc.getFramebuffer().framebufferWidth ||
                        lastHeight != mc.getFramebuffer().framebufferHeight) {
                            disposeDepthTexture();
                            createDepthTexture();
                        }
            } else {
                disposeDepthTexture();
            }
            lastBind = false;
        }
    }

    public static void renderWorld(RenderWorldLastEvent event) { // re-render world in our own fbo.
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewer = mc.getRenderViewEntity();
        if (DepthTextureUtil.framebufferDepthTexture != 0 && mc.world != null && viewer != null &&
                !DepthTextureUtil.useDefaultFBO) {
            int lastFBO = GlStateManager.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebufferObject);
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            GlStateManager.disableTexture2D();
            mc.renderGlobal.renderBlockLayer(BlockRenderLayer.SOLID, event.getPartialTicks(), 0, viewer);
            mc.renderGlobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, event.getPartialTicks(), 0, viewer);
            GlStateManager.enableTexture2D();
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastFBO);
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

        lastWidth = framebuffer.framebufferTextureWidth;
        lastHeight = framebuffer.framebufferTextureHeight;

        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebufferObject); // bind buffer then bind depth
                                                                                        // texture
        OpenGlHelper.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER,
                stencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : OpenGlHelper.GL_DEPTH_ATTACHMENT,
                GL11.GL_TEXTURE_2D,
                framebufferDepthTexture, 0);

        if (BloomEffectUtil.getBloomFBO() != null && useDefaultFBO) {
            RenderUtil.hookDepthTexture(BloomEffectUtil.getBloomFBO(), framebufferDepthTexture);
        }

        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastFBO);
    }

    public static void disposeDepthTexture() {
        if (framebufferDepthTexture != 0 || framebufferObject != 0) {
            if (useDefaultFBO) {
                Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
                Framebuffer bloomFBO = BloomEffectUtil.getBloomFBO();
                if (bloomFBO != null) {
                    RenderUtil.hookDepthBuffer(bloomFBO, framebuffer.depthBuffer);
                }
                RenderUtil.hookDepthBuffer(framebuffer, framebuffer.depthBuffer);
            } else {
                OpenGlHelper.glDeleteFramebuffers(framebufferObject);
            }
            TextureUtil.deleteTexture(framebufferDepthTexture);
            framebufferObject = 0;
            framebufferDepthTexture = 0;
        }
    }

    public static void bindDepthTexture() {
        lastBind = true;
        if (useDefaultFBO && framebufferDepthTexture != 0) {
            Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
            RenderUtil.hookDepthBuffer(framebuffer, framebuffer.depthBuffer);
        }
        GlStateManager.bindTexture(framebufferDepthTexture);
    }

    public static void unBindDepthTexture() {
        GlStateManager.bindTexture(0);
        if (useDefaultFBO) {
            Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
            RenderUtil.hookDepthTexture(framebuffer, framebufferDepthTexture);
        }
    }

    public static boolean isUseDefaultFBO() {
        return useDefaultFBO;
    }

    public static boolean isLastBind() {
        return framebufferObject != 0;
    }
}
