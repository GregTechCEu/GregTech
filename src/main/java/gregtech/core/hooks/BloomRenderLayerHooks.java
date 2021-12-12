package gregtech.core.hooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gregtech.api.render.DepthTextureHook;
import gregtech.api.render.ICustomRenderFast;
import gregtech.api.render.shader.Shaders;
import gregtech.api.render.shader.postprocessing.BloomEffect;
import gregtech.api.util.RenderUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_LINEAR;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class BloomRenderLayerHooks {

    public static BlockRenderLayer BLOOM;
    private static Framebuffer BLOOM_FBO;
    private static List<Runnable> RENDER_DYNAMICS;
    private static Map<ICustomRenderFast, List<Consumer<BufferBuilder>>> RENDER_FAST;

    public static BlockRenderLayer getRealBloomLayer(){
        return Shaders.isOptiFineShaderPackLoaded() ? BlockRenderLayer.CUTOUT : BLOOM;
    }

    public static void init() {
        BLOOM = EnumHelper.addEnum(BlockRenderLayer.class, "BLOOM", new Class[]{String.class}, "Bloom");
        RENDER_FAST = Maps.newHashMap();
        RENDER_DYNAMICS = Lists.newLinkedList();
    }

    public static void initBloomRenderLayer(BufferBuilder[] worldRenderers) {
        worldRenderers[BLOOM.ordinal()] = new BufferBuilder(131072);
    }

    public static int renderBloomBlockLayer(RenderGlobal renderglobal, BlockRenderLayer blockRenderLayer, double partialTicks, int pass, Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.profiler.endStartSection("BTLayer");
        if (Shaders.isOptiFineShaderPackLoaded()) {
            int result =  renderglobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);
            RENDER_DYNAMICS.clear();
            RENDER_FAST.clear();
            return result;
        } else if (!ConfigHolder.client.shader.emissiveTexturesBloom) {
            GlStateManager.depthMask(true);
            renderglobal.renderBlockLayer(BloomRenderLayerHooks.BLOOM, partialTicks, pass, entity);
            // render dynamics
            if (!RENDER_DYNAMICS.isEmpty()) {
                RENDER_DYNAMICS.forEach(Runnable::run);
                RENDER_DYNAMICS.clear();
            }

            // render fast
            if (!RENDER_FAST.isEmpty()) {
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                RENDER_FAST.forEach((handler, list)->{
                    handler.preDraw(buffer);
                    list.forEach(consumer->consumer.accept(buffer));
                    handler.postDraw(buffer);
                });
                RENDER_FAST.clear();
            }
            GlStateManager.depthMask(false);
            return renderglobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);
        }

        Framebuffer fbo = mc.getFramebuffer();

        if (BLOOM_FBO == null || BLOOM_FBO.framebufferWidth != fbo.framebufferWidth || BLOOM_FBO.framebufferHeight != fbo.framebufferHeight || (fbo.isStencilEnabled() && !BLOOM_FBO.isStencilEnabled())) {
            if (BLOOM_FBO == null) {
                BLOOM_FBO = new Framebuffer(fbo.framebufferWidth, fbo.framebufferHeight, false);
                BLOOM_FBO.setFramebufferColor(0, 0, 0, 0);
            } else {
                BLOOM_FBO.createBindFramebuffer(fbo.framebufferWidth, fbo.framebufferHeight);
            }
            if (fbo.isStencilEnabled() && !BLOOM_FBO.isStencilEnabled()) {
                BLOOM_FBO.enableStencil();
            }
            if (DepthTextureHook.isLastBind() && DepthTextureHook.isUseDefaultFBO()) {
                RenderUtil.hookDepthTexture(BLOOM_FBO, DepthTextureHook.framebufferDepthTexture);
            } else {
                RenderUtil.hookDepthBuffer(BLOOM_FBO, fbo.depthBuffer);
            }
            BLOOM_FBO.setFramebufferFilter(GL_LINEAR);
        }

        BLOOM_FBO.framebufferClear();
        BLOOM_FBO.bindFramebuffer(true);

        // render to BLOOM BUFFER
        GlStateManager.depthMask(true);
        renderglobal.renderBlockLayer(BloomRenderLayerHooks.BLOOM, partialTicks, pass, entity);

        // render dynamics
        if (!RENDER_DYNAMICS.isEmpty()) {
            RENDER_DYNAMICS.forEach(Runnable::run);
            RENDER_DYNAMICS.clear();
        }

        // render fast
        if (!RENDER_FAST.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            RENDER_FAST.forEach((handler, list)->{
                handler.preDraw(buffer);
                list.forEach(consumer->consumer.accept(buffer));
                handler.postDraw(buffer);
            });
            RENDER_FAST.clear();
        }
        GlStateManager.depthMask(false);

        // fast render bloom layer to main fbo
        BLOOM_FBO.bindFramebufferTexture();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        // reset next layer's render state and render
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, fbo.framebufferObject);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(7425);

        int result = renderglobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);

        mc.profiler.endStartSection("bloom");

        // blend bloom + transparent
        fbo.bindFramebufferTexture();
        GlStateManager.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ZERO);
        Shaders.renderFullImageInFBO(BLOOM_FBO, Shaders.IMAGE_F, null);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // render bloom effect to fbo
        switch (ConfigHolder.client.shader.bloomStyle) {
            case 0:
                BloomEffect.renderLOG(BLOOM_FBO, fbo, (float) ConfigHolder.client.shader.strength);
                break;
            case 1:
                BloomEffect.renderUnity(BLOOM_FBO, fbo, (float) ConfigHolder.client.shader.strength);
                break;
            case 2:
                BloomEffect.renderUnreal(BLOOM_FBO, fbo, (float) ConfigHolder.client.shader.strength);
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

    public static Framebuffer getBloomFBO() {
        return BLOOM_FBO;
    }

    public static void requestRenderDynamic(Runnable render) {
        if (render != null) {
            RENDER_DYNAMICS.add(render);
        }
    }

    public static void requestRenderFast(ICustomRenderFast handler, Consumer<BufferBuilder> render) {
        if (render != null && handler != null) {
            if (!RENDER_FAST.containsKey(handler)) {
                RENDER_FAST.put(handler, Lists.newLinkedList());
            }
            RENDER_FAST.get(handler).add(render);
        }
    }
}
