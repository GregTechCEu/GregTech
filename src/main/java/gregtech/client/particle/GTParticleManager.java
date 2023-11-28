package gregtech.client.particle;

import gregtech.api.util.GTLog;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.utils.EffectRenderContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Singleton class responsible for managing, updating and rendering {@link GTParticle} instances.
 */
@SideOnly(Side.CLIENT)
public class GTParticleManager {

    public static final GTParticleManager INSTANCE = new GTParticleManager();

    @Nullable
    private static World currentWorld = null;

    private final Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> depthEnabledParticles = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> depthDisabledParticles = new Object2ObjectLinkedOpenHashMap<>();

    private final List<GTParticle> newParticleQueue = new ArrayList<>();

    public void addEffect(@NotNull GTParticle particles) {
        newParticleQueue.add(particles);
    }

    public void updateEffects() {
        if (!depthEnabledParticles.isEmpty()) {
            updateQueue(depthEnabledParticles);
        }
        if (!depthDisabledParticles.isEmpty()) {
            updateQueue(depthDisabledParticles);
        }
        if (!newParticleQueue.isEmpty()) {
            for (GTParticle particle : newParticleQueue) {
                var queue = particle.shouldDisableDepth() ? depthDisabledParticles : depthEnabledParticles;

                ArrayDeque<GTParticle> particles = queue.computeIfAbsent(particle.getRenderSetup(),
                        setup -> new ArrayDeque<>());

                if (particles.size() > 6000) {
                    particles.removeFirst().setExpired();
                }
                particles.add(particle);
            }
            newParticleQueue.clear();
        }
    }

    private void updateQueue(Map<IRenderSetup, ArrayDeque<GTParticle>> renderQueue) {
        Iterator<ArrayDeque<GTParticle>> it = renderQueue.values().iterator();
        while (it.hasNext()) {
            ArrayDeque<GTParticle> particles = it.next();

            Iterator<GTParticle> it2 = particles.iterator();
            while (it2.hasNext()) {
                GTParticle particle = it2.next();
                if (particle.isAlive()) {
                    try {
                        particle.onUpdate();
                    } catch (RuntimeException exception) {
                        GTLog.logger.error("particle update error: {}", particle.toString(), exception);
                        particle.setExpired();
                    }
                    if (particle.isAlive()) continue;
                }
                it2.remove();
            }

            if (particles.isEmpty()) {
                it.remove();
            }
        }
    }

    public void clearAllEffects(boolean cleanNewQueue) {
        if (cleanNewQueue) {
            for (GTParticle particle : newParticleQueue) {
                particle.setExpired();
            }
            newParticleQueue.clear();
        }
        for (ArrayDeque<GTParticle> particles : depthEnabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        for (ArrayDeque<GTParticle> particles : depthDisabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        depthEnabledParticles.clear();
        depthDisabledParticles.clear();
    }

    public void renderParticles(@NotNull Entity renderViewEntity, float partialTicks) {
        if (depthEnabledParticles.isEmpty() && depthDisabledParticles.isEmpty()) return;

        EffectRenderContext instance = EffectRenderContext.getInstance().update(renderViewEntity, partialTicks);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);

        GlStateManager.disableLighting();

        if (!depthDisabledParticles.isEmpty()) {
            GlStateManager.depthMask(false);

            renderGlParticlesInLayer(depthDisabledParticles, instance);

            GlStateManager.depthMask(true);
        }

        renderGlParticlesInLayer(depthEnabledParticles, instance);

        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    }

    private static void renderGlParticlesInLayer(@NotNull Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> renderQueue,
                                                 @NotNull EffectRenderContext context) {
        for (var e : renderQueue.entrySet()) {
            @Nullable
            IRenderSetup handler = e.getKey();
            ArrayDeque<GTParticle> particles = e.getValue();
            if (particles.isEmpty()) continue;

            boolean initialized = false;
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            for (GTParticle particle : particles) {
                if (particle.shouldRender(context)) {
                    try {
                        if (!initialized) {
                            initialized = true;
                            if (handler != null) {
                                handler.preDraw(buffer);
                            }
                        }
                        particle.renderParticle(buffer, context);
                    } catch (Throwable throwable) {
                        GTLog.logger.error("particle render error: {}", particle.toString(), throwable);
                        particle.setExpired();
                    }
                }
            }
            if (initialized && handler != null) {
                handler.postDraw(buffer);
            }
        }
    }

    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().isGamePaused()) {
            return;
        }

        WorldClient world = Minecraft.getMinecraft().world;
        if (currentWorld != world) {
            INSTANCE.clearAllEffects(currentWorld != null);
            currentWorld = world;
        }

        if (currentWorld != null) {
            INSTANCE.updateEffects();
        }
    }

    public static void renderWorld(RenderWorldLastEvent event) {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        INSTANCE.renderParticles(entity == null ? Minecraft.getMinecraft().player : entity, event.getPartialTicks());
    }

    public static void debugOverlay(RenderGameOverlayEvent.Text event) {
        if (event.getLeft().size() >= 5) {
            String particleTxt = event.getLeft().get(4);
            particleTxt += "." + TextFormatting.GOLD +
                    " PARTICLE-BACK: " + count(INSTANCE.depthEnabledParticles) +
                    "PARTICLE-FRONT: " + count(INSTANCE.depthDisabledParticles);
            event.getLeft().set(4, particleTxt);
        }
    }

    private static int count(Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> renderQueue) {
        int g = 0;
        for (Deque<GTParticle> queue : renderQueue.values()) {
            g += queue.size();
        }
        return g;
    }
}
