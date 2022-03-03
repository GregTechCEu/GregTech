package gregtech.api.items.toolitem;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;

// Thanks to EnderIO
@SideOnly(Side.CLIENT)
public class ToolChargeBarRenderer {

    private static final double BAR_W = 12d;

    private static final Color colorShadow = new Color(0, 0, 0, 255);
    private static final Color colorBarLeft = new Color(0, 0, 139, 255);
    private static final Color colorBarRight = new Color(0, 255, 255, 255);
    private static final Color colorBG = new Color(0x0E, 0x01, 0x16, 255);

    public static void render(double level, int xPosition, int yPosition, int offset, boolean shadow) {
        double width = level * BAR_W;
        GlStateManager.enableLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        drawPlain(worldrenderer, xPosition + 2, yPosition + 13 - offset, 13, shadow ? 2 : 1);
        drawGrad(worldrenderer, xPosition + 2, yPosition + 13 - offset, (BAR_W + width) / 2);
        drawRight(worldrenderer, xPosition + 2 + (int) BAR_W, yPosition + 13 - offset, BAR_W - width);
        if (offset == 2) {
            overpaintVanillaRenderBug(worldrenderer, xPosition, yPosition);
        }
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    private static void drawGrad(BufferBuilder renderer, int x, int y, double width) {
        renderer.pos(x, y, 0.0D).color(colorBarLeft.getRed(), colorBarLeft.getGreen(), colorBarLeft.getBlue(), colorBarLeft.getAlpha()).endVertex();
        renderer.pos(x, y + (double) 1, 0.0D).color(colorBarLeft.getRed(), colorBarLeft.getGreen(), colorBarLeft.getBlue(), colorBarLeft.getAlpha()).endVertex();
        renderer.pos(x + width, y + (double) 1, 0.0D).color(colorBarRight.getRed(), colorBarRight.getGreen(), colorBarRight.getBlue(), colorBarRight.getAlpha()).endVertex();
        renderer.pos(x + width, y, 0.0D).color(colorBarRight.getRed(), colorBarRight.getGreen(), colorBarRight.getBlue(), colorBarRight.getAlpha()).endVertex();
    }

    private static void drawPlain(BufferBuilder renderer, int x, int y, double width, double height) {
        renderer.pos(x, y, 0.0D).color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha()).endVertex();
        renderer.pos(x, y + height, 0.0D).color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha()).endVertex();
        renderer.pos(x + width, y + height, 0.0D).color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha()).endVertex();
        renderer.pos(x + width, y, 0.0D).color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha()).endVertex();
    }

    private static void drawRight(BufferBuilder renderer, int x, int y, double width) {
        renderer.pos(x - width, y, 0.0D).color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
        renderer.pos(x - width, y + (double) 1, 0.0D).color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
        renderer.pos(x, y + (double) 1, 0.0D).color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
        renderer.pos(x, y, 0.0D).color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
    }

    public static void overpaintVanillaRenderBug(int xPosition, int yPosition) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        overpaintVanillaRenderBug(worldrenderer, xPosition, yPosition);
        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    public static void overpaintVanillaRenderBug(BufferBuilder worldrenderer, int xPosition, int yPosition) {
        drawPlain(worldrenderer, xPosition + 2 + 12, yPosition + 13, 1, 1);
    }

    private ToolChargeBarRenderer() { }

}
