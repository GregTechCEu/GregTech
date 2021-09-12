package gregtech.api.render;

import gregtech.api.render.shader.Shaders;
import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/12
 * @Description: WorldProspectorRenderer. render blocks scanning scene in world.
 */
@SideOnly(Side.CLIENT)
public class WorldProspectorRenderer {

    public static void renderWorld(RenderWorldLastEvent event) {
        if (DepthTextureHook.framebufferDepthTexture != 0) {
            Minecraft mc = Minecraft.getMinecraft();
            World world = mc.world;
            Entity viewer = mc.getRenderViewEntity();
            if (world != null && viewer != null) {

                Framebuffer fbo = mc.getFramebuffer();

                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

                DepthTextureHook.bindDepthTexture();

                float time = (viewer.ticksExisted + event.getPartialTicks()) / 20;

                Shaders.renderFullImageInFBO(fbo, Shaders.SCANNING, uniformCache -> {
                    uniformCache.glUniform1F("u_time", time);
                    uniformCache.glUniform1F("radius", (time % 2) / 2 * 70f);
                    uniformCache.glUniform1F("u_zFar", mc.gameSettings.renderDistanceChunks * 16 * MathHelper.SQRT_2);
                    uniformCache.glUniform1F("u_FOV", mc.gameSettings.fovSetting);
                });

                DepthTextureHook.unBindDepthTexture();

                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.disableBlend();
                GlStateManager.depthMask(true);

            }
        }
    }
}
