package gregtech.client.utils;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.Mods;
import gregtech.client.particle.GTParticle;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.Shaders;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class BloomEffectUtil {

    private static final Map<BloomRenderKey, List<BloomRenderTicket>> BLOOM_RENDERS = new Object2ObjectOpenHashMap<>();
    private static final List<BloomRenderTicket> SCHEDULED_BLOOM_RENDERS = new ArrayList<>();

    private static final ReentrantLock BLOOM_RENDER_LOCK = new ReentrantLock();

    private static BlockRenderLayer bloom;
    private static Framebuffer bloomFBO;

    /**
     * @return {@link BlockRenderLayer} instance for the bloom render layer.
     */
    @NotNull
    public static BlockRenderLayer getBloomLayer() {
        return Objects.requireNonNull(bloom, "Bloom effect is not initialized yet");
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, {@link BlockRenderLayer#CUTOUT} is returned instead.
     *
     * @return {@link BlockRenderLayer} instance for the bloom render layer, or {@link BlockRenderLayer#CUTOUT} if bloom
     *         layer is disabled
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
        return Mods.ShadersMod.isModLoaded() ? fallback : bloom;
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, {@link BlockRenderLayer#CUTOUT} is returned instead.
     *
     * @param isBloomActive Whether bloom layer should be active. If this value is {@code false}, {@code fallback} layer
     *                      will be returned. Has no effect if Optifine is present.
     * @return {@link BlockRenderLayer} instance for the bloom render layer, or {@link BlockRenderLayer#CUTOUT} if bloom
     *         layer is disabled
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
        return Mods.ShadersMod.isModLoaded() || !isBloomActive ? fallback : bloom;
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
     * Register a custom bloom render callback for subsequent world render. The render call persists until the
     * {@code metaTileEntity} is invalidated, or the world associated with {@code metaTileEntity} or the ticket is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}.
     * </p>
     * <p>
     * This method does not register bloom render ticket when Optifine is present, and an invalid ticket will be
     * returned instead.
     * </p>
     *
     * @param setup          Render setup, if exists
     * @param bloomType      Type of the bloom
     * @param render         Rendering callback
     * @param metaTileEntity Meta tile entity instance
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null || metaTileEntity == null}
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull BloomType bloomType,
                                                        @NotNull IBloomEffect render,
                                                        @NotNull MetaTileEntity metaTileEntity) {
        Objects.requireNonNull(metaTileEntity, "metaTileEntity == null");
        return registerBloomRender(setup, bloomType,
                new IBloomEffect() {

                    @Override
                    public void renderBloomEffect(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {
                        render.renderBloomEffect(buffer, context);
                    }

                    @Override
                    public boolean shouldRenderBloomEffect(@NotNull EffectRenderContext context) {
                        return metaTileEntity.getWorld() == context.renderViewEntity().world &&
                                render.shouldRenderBloomEffect(context);
                    }
                },
                t -> metaTileEntity.isValid(),
                metaTileEntity::getWorld);
    }

    /**
     * <p>
     * Register a custom bloom render callback for subsequent world render. The render call persists until the
     * {@code particle} is invalidated, or the ticket is manually freed by calling
     * {@link BloomRenderTicket#invalidate()}.
     * </p>
     * <p>
     * This method does not register bloom render ticket when Optifine is present, and an invalid ticket will be
     * returned instead.
     * </p>
     *
     * @param setup     Render setup, if exists
     * @param bloomType Type of the bloom
     * @param render    Rendering callback
     * @param particle  Particle instance
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null || metaTileEntity == null}
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull BloomType bloomType,
                                                        @NotNull IBloomEffect render,
                                                        @NotNull GTParticle particle) {
        Objects.requireNonNull(particle, "particle == null");
        return registerBloomRender(setup, bloomType, render, t -> particle.isAlive());
    }

    /**
     * <p>
     * Register a custom bloom render callback for subsequent world render. The render call persists until it is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}, or invalidated by validity checker.
     * </p>
     * <p>
     * This method does not register bloom render ticket when Optifine is present, and an invalid ticket will be
     * returned instead.
     * </p>
     *
     * @param setup           Render setup, if exists
     * @param bloomType       Type of the bloom
     * @param render          Rendering callback
     * @param validityChecker Optional validity checker; returning {@code false} causes the ticket to be invalidated.
     *                        Checked on both pre/post render each frame.
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null}
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, MetaTileEntity)
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, GTParticle)
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, Predicate, Supplier)
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull BloomType bloomType,
                                                        @NotNull IBloomEffect render,
                                                        @Nullable Predicate<BloomRenderTicket> validityChecker) {
        return registerBloomRender(setup, bloomType, render, validityChecker, null);
    }

    /**
     * <p>
     * Register a custom bloom render callback for subsequent world render. The render call persists until it is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}, or invalidated by validity checker.
     * </p>
     * <p>
     * This method does not register bloom render ticket when Optifine is present, and an invalid ticket will be
     * returned instead.
     * </p>
     *
     * @param setup           Render setup, if exists
     * @param bloomType       Type of the bloom
     * @param render          Rendering callback
     * @param validityChecker Optional validity checker; returning {@code false} causes the ticket to be invalidated.
     *                        Checked on both pre/post render each frame.
     * @param worldContext    Optional world bound to the ticket. If the world returned is not null, the bloom ticket
     *                        will be automatically invalidated on world unload. If world context returns {@code null},
     *                        it will not be affected by aforementioned automatic invalidation.
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null}
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, MetaTileEntity)
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, GTParticle)
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull BloomType bloomType,
                                                        @NotNull IBloomEffect render,
                                                        @Nullable Predicate<BloomRenderTicket> validityChecker,
                                                        @Nullable Supplier<World> worldContext) {
        if (Mods.ShadersMod.isModLoaded()) return BloomRenderTicket.INVALID;
        BloomRenderTicket ticket = new BloomRenderTicket(setup, bloomType, render, validityChecker, worldContext);
        BLOOM_RENDER_LOCK.lock();
        try {
            SCHEDULED_BLOOM_RENDERS.add(ticket);
        } finally {
            BLOOM_RENDER_LOCK.unlock();
        }
        return ticket;
    }

    /**
     * Invalidate tickets associated with given world.
     *
     * @param world World
     */
    public static void invalidateWorldTickets(@NotNull World world) {
        Objects.requireNonNull(world, "world == null");
        BLOOM_RENDER_LOCK.lock();
        try {
            for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
                if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == world) {
                    ticket.invalidate();
                }
            }

            for (Map.Entry<BloomRenderKey, List<BloomRenderTicket>> e : BLOOM_RENDERS.entrySet()) {
                for (BloomRenderTicket ticket : e.getValue()) {
                    if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == world) {
                        ticket.invalidate();
                    }
                }
            }
        } finally {
            BLOOM_RENDER_LOCK.unlock();
        }
    }

    public static void init() {
        bloom = BlockRenderLayer.valueOf("BLOOM");
    }

    public static int renderBloomBlockLayer(RenderGlobal renderGlobal,
                                            BlockRenderLayer blockRenderLayer, // 70% sure it's translucent uh yeah
                                            double partialTicks,
                                            int pass,
                                            @NotNull Entity entity) {
        Minecraft.getMinecraft().profiler.endStartSection("BTLayer");

        if (Mods.ShadersMod.isModLoaded()) {
            return renderGlobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);
        }

        BLOOM_RENDER_LOCK.lock();
        try {
            return renderBloomInternal(renderGlobal, blockRenderLayer, partialTicks, pass, entity);
        } finally {
            BLOOM_RENDER_LOCK.unlock();
        }
    }

    private static int renderBloomInternal(RenderGlobal renderGlobal,
                                           BlockRenderLayer blockRenderLayer,
                                           double partialTicks,
                                           int pass,
                                           @NotNull Entity entity) {
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

        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();

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
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        int result = renderGlobal.renderBlockLayer(blockRenderLayer, partialTicks, pass, entity);

        Minecraft.getMinecraft().profiler.endStartSection("bloom");

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

    private static void preDraw() {
        for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
            if (!ticket.isValid()) continue;
            BLOOM_RENDERS.computeIfAbsent(new BloomRenderKey(ticket.renderSetup, ticket.bloomType),
                    k -> new ArrayList<>()).add(ticket);
        }
        SCHEDULED_BLOOM_RENDERS.clear();
    }

    private static void draw(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context,
                             @NotNull List<BloomRenderTicket> tickets) {
        boolean initialized = false;
        @Nullable
        IRenderSetup renderSetup = null;
        for (BloomRenderTicket ticket : tickets) {
            ticket.checkValidity();
            if (!ticket.isValid() || !ticket.render.shouldRenderBloomEffect(context)) continue;
            if (!initialized) {
                initialized = true;
                renderSetup = ticket.renderSetup;
                if (renderSetup != null) {
                    renderSetup.preDraw(buffer);
                }
            }
            ticket.render.renderBloomEffect(buffer, context);
        }
        if (initialized && renderSetup != null) {
            renderSetup.postDraw(buffer);
        }
    }

    private static void postDraw() {
        for (var it = BLOOM_RENDERS.values().iterator(); it.hasNext();) {
            List<BloomRenderTicket> list = it.next();

            if (!list.isEmpty()) {
                if (!list.removeIf(ticket -> {
                    ticket.checkValidity();
                    return !ticket.isValid();
                }) || !list.isEmpty()) continue;
            }

            it.remove();
        }
    }

    @Desugar
    private record BloomRenderKey(@Nullable IRenderSetup renderSetup, @NotNull BloomType bloomType) {}

    public static final class BloomRenderTicket {

        public static final BloomRenderTicket INVALID = new BloomRenderTicket();

        @Nullable
        private final IRenderSetup renderSetup;
        private final BloomType bloomType;
        private final IBloomEffect render;
        @Nullable
        private final Predicate<BloomRenderTicket> validityChecker;
        @Nullable
        private final Supplier<World> worldContext;

        private boolean invalidated;

        BloomRenderTicket() {
            this(null, BloomType.DISABLED, (b, c) -> {}, null, null);
            this.invalidated = true;
        }

        BloomRenderTicket(@Nullable IRenderSetup renderSetup, @NotNull BloomType bloomType,
                          @NotNull IBloomEffect render, @Nullable Predicate<BloomRenderTicket> validityChecker,
                          @Nullable Supplier<World> worldContext) {
            this.renderSetup = renderSetup;
            this.bloomType = Objects.requireNonNull(bloomType, "bloomType == null");
            this.render = Objects.requireNonNull(render, "render == null");
            this.validityChecker = validityChecker;
            this.worldContext = worldContext;
        }

        public boolean isValid() {
            return !this.invalidated;
        }

        public void invalidate() {
            this.invalidated = true;
        }

        private void checkValidity() {
            if (!this.invalidated && this.validityChecker != null && !this.validityChecker.test(this)) {
                invalidate();
            }
        }
    }
}
