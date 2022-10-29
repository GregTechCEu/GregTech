package gregtech.client.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gregtech.client.renderer.ICustomRenderFast;
import gregtech.client.shader.Shaders;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_LINEAR;

@SideOnly(Side.CLIENT)
public class BloomEffectUtil {

    public static BlockRenderLayer BLOOM;
    private static Framebuffer BLOOM_FBO;
    private static Map<IBloomRenderFast, List<Consumer<BufferBuilder>>> RENDER_FAST;

    public static BlockRenderLayer getRealBloomLayer(){
        return Shaders.isOptiFineShaderPackLoaded() ? BlockRenderLayer.CUTOUT : BLOOM;
    }

    public static void init() {
        BLOOM = EnumHelper.addEnum(BlockRenderLayer.class, "BLOOM", new Class[]{String.class}, "Bloom");
        if (Loader.isModLoaded("nothirium")) {
            try {
                //Nothirium hard copies the BlockRenderLayer enum into a ChunkRenderPass enum. Add our BLOOM layer to that too.
                Class crp = Class.forName("meldexun.nothirium.api.renderer.chunk.ChunkRenderPass", false, Launch.classLoader);
                EnumHelper.addEnum(crp, "BLOOM", new Class[]{});
                Field all = FieldUtils.getField(crp, "ALL", false);
                FieldUtils.removeFinalModifier(all);
                FieldUtils.writeStaticField(all, crp.getEnumConstants());
            } catch (ClassNotFoundException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        RENDER_FAST = Maps.newHashMap();
    }

    public static void initBloomRenderLayer(BufferBuilder[] worldRenderers) {
        worldRenderers[BLOOM.ordinal()] = new BufferBuilder(131072);
    }

    public static int renderBloomBlockLayer(RenderGlobal renderglobal, BlockRenderLayer blockRenderLayer, double partialTicks, int pass, Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.profiler.endStartSection("BTLayer");
        if (Shaders.isOptiFineShaderPackLoaded()) {
            int result =  renderglobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);
            RENDER_FAST.clear();
            return result;
        } else if (!ConfigHolder.client.shader.emissiveTexturesBloom) {
            GlStateManager.depthMask(true);
            renderglobal.renderBlockLayer(BloomEffectUtil.BLOOM, partialTicks, pass, entity);

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
            if (DepthTextureUtil.isLastBind() && DepthTextureUtil.isUseDefaultFBO()) {
                RenderUtil.hookDepthTexture(BLOOM_FBO, DepthTextureUtil.framebufferDepthTexture);
            } else {
                RenderUtil.hookDepthBuffer(BLOOM_FBO, fbo.depthBuffer);
            }
            BLOOM_FBO.setFramebufferFilter(GL_LINEAR);
        }



        GlStateManager.depthMask(true);

        fbo.bindFramebuffer(true);

        // render fast
        if (!RENDER_FAST.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            RENDER_FAST.forEach((handler, list)->{
                handler.preDraw(buffer);
                list.forEach(consumer->consumer.accept(buffer));
                handler.postDraw(buffer);
            });
        }

        // render to BLOOM BUFFER
        BLOOM_FBO.framebufferClear();
        BLOOM_FBO.bindFramebuffer(false);

        renderglobal.renderBlockLayer(BloomEffectUtil.BLOOM, partialTicks, pass, entity);

        GlStateManager.depthMask(false);

        // fast render bloom layer to main fbo
        BLOOM_FBO.bindFramebufferTexture();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        // reset transparent layer render state and render
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
        BloomEffect.strength = (float) ConfigHolder.client.shader.strength;
        BloomEffect.baseBrightness = (float) ConfigHolder.client.shader.baseBrightness;
        BloomEffect.highBrightnessThreshold = (float) ConfigHolder.client.shader.highBrightnessThreshold;
        BloomEffect.lowBrightnessThreshold = (float) ConfigHolder.client.shader.lowBrightnessThreshold;
        BloomEffect.step = (float) ConfigHolder.client.shader.step;
        switch (ConfigHolder.client.shader.bloomStyle) {
            case 0:
                BloomEffect.renderLOG(BLOOM_FBO, fbo);
                break;
            case 1:
                BloomEffect.renderUnity(BLOOM_FBO, fbo);
                break;
            case 2:
                BloomEffect.renderUnreal(BLOOM_FBO, fbo);
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



        //********** render custom bloom ************

        // render fast
        if (!RENDER_FAST.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            RENDER_FAST.forEach((handler, list)->{
                GlStateManager.depthMask(true);

                BLOOM_FBO.framebufferClear();
                BLOOM_FBO.bindFramebuffer(true);

                handler.preDraw(buffer);
                list.forEach(consumer->consumer.accept(buffer));
                handler.postDraw(buffer);

                GlStateManager.depthMask(false);

                // blend bloom + transparent
                fbo.bindFramebufferTexture();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ZERO);
                Shaders.renderFullImageInFBO(BLOOM_FBO, Shaders.IMAGE_F, null);
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                switch (handler.customBloomStyle()) {
                    case 0:
                        BloomEffect.renderLOG(BLOOM_FBO, fbo);
                        break;
                    case 1:
                        BloomEffect.renderUnity(BLOOM_FBO, fbo);
                        break;
                    case 2:
                        BloomEffect.renderUnreal(BLOOM_FBO, fbo);
                        break;
                    default:
                        GlStateManager.disableBlend();
                        return;
                }

                // render bloom blend result to fbo
                GlStateManager.disableBlend();
                Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);
            });
            RENDER_FAST.clear();
        }

        return result;
    }

    public static Framebuffer getBloomFBO() {
        return BLOOM_FBO;
    }

    public static void requestCustomBloom(IBloomRenderFast handler, Consumer<BufferBuilder> render) {
        RENDER_FAST.computeIfAbsent(handler, (x)->Lists.newLinkedList()).add(render);
    }

    public interface IBloomRenderFast extends ICustomRenderFast {

        /**
         * Custom Bloom Style.
         *
         * @return
         * 0 - Simple Gaussian Blur Bloom
         * <p>
         *  1 - Unity Bloom
         * </p>
         * <p>
         * 2 - Unreal Bloom
         * </p>
         */
         int customBloomStyle();

    }
}
