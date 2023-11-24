package gregtech.client.utils;

import com.github.bsideup.jabel.Desugar;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.Shaders;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_LINEAR;

@SideOnly(Side.CLIENT)
public class BloomEffectUtil {

    private static final Map<BloomRenderSetup, List<Consumer<BufferBuilder>>> SCHEDULED_BLOOM_RENDERS = new Object2ObjectOpenHashMap<>();

    /**
     * @deprecated use {@link #getBloomLayer()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static BlockRenderLayer BLOOM;

    private static BlockRenderLayer bloom;
    private static Framebuffer bloomFBO;

    /**
     * @return {@link BlockRenderLayer} instance for the bloom render layer.
     */
    @Nonnull
    public static BlockRenderLayer getBloomLayer() {
        return Objects.requireNonNull(bloom, "Bloom effect is not initialized yet");
    }

    /**
     * @deprecated renamed for clarity; use {@link #getEffectiveBloomLayer()}.
     */
    @Nonnull
    @Deprecated
    public static BlockRenderLayer getRealBloomLayer() {
        return getEffectiveBloomLayer();
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, {@link BlockRenderLayer#CUTOUT} is returned instead.
     *
     * @return {@link BlockRenderLayer} instance for the bloom render layer, or {@link BlockRenderLayer#CUTOUT} if
     * bloom layer is disabled
     * @see #getEffectiveBloomLayer(BlockRenderLayer)
     */
    @Nonnull
    public static BlockRenderLayer getEffectiveBloomLayer() {
        return getEffectiveBloomLayer(BlockRenderLayer.CUTOUT);
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, the fallback layer specified is returned instead.
     *
     * @param fallback Block render layer to be returned when bloom layer is disabled
     * @return {@link BlockRenderLayer} instance for the bloom render layer, or {@code fallback} if bloom layer is
     * disabled
     * @see #getEffectiveBloomLayer(boolean, BlockRenderLayer)
     */
    @Contract("null -> _; !null -> !null")
    public static BlockRenderLayer getEffectiveBloomLayer(BlockRenderLayer fallback) {
        return Shaders.isOptiFineShaderPackLoaded() ? fallback : bloom;
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, {@link BlockRenderLayer#CUTOUT} is returned instead.
     *
     * @param isBloomActive Whether bloom layer should be active. If this value is {@code false}, {@code fallback} layer
     *                      will be returned. Has no effect if Optifine is present.
     * @return {@link BlockRenderLayer} instance for the bloom render layer, or {@link BlockRenderLayer#CUTOUT} if
     * bloom layer is disabled
     * @see #getEffectiveBloomLayer(boolean, BlockRenderLayer)
     */
    @Nonnull
    public static BlockRenderLayer getEffectiveBloomLayer(boolean isBloomActive) {
        return getEffectiveBloomLayer(isBloomActive, BlockRenderLayer.CUTOUT);
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, the fallback layer specified is returned instead.
     *
     * @param isBloomActive Whether bloom layer should be active. If this value is {@code false}, {@code fallback} layer
     *                      will be returned. Has no effect if Optifine is present.
     * @param fallback      Block render layer to be returned when bloom layer is disabled
     * @return {@link BlockRenderLayer} instance for the bloom render layer, or {@code fallback} if bloom layer is
     * disabled
     */
    @Contract("_, null -> _; _, !null -> !null")
    public static BlockRenderLayer getEffectiveBloomLayer(boolean isBloomActive, BlockRenderLayer fallback) {
        return Shaders.isOptiFineShaderPackLoaded() || !isBloomActive ? fallback : bloom;
    }

    /**
     * @return bloom framebuffer object
     */
    @Nullable
    public static Framebuffer getBloomFBO() {
        return bloomFBO;
    }

    /**
     * Schedule a custom bloom render function for next world render. This render call gets processed only once; all
     * scheduled render callbacks are cleared once they have been processed.
     *
     * @param setup     Render setup, if exists
     * @param bloomType Type of the bloom
     * @param render    The function to be called on next world render
     */
    public static void scheduleBloomRender(@Nullable IRenderSetup setup,
                                           @Nonnull BloomType bloomType,
                                           @Nonnull Consumer<BufferBuilder> render) {
        SCHEDULED_BLOOM_RENDERS.computeIfAbsent(new BloomRenderSetup(setup, bloomType), x -> new ArrayList<>()).add(render);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void init() {
        bloom = EnumHelper.addEnum(BlockRenderLayer.class, "BLOOM", new Class[]{String.class}, "Bloom");
        BLOOM = bloom;
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
    }

    // Calls injected via ASM
    @SuppressWarnings("unused")
    public static void initBloomRenderLayer(BufferBuilder[] worldRenderers) {
        worldRenderers[bloom.ordinal()] = new BufferBuilder(131072);
    }

    // Calls injected via ASM
    @SuppressWarnings("unused")
    public static int renderBloomBlockLayer(RenderGlobal renderGlobal,
                                            BlockRenderLayer blockRenderLayer, // 70% sure it's translucent uh yeah
                                            double partialTicks,
                                            int pass,
                                            Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.profiler.endStartSection("BTLayer");

        if (Shaders.isOptiFineShaderPackLoaded()) {
            int result = renderGlobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);
            SCHEDULED_BLOOM_RENDERS.clear();
            return result;
        }

        if (!ConfigHolder.client.shader.emissiveTexturesBloom) {
            GlStateManager.depthMask(true);
            renderGlobal.renderBlockLayer(BloomEffectUtil.bloom, partialTicks, pass, entity);

            // render fast
            if (!SCHEDULED_BLOOM_RENDERS.isEmpty()) {
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                for (var e : SCHEDULED_BLOOM_RENDERS.entrySet()) {
                    draw(buffer, e.getKey(), e.getValue());
                }
                SCHEDULED_BLOOM_RENDERS.clear();
            }
            GlStateManager.depthMask(false);
            return renderGlobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);
        }

        Framebuffer fbo = mc.getFramebuffer();

        if (bloomFBO == null ||
                bloomFBO.framebufferWidth != fbo.framebufferWidth ||
                bloomFBO.framebufferHeight != fbo.framebufferHeight ||
                (fbo.isStencilEnabled() && !bloomFBO.isStencilEnabled())) {
            if (bloomFBO == null) {
                bloomFBO = new Framebuffer(fbo.framebufferWidth, fbo.framebufferHeight, false);
                bloomFBO.setFramebufferColor(0, 0, 0, 0);
            } else {
                bloomFBO.createBindFramebuffer(fbo.framebufferWidth, fbo.framebufferHeight);
            }

            if (fbo.isStencilEnabled() && !bloomFBO.isStencilEnabled()) {
                bloomFBO.enableStencil();
            }

            if (DepthTextureUtil.isLastBind() && DepthTextureUtil.isUseDefaultFBO()) {
                RenderUtil.hookDepthTexture(bloomFBO, DepthTextureUtil.framebufferDepthTexture);
            } else {
                RenderUtil.hookDepthBuffer(bloomFBO, fbo.depthBuffer);
            }

            bloomFBO.setFramebufferFilter(GL_LINEAR);
        }

        GlStateManager.depthMask(true);
        fbo.bindFramebuffer(true);

        // render fast
        if (!SCHEDULED_BLOOM_RENDERS.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            for (var entry : SCHEDULED_BLOOM_RENDERS.entrySet()) {
                draw(buffer, entry.getKey(), entry.getValue());
            }
        }

        // render to BLOOM BUFFER
        bloomFBO.framebufferClear();
        bloomFBO.bindFramebuffer(false);

        renderGlobal.renderBlockLayer(BloomEffectUtil.bloom, partialTicks, pass, entity);

        GlStateManager.depthMask(false);

        // fast render bloom layer to main fbo
        bloomFBO.bindFramebufferTexture();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        // reset transparent layer render state and render
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, fbo.framebufferObject);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(7425);

        int result = renderGlobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);

        mc.profiler.endStartSection("bloom");

        // blend bloom + transparent
        fbo.bindFramebufferTexture();
        GlStateManager.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ZERO);
        Shaders.renderFullImageInFBO(bloomFBO, Shaders.IMAGE_F, null);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // render bloom effect to fbo
        BloomEffect.strength = (float) ConfigHolder.client.shader.strength;
        BloomEffect.baseBrightness = (float) ConfigHolder.client.shader.baseBrightness;
        BloomEffect.highBrightnessThreshold = (float) ConfigHolder.client.shader.highBrightnessThreshold;
        BloomEffect.lowBrightnessThreshold = (float) ConfigHolder.client.shader.lowBrightnessThreshold;
        BloomEffect.step = (float) ConfigHolder.client.shader.step;
        switch (ConfigHolder.client.shader.bloomStyle) {
            case 0 -> BloomEffect.renderLOG(bloomFBO, fbo);
            case 1 -> BloomEffect.renderUnity(bloomFBO, fbo);
            case 2 -> BloomEffect.renderUnreal(bloomFBO, fbo);
            default -> {
                GlStateManager.depthMask(false);
                GlStateManager.disableBlend();
                return result;
            }
        }

        GlStateManager.depthMask(false);

        // render bloom blend result to fbo
        GlStateManager.disableBlend();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        //********** render custom bloom ************

        // render fast
        if (!SCHEDULED_BLOOM_RENDERS.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            for (var e : SCHEDULED_BLOOM_RENDERS.entrySet()) {
                BloomRenderSetup handler = e.getKey();
                List<Consumer<BufferBuilder>> list = e.getValue();

                GlStateManager.depthMask(true);

                bloomFBO.framebufferClear();
                bloomFBO.bindFramebuffer(true);

                draw(buffer, handler, list);

                GlStateManager.depthMask(false);

                // blend bloom + transparent
                fbo.bindFramebufferTexture();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ZERO);
                Shaders.renderFullImageInFBO(bloomFBO, Shaders.IMAGE_F, null);
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                switch (handler.bloomType) {
                    case GAUSSIAN -> BloomEffect.renderLOG(bloomFBO, fbo);
                    case UNITY -> BloomEffect.renderUnity(bloomFBO, fbo);
                    case UNREAL -> BloomEffect.renderUnreal(bloomFBO, fbo);
                    default -> {
                        GlStateManager.disableBlend();
                        continue;
                    }
                }

                // render bloom blend result to fbo
                GlStateManager.disableBlend();
                Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);
            }
            SCHEDULED_BLOOM_RENDERS.clear();
        }

        return result;
    }

    private static void draw(@Nonnull BufferBuilder buffer,
                             @Nonnull BloomRenderSetup handler,
                             @Nonnull List<Consumer<BufferBuilder>> renderers) {
        if (handler.renderSetup != null) {
            handler.renderSetup.preDraw(buffer);
        }
        for (Consumer<BufferBuilder> renderer : renderers) {
            renderer.accept(buffer);
        }
        if (handler.renderSetup != null) {
            handler.renderSetup.postDraw(buffer);
        }
    }

    @Desugar
    private record BloomRenderSetup(@Nullable IRenderSetup renderSetup, @Nonnull BloomType bloomType) {}

    private static final class BloomRenderTicket {

    }
}
