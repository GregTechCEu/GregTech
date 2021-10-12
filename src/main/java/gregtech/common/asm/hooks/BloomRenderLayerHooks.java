package gregtech.common.asm.hooks;

import gregtech.api.render.shader.Shaders;
import gregtech.api.render.shader.postprocessing.BloomEffect;
import gregtech.api.util.RenderUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.glGetInteger;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/10/04
 * @Description:
 */
@SideOnly(Side.CLIENT)
public class BloomRenderLayerHooks {
    public static BlockRenderLayer BLOOM;
    private static Framebuffer BLOOM_FBO;

    public static void preInit() {
        BLOOM = EnumHelper.addEnum(BlockRenderLayer.class, "BLOOM", new Class[]{String.class}, "Bloom");
    }

    public static void initBloomRenderLayer(BufferBuilder[] worldRenderers) {
        worldRenderers[BLOOM.ordinal()] = new BufferBuilder(131072);
    }

    public static int renderBloomBlockLayer(RenderGlobal renderglobal, BlockRenderLayer blockRenderLayer, double partialTicks, int pass, Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.profiler.endStartSection("BTLayer");
        if (!ConfigHolder.U.clientConfig.shader.bloom.emissiveTexturesBloom) {
            GlStateManager.depthMask(true);
            renderglobal.renderBlockLayer(BloomRenderLayerHooks.BLOOM, partialTicks, pass, entity);
            GlStateManager.depthMask(false);
            int result =  renderglobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);
            mc.profiler.endStartSection("bloom");
            return result;
        }

        int lastID = glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();

        if (BLOOM_FBO == null || BLOOM_FBO.framebufferWidth != fbo.framebufferWidth || BLOOM_FBO.framebufferHeight != fbo.framebufferHeight) {
            if (BLOOM_FBO == null) {
                BLOOM_FBO = new Framebuffer(fbo.framebufferWidth, fbo.framebufferHeight, false);
                BLOOM_FBO.setFramebufferColor(0, 0, 0, 0);
            } else {
                BLOOM_FBO.createBindFramebuffer(fbo.framebufferWidth, fbo.framebufferHeight);
            }
            RenderUtil.hookDepthBuffer(BLOOM_FBO, fbo);
        }

        BLOOM_FBO.framebufferClear();
        BLOOM_FBO.bindFramebuffer(true);

        // render to BLOOM BUFFER
        GlStateManager.depthMask(true);
        renderglobal.renderBlockLayer(BloomRenderLayerHooks.BLOOM, partialTicks, pass, entity);
        GlStateManager.depthMask(false);

        // fast render bloom layer to main fbo
        BLOOM_FBO.bindFramebufferTexture();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        // reset next layer's render state and render
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(7425);

        int result = renderglobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);

        mc.profiler.endStartSection("bloom");

        // render bloom effect to fbo
        switch (ConfigHolder.U.clientConfig.shader.bloom.bloomStyle) {
            case 0:
                BloomEffect.renderLOG(BLOOM_FBO, fbo, ConfigHolder.U.clientConfig.shader.bloom.intensive);
                break;
            case 2:
                BloomEffect.renderUnity(BLOOM_FBO, fbo, ConfigHolder.U.clientConfig.shader.bloom.intensive);
                break;
            case 1:
                BloomEffect.renderUnReal(BLOOM_FBO, fbo, ConfigHolder.U.clientConfig.shader.bloom.intensive);
                break;
            default:
                GlStateManager.depthMask(false);
                GlStateManager.disableBlend();
                return result;
        }

        GlStateManager.depthMask(false);

        // render bloom blend result to fbo
        GlStateManager.disableBlend();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        return result;
    }
}
