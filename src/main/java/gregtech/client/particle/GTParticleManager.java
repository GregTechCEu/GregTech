package gregtech.client.particle;

import gregtech.api.util.GTLog;
import gregtech.client.renderer.IRenderSetup;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Singleton class responsible for managing, updating and rendering {@link GTParticle} instances.
 */
@SideOnly(Side.CLIENT)
public class GTParticleManager {

    public static final GTParticleManager INSTANCE = new GTParticleManager();

    @Nullable
    private static World currentWorld = null;

    private final Map<IRenderSetup, ArrayDeque<GTParticle>> depthEnabledParticles = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<IRenderSetup, ArrayDeque<GTParticle>> depthDisabledParticles = new Object2ObjectLinkedOpenHashMap<>();

    private final List<GTParticle> newParticleQueue = new ArrayList<>();

    public void addEffect(@Nonnull GTParticle particles) {
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

                ArrayDeque<GTParticle> particles = queue.computeIfAbsent(particle.getRenderSetup(), setup -> new ArrayDeque<>());

                if (particles.size() > 6000) {
                    particles.removeFirst().setExpired();
                }
                particles.add(particle);
            }
            newParticleQueue.clear();
        }
    }

    private void updateQueue(Map<IRenderSetup, ArrayDeque<GTParticle>> renderQueue) {
        var entryIterator = renderQueue.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<IRenderSetup, ArrayDeque<GTParticle>> entry = entryIterator.next();
            ArrayDeque<GTParticle> particles = entry.getValue();

            Iterator<GTParticle> iterator = particles.iterator();
            while (iterator.hasNext()) {
                GTParticle particle = iterator.next();
                try {
                    particle.onUpdate();
                } catch (RuntimeException exception) {
                    GTLog.logger.error("particle update error: {}", particle.toString(), exception);
                    particle.setExpired();
                }
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }

            if (particles.isEmpty()) {
                entryIterator.remove();
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

    public void renderParticles(@Nonnull Entity renderViewEntity, float partialTicks) {
        if (depthEnabledParticles.isEmpty() && depthDisabledParticles.isEmpty()) return;

        double cameraX = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
        double cameraY = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
        double cameraZ = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
        Vec3d cameraViewDir = renderViewEntity.getLook(partialTicks);

        float rotationX = ActiveRenderInfo.getRotationX();
        float rotationZ = ActiveRenderInfo.getRotationZ();
        float rotationYZ = ActiveRenderInfo.getRotationYZ();
        float rotationXY = ActiveRenderInfo.getRotationXY();
        float rotationXZ = ActiveRenderInfo.getRotationXZ();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);

        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.disableLighting();

        if (!depthDisabledParticles.isEmpty()) {
            GlStateManager.depthMask(false);

            renderGlParticlesInLayer(depthDisabledParticles, tessellator, renderViewEntity,
                    partialTicks, cameraX, cameraY, cameraZ, cameraViewDir,
                    rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);

            GlStateManager.depthMask(true);
        }

        renderGlParticlesInLayer(depthEnabledParticles, tessellator, renderViewEntity,
                partialTicks, cameraX, cameraY, cameraZ, cameraViewDir,
                rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);

        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    }

    private static void renderGlParticlesInLayer(@Nonnull Map<IRenderSetup, ArrayDeque<GTParticle>> renderQueue,
                                                 @Nonnull Tessellator tessellator, @Nonnull Entity renderViewEntity,
                                                 float partialTicks, double cameraX, double cameraY, double cameraZ,
                                                 @Nonnull Vec3d cameraViewDir, float rotationX, float rotationZ,
                                                 float rotationYZ, float rotationXY, float rotationXZ) {
        for (var e : renderQueue.entrySet()) {
            IRenderSetup handler = e.getKey();
            ArrayDeque<GTParticle> particles = e.getValue();
            if (particles.isEmpty()) continue;
            BufferBuilder buffer = tessellator.getBuffer();
            if (handler != null) {
                handler.preDraw(buffer);
            }
            for (GTParticle particle : particles) {
                if (particle.shouldRender(renderViewEntity, partialTicks)) {
                    try {
                        particle.renderParticle(buffer, renderViewEntity,
                                partialTicks, cameraX, cameraY, cameraZ, cameraViewDir,
                                rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
                    } catch (Throwable throwable) {
                        GTLog.logger.error("particle render error: {}", particle.toString(), throwable);
                        particle.setExpired();
                    }
                }
            }
            if (handler != null) {
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

    private static int count(Map<IRenderSetup, ArrayDeque<GTParticle>> renderQueue) {
        int g = 0;
        for (Deque<GTParticle> queue : renderQueue.values()) {
            g += queue.size();
        }
        return g;
    }
}
