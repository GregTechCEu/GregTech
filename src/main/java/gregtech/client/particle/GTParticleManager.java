package gregtech.client.particle;

import gregtech.api.util.GTLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * //TODO - One day switch to using GPU instances for rendering when particle is under pressure.
 *
 * @Author: KilaBash
 * @Date: 2021/08/31
 * @Description: ParticleManger register, spawn, efficient rendering, update our custom particles.
 */
@SideOnly(Side.CLIENT)
public class GTParticleManager {
    public final static GTParticleManager INSTANCE = new GTParticleManager();

    private static World currentWorld = null;
    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueueBack = new HashMap<>();
    private final Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueueFront = new HashMap<>();
    private final Queue<Tuple<IGTParticleHandler, GTParticle>> newParticleQueue = new ArrayDeque<>();

    public void addEffect(GTParticle... particles) {
        for (GTParticle particle : particles) {
            if (particle.getGLHandler() != null) {
                newParticleQueue.add(new Tuple<>(particle.getGLHandler(), particle));
            } 
        }
    }

    public void updateEffects() {
        updateEffectLayer();
        if (!newParticleQueue.isEmpty()) {
            for (Tuple<IGTParticleHandler, GTParticle> handlerParticle = newParticleQueue.poll(); handlerParticle != null; handlerParticle = newParticleQueue.poll()) {
                IGTParticleHandler handler = handlerParticle.getFirst();
                GTParticle particle = handlerParticle.getSecond();
                Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueue = particle.getFXLayer() > 0 ? renderQueueFront : renderQueueBack;
                if (!renderQueue.containsKey(handler)) {
                    renderQueue.put(handler, new ArrayDeque<>());
                }
                ArrayDeque<GTParticle> arrayDeque = renderQueue.get(handler);
                if (arrayDeque.size() > 6000) {
                    arrayDeque.removeFirst().setExpired();
                }
                arrayDeque.add(particle);
            }
        }
    }

    private void updateEffectLayer() {
        if (!renderQueueBack.isEmpty()) {
            updateQueue(renderQueueBack);
        }
        if (!renderQueueFront.isEmpty()) {
            updateQueue(renderQueueFront);
        }
    }

    private void updateQueue(Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueue) {
        Iterator<Map.Entry<IGTParticleHandler, ArrayDeque<GTParticle>>> entryIterator = renderQueue.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<IGTParticleHandler, ArrayDeque<GTParticle>> entry = entryIterator.next();
            Iterator<GTParticle> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                tickParticle(particle);
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
            if (entry.getValue().isEmpty()) {
                entryIterator.remove();
            }
        }
    }

    public void clearAllEffects(boolean cleanNewQueue) {
        if (cleanNewQueue) {
            for (Tuple<IGTParticleHandler, GTParticle> tuple : newParticleQueue) {
                tuple.getSecond().setExpired();
            }
            newParticleQueue.clear();
        }
        for (ArrayDeque<GTParticle> particles : renderQueueBack.values()) {
            particles.forEach(Particle::setExpired);
        }
        for (ArrayDeque<GTParticle> particles : renderQueueFront.values()) {
            particles.forEach(Particle::setExpired);
        }
        renderQueueBack.clear();
        renderQueueFront.clear();
    }

    private void tickParticle(final Particle particle) {
        try {
            particle.onUpdate();
        }
        catch (Throwable throwable) {
            GTLog.logger.error("particle update error: {}", particle.toString(), throwable);
            particle.setExpired();
        }
    }

    public void renderParticles(Entity entityIn, float partialTicks) {
        if (renderQueueBack.isEmpty() && renderQueueFront.isEmpty()) return;
        float rotationX = ActiveRenderInfo.getRotationX();
        float rotationZ = ActiveRenderInfo.getRotationZ();
        float rotationYZ = ActiveRenderInfo.getRotationYZ();
        float rotationXY = ActiveRenderInfo.getRotationXY();
        float rotationXZ = ActiveRenderInfo.getRotationXZ();
        Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        Particle.cameraViewDir = entityIn.getLook(partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);

        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.disableLighting();

        renderGlParticlesInLayer(renderQueueBack, tessellator, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);

        GlStateManager.depthMask(false);
        renderGlParticlesInLayer(renderQueueFront, tessellator, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    }

    private void renderGlParticlesInLayer(Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueue, Tessellator tessellator, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        for (IGTParticleHandler handler : renderQueue.keySet()) {
            ArrayDeque<GTParticle> particles = renderQueue.get(handler);
            if (particles.isEmpty()) continue;
            BufferBuilder buffer = tessellator.getBuffer();
            handler.preDraw(buffer);
            for (final GTParticle particle : particles) {
                if (particle.shouldRendered(entityIn, partialTicks)) {
                    try {
                        particle.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
                    }
                    catch (Throwable throwable) {
                        GTLog.logger.error("particle render error: {}", particle.toString(), throwable);
                        particle.setExpired();
                    }
                }
            }
            handler.postDraw(buffer);
        }
    }

    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.isGamePaused()) {
            return;
        }

        if (currentWorld != mc.world) {
            INSTANCE.clearAllEffects(currentWorld != null);
            currentWorld = mc.world;
        }

        if (currentWorld != null) {
            INSTANCE.updateEffects();
        }
    }

    public static void renderWorld(RenderWorldLastEvent event) {
        Entity entity = mc.getRenderViewEntity();
        INSTANCE.renderParticles(entity == null ? mc.player : entity, event.getPartialTicks());
    }

    public static void debugOverlay(RenderGameOverlayEvent.Text event) {
        if (event.getLeft().size() >= 5) {
            String particleTxt = event.getLeft().get(4);
            particleTxt += "." + TextFormatting.GOLD + " PARTICLE-BACK: " + INSTANCE.getStatistics(INSTANCE.renderQueueBack) + "PARTICLE-FRONt: " + INSTANCE.getStatistics(INSTANCE.renderQueueFront);
            event.getLeft().set(4, particleTxt);
        }
    }

    public String getStatistics(Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueue) {
        int g = 0;
        for (ArrayDeque<GTParticle> queue : renderQueue.values()) {
            g += queue.size();
        }
        return " GLFX: " + g;
    }

}
