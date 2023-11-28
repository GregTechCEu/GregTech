package gregtech.client.utils;

import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.Shaders;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class BloomEffectUtil {

    private static final Map<BloomRenderKey, List<BloomRenderTicket>> BLOOM_RENDERS = new Object2ObjectOpenHashMap<>();
    private static final List<BloomRenderTicket> SCHEDULED_BLOOM_RENDERS = new ArrayList<>();

    /**
     * @deprecated use {@link #getBloomLayer()}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static BlockRenderLayer BLOOM;

    private static BlockRenderLayer bloom;
    private static Framebuffer bloomFBO;

    @Nullable
    private static World currentWorld = null;

    /**
     * @return {@link BlockRenderLayer} instance for the bloom render layer.
     */
    @NotNull
    public static BlockRenderLayer getBloomLayer() {
        return Objects.requireNonNull(bloom, "Bloom effect is not initialized yet");
    }

    /**
     * @deprecated renamed for clarity; use {@link #getEffectiveBloomLayer()}.
     */
    @NotNull
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static BlockRenderLayer getRealBloomLayer() {
        return getEffectiveBloomLayer();
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, {@link BlockRenderLayer#CUTOUT} is returned instead.
     *
     * @return {@link BlockRenderLayer} instance for the bloom render layer, or {@link BlockRenderLayer#CUTOUT} if
     *         bloom layer is disabled
     * @see #getEffectiveBloomLayer(BlockRenderLayer)
     */
    @NotNull
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
     *         disabled
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
     *         bloom layer is disabled
     * @see #getEffectiveBloomLayer(boolean, BlockRenderLayer)
     */
    @NotNull
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
     *         disabled
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
     * <p>
     * Register a custom bloom render callback for subsequent world render. The render call persists until it is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}, or automatically freed on client world change.
     * </p>
     * <p>
     * This method does nothing if Optifine is present.
     * </p>
     *
     * @param setup     Render setup, if exists
     * @param bloomType Type of the bloom
     * @param render    Rendering callback
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null}
     */
    @NotNull
    @CheckReturnValue
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull BloomType bloomType,
                                                        @NotNull IBloomEffect render) {
        BloomRenderTicket ticket = new BloomRenderTicket(setup, bloomType, render);
        if (Shaders.isOptiFineShaderPackLoaded()) {
            ticket.invalidate();
        } else {
            SCHEDULED_BLOOM_RENDERS.add(ticket);
        }
        return ticket;
    }

    /**
     * @deprecated use ticket-based bloom render hook
     *             {@link #registerBloomRender(IRenderSetup, BloomType, IBloomEffect)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public static void requestCustomBloom(IBloomRenderFast handler, Consumer<BufferBuilder> render) {
        BloomType bloomType = BloomType.fromValue(handler.customBloomStyle());
        registerBloomRender(handler, bloomType, (b, c) -> render.accept(b)).legacy = true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void init() {
        bloom = EnumHelper.addEnum(BlockRenderLayer.class, "BLOOM", new Class[] { String.class }, "Bloom");
        BLOOM = bloom;
        if (Loader.isModLoaded("nothirium")) {
            try {
                // Nothirium hard copies the BlockRenderLayer enum into a ChunkRenderPass enum. Add our BLOOM layer to
                // that too.
                Class crp = Class.forName("meldexun.nothirium.api.renderer.chunk.ChunkRenderPass", false,
                        Launch.classLoader);
                EnumHelper.addEnum(crp, "BLOOM", new Class[] {});
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
            return renderGlobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);
        }

        preDraw();

        EffectRenderContext context = EffectRenderContext.getInstance().update(entity, (float) partialTicks);

        if (!ConfigHolder.client.shader.emissiveTexturesBloom) {
            GlStateManager.depthMask(true);
            renderGlobal.renderBlockLayer(bloom, partialTicks, pass, entity);

            if (!BLOOM_RENDERS.isEmpty()) {
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                for (List<BloomRenderTicket> list : BLOOM_RENDERS.values()) {
                    draw(buffer, context, list);
                }
            }
            postDraw();
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

            bloomFBO.setFramebufferFilter(GL11.GL_LINEAR);
        }

        GlStateManager.depthMask(true);
        fbo.bindFramebuffer(true);

        if (!BLOOM_RENDERS.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            for (List<BloomRenderTicket> list : BLOOM_RENDERS.values()) {
                draw(buffer, context, list);
            }
        }

        // render to BLOOM BUFFER
        bloomFBO.framebufferClear();
        bloomFBO.bindFramebuffer(false);

        renderGlobal.renderBlockLayer(bloom, partialTicks, pass, entity);

        GlStateManager.depthMask(false);

        // fast render bloom layer to main fbo
        bloomFBO.bindFramebufferTexture();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        // reset transparent layer render state and render
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, fbo.framebufferObject);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

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
                postDraw();
                GlStateManager.depthMask(false);
                GlStateManager.disableBlend();
                return result;
            }
        }

        GlStateManager.depthMask(false);

        // render bloom blend result to fbo
        GlStateManager.disableBlend();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE_F, null);

        // ********** render custom bloom ************

        if (!BLOOM_RENDERS.isEmpty()) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            for (var e : BLOOM_RENDERS.entrySet()) {
                BloomRenderKey key = e.getKey();
                List<BloomRenderTicket> list = e.getValue();

                GlStateManager.depthMask(true);

                bloomFBO.framebufferClear();
                bloomFBO.bindFramebuffer(true);

                draw(buffer, context, list);

                GlStateManager.depthMask(false);

                // blend bloom + transparent
                fbo.bindFramebufferTexture();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ZERO);
                Shaders.renderFullImageInFBO(bloomFBO, Shaders.IMAGE_F, null);
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                switch (key.bloomType) {
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
            postDraw();
        }

        return result;
    }

    private static void draw(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context,
                             @NotNull List<BloomRenderTicket> tickets) {
        boolean initialized = false;
        @Nullable
        IRenderSetup renderSetup = null;
        for (BloomRenderTicket ticket : tickets) {
            if (!ticket.isValid() || !ticket.render.shouldRenderBloomEffect(context)) continue;
            if (!initialized) {
                initialized = true;
                renderSetup = ticket.renderSetup;
                if (renderSetup != null) {
                    renderSetup.preDraw(buffer);
                }
            }
            ticket.render.renderBloomEffect(buffer, context);
            if (ticket.legacy) ticket.invalidate();
        }
        if (initialized && renderSetup != null) {
            renderSetup.postDraw(buffer);
        }
    }

    private static void preDraw() {
        WorldClient world = Minecraft.getMinecraft().world;
        if (currentWorld != world) {
            for (List<BloomRenderTicket> list : BLOOM_RENDERS.values()) {
                for (BloomRenderTicket ticket : list) {
                    ticket.invalidate();
                }
            }
            BLOOM_RENDERS.clear();
            if (currentWorld != null) {
                for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
                    ticket.invalidate();
                }
                SCHEDULED_BLOOM_RENDERS.clear();
            }
            currentWorld = world;
        }

        for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
            if (!ticket.isValid()) continue;
            BLOOM_RENDERS.computeIfAbsent(new BloomRenderKey(ticket.renderSetup, ticket.bloomType),
                    k -> new ArrayList<>()).add(ticket);
        }
        SCHEDULED_BLOOM_RENDERS.clear();
    }

    private static void postDraw() {
        for (var it = BLOOM_RENDERS.values().iterator(); it.hasNext();) {
            List<BloomRenderTicket> list = it.next();

            if (!list.isEmpty()) {
                if (!list.removeIf(ticket -> !ticket.isValid()) || !list.isEmpty()) continue;
            }

            it.remove();
        }
    }

    @Desugar
    private record BloomRenderKey(@Nullable IRenderSetup renderSetup, @NotNull BloomType bloomType) {}

    public static final class BloomRenderTicket {

        @Nullable
        private final IRenderSetup renderSetup;
        private final BloomType bloomType;
        private final IBloomEffect render;

        private boolean valid = true;
        /**
         * Used to mark bloom tickets made by legacy API; this ticket will be automatically expired after 1 render call.
         * Remove this method in 2.9 as well as the deprecated method.
         */
        private boolean legacy;

        BloomRenderTicket(@Nullable IRenderSetup renderSetup, @NotNull BloomType bloomType,
                          @NotNull IBloomEffect render) {
            this.renderSetup = renderSetup;
            this.bloomType = Objects.requireNonNull(bloomType, "bloomType == null");
            this.render = Objects.requireNonNull(render, "render == null");
        }

        @Nullable
        public IRenderSetup getRenderSetup() {
            return this.renderSetup;
        }

        @NotNull
        public BloomType getBloomType() {
            return this.bloomType;
        }

        public boolean isValid() {
            return this.valid;
        }

        public void invalidate() {
            this.valid = false;
        }
    }

    /**
     * @deprecated use ticket-based bloom render hook
     *             {@link #registerBloomRender(IRenderSetup, BloomType, IBloomEffect)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public interface IBloomRenderFast extends IRenderSetup {

        /**
         * Custom Bloom Style.
         *
         * @return 0 - Simple Gaussian Blur Bloom
         *         <p>
         *         1 - Unity Bloom
         *         </p>
         *         <p>
         *         2 - Unreal Bloom
         *         </p>
         */
        int customBloomStyle();
    }
}
