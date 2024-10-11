package gregtech.client.renderer.handler;

import gregtech.client.utils.RenderBufferHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlockPosHighlightRenderer {

    private static BlockPos posHighLight;
    private static long duration;
    private static long offset;
    private static long start;

    public static void renderBlockBoxHighLight(BlockPos blockpos, long durTimeMillis) {
        posHighLight = blockpos;
        duration = durTimeMillis;
        offset = 1500;
        start = System.currentTimeMillis();
    }

    public static void renderBlockBoxHighLight(BlockPos blockpos, long durTimeMillis, long offsetTimeMillis) {
        posHighLight = blockpos;
        duration = durTimeMillis;
        offset = offsetTimeMillis;
        start = System.currentTimeMillis();
    }

    public static void renderWorldLastEvent(RenderWorldLastEvent evt) {
        if (posHighLight != null) {
            long time = System.currentTimeMillis();
            if (time > duration + start) {
                posHighLight = null;
                duration = 0;
                return;
            }
            if (time % offset >= offset / 2) {
                return;
            }
            EntityPlayerSP p = Minecraft.getMinecraft().player;
            double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.getPartialTicks();
            double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.getPartialTicks();
            double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.getPartialTicks();

            GlStateManager.pushMatrix();
            GlStateManager.color(1.0f, 0, 0);
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            RenderBufferHelper.renderCubeFace(buffer, posHighLight.getX(), posHighLight.getY(), posHighLight.getZ(),
                    posHighLight.getX() + 1, posHighLight.getY() + 1, posHighLight.getZ() + 1, 1.0f, 0.0f, 0.0f, 0.8f);

            tessellator.draw();

            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.color(1, 1, 1);
            GlStateManager.popMatrix();
        }
    }
}
