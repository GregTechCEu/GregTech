package gregtech.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReportedException;
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
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/31
 * @Description: ParticleManger register, spawn, efficient rendering, update our custom particles.
 */
@SideOnly(Side.CLIENT)
public class GTParticleManager {
    public final static GTParticleManager INSTANCE = new GTParticleManager();

    private static World currentWorld = null;
    private static ParticleManager particleManager = Minecraft.getMinecraft().effectRenderer;

    private final Map<IGTParticleHandler, ArrayDeque<GTParticle>[]> renderQueue = new HashMap<>();
    private final Queue<Tuple<IGTParticleHandler, GTParticle>> newParticleQueue = new ArrayDeque<>();

    public void addEffect(Particle... particles) {
        if (particleManager == null) {
            particleManager = Minecraft.getMinecraft().effectRenderer;
        }
        for (Particle particle : particles) {
            if (particle instanceof GTParticle && ((GTParticle) particle).getGLHandler() != null) {
                newParticleQueue.add(new Tuple<>(((GTParticle) particle).getGLHandler(), (GTParticle)particle));
            } else {
                particleManager.addEffect(particle);
            }
        }
    }

    public void updateEffects() {
        updateEffectLayer();
        if (!newParticleQueue.isEmpty()) {
            for (Tuple<IGTParticleHandler, GTParticle> handlerParticle = newParticleQueue.poll(); handlerParticle != null; handlerParticle = newParticleQueue.poll()) {
                IGTParticleHandler handler = handlerParticle.getFirst();
                GTParticle particle = handlerParticle.getSecond();
                if (!renderQueue.containsKey(handler)) {
                    renderQueue.put(handler, new ArrayDeque[]{new ArrayDeque(), new ArrayDeque(), new ArrayDeque(), new ArrayDeque()});
                }
                ArrayDeque<GTParticle>[] arrayDeques = renderQueue.get(handler);
                int layer = particle.getFXLayer();
                if (arrayDeques[layer].size() > 6000) {
                    arrayDeques[layer].removeFirst().setExpired();
                }
                arrayDeques[layer].add(particle);
            }
        }
    }

    private void updateEffectLayer() {
        if (renderQueue.isEmpty()) return;
        boolean empty = true;
        for (ArrayDeque<GTParticle>[] array : renderQueue.values()) {
            for (int layer = 0; layer < 4; ++layer) {
                ArrayDeque<GTParticle> queue = array[layer];
                if (!queue.isEmpty()) {
                    empty = false;
                    Iterator<GTParticle> iterator = queue.iterator();
                    while (iterator.hasNext()) {
                        Particle particle = iterator.next();
                        tickParticle(particle);
                        if (!particle.isAlive()) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
        if (empty) {
            renderQueue.clear();
        }
    }

    public void clearAllEffects() {
        renderQueue.clear();
        newParticleQueue.clear();
    }

    private void tickParticle(final Particle particle) {
        try {
            particle.onUpdate();
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
            final int i = particle.getFXLayer();
            crashreportcategory.addCrashSection("Particle", new Callable<String>() {
                public String call() {
                    return particle.toString();
                }
            });
            crashreportcategory.addCrashSection("Particle Type", new Callable<String>() {
                public String call() {
                    return i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i));
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    public void renderParticles(Entity entityIn, float partialTicks) {
        if (renderQueue.isEmpty()) return;
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

        renderGlParticlesInLayer(tessellator, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    }

    private void renderGlParticlesInLayer(Tessellator tessellator, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        for (int layer = 0; layer < 4; layer++) {
            for (IGTParticleHandler handler : renderQueue.keySet()) {
                ArrayDeque<GTParticle> particles = renderQueue.get(handler)[layer];
                if (particles.isEmpty()) continue;
                BufferBuilder buffer = tessellator.getBuffer();
                handler.preDraw(buffer);
                for (final Particle particle : particles) {
                    try {
                        particle.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
                    }
                    catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                        crashreportcategory.addCrashSection("Particle", new Callable<String>() {
                            public String call() {
                                return particle.toString();
                            }
                        });
                        throw new ReportedException(crashreport);
                    }
                }
                handler.postDraw(buffer);
            }
        }
    }

    public String getStatistics() {
        int g = 0;
        for (ArrayDeque<GTParticle>[] array : renderQueue.values()) {
            for (ArrayDeque<GTParticle> queue : array) {
                g += queue.size();
            }
        }
        return " GLFX: " + g;
    }

    public static void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || mc.isGamePaused()) {
            return;
        }

        if (currentWorld != mc.world) {
            currentWorld = mc.world;
            INSTANCE.clearAllEffects();
        }

        if (mc.world != null) {
            INSTANCE.updateEffects();
        }
    }

    public static void renderWorld(RenderWorldLastEvent event) {
        INSTANCE.renderParticles(Minecraft.getMinecraft().player, event.getPartialTicks());
    }

    public static void debugOverlay(RenderGameOverlayEvent.Text event) {
        if (event.getLeft().size() >= 5) {
            String particleTxt = event.getLeft().get(4);
            particleTxt += "." + TextFormatting.GOLD + " BC-P: " + INSTANCE.getStatistics();
            event.getLeft().set(4, particleTxt);
        }
    }
}
