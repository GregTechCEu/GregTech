package gregtech.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import org.lwjgl.opengl.GL11;

import java.util.Stack;

public class RenderUtil {

    private static Stack<int[]> scissorFrameStack = new Stack<>();

    public static void useScissor(int x, int y, int width, int height, Runnable codeBlock) {
        pushScissorFrame(x, y, width, height);
        try {
            codeBlock.run();
        } finally {
            popScissorFrame();
        }
    }

    private static int[] peekFirstScissorOrFullScreen() {
        int[] currentTopFrame = scissorFrameStack.isEmpty() ? null : scissorFrameStack.peek();
        if(currentTopFrame == null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            return new int[] {0, 0, minecraft.displayWidth, minecraft.displayHeight};
        }
        return currentTopFrame;
    }

    public static void pushScissorFrame(int x, int y, int width, int height) {
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];

        boolean pushedFrame = false;
        if(x <= parentX + parentWidth && y <= parentY + parentHeight) {
            int newX = x >= parentX ? x : parentX;
            int newY = y >= parentY ? y : parentY;
            int newWidth = width - (newX - x);
            int newHeight = height - (newY - y);
            if(newWidth > 0 && newHeight > 0) {
                int maxWidth = parentWidth - (x - parentX);
                int maxHeight = parentHeight - (y - parentY);
                newWidth = maxWidth > newWidth ? newWidth : maxWidth;
                newHeight = maxHeight > newHeight ? newHeight : maxHeight;
                applyScissor(newX, newY, newWidth, newHeight);
                //finally, push applied scissor on top of scissor stack
                if (scissorFrameStack.isEmpty()) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);
                }
                scissorFrameStack.push(new int[] {newX, newY, newWidth, newHeight});
                pushedFrame = true;
            }
        }
        if(!pushedFrame) {
            if (scissorFrameStack.isEmpty()) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
            scissorFrameStack.push(new int[] {parentX, parentY, parentWidth, parentHeight});
        }
    }

    public static void popScissorFrame() {
        scissorFrameStack.pop();
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];
        applyScissor(parentX, parentY, parentWidth, parentHeight);
        if (scissorFrameStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    //applies scissor with gui-space coordinates and sizes
    private static void applyScissor(int x, int y, int w, int h) {
        //translate upper-left to bottom-left
        ScaledResolution r = ((GuiIngameForge) Minecraft.getMinecraft().ingameGUI).getResolution();
        int s = r.getScaleFactor();
        int translatedY = r.getScaledHeight() - y - h;
        GL11.glScissor(x*s, translatedY*s, w*s, h*s);
    }


    public static void renderSlot(Slot slot, FontRenderer fr) {
        ItemStack stack = slot.getStack();
        if (!stack.isEmpty() && slot.isEnabled()) {
            net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1, 1, 0);
            GlStateManager.translate(slot.xPos, slot.yPos, 0);
            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);
            String text = stack.getCount() > 1? Integer.toString(stack.getCount()) : null;

            if (!stack.isEmpty())
            {
                if (stack.getCount() != 1)
                {
                    String s = text == null ? String.valueOf(stack.getCount()) : text;
                    GlStateManager.disableLighting();
                    GlStateManager.disableBlend();
                    fr.drawStringWithShadow(s, (float)(17 - fr.getStringWidth(s)), (float)9, 16777215);
                    GlStateManager.enableLighting();
                    GlStateManager.enableBlend();
                }

                if (stack.getItem().showDurabilityBar(stack))
                {
                    GlStateManager.disableLighting();
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableAlpha();
                    GlStateManager.disableBlend();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    double health = stack.getItem().getDurabilityForDisplay(stack);
                    int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
                    int i = Math.round(13.0F - (float)health * 13.0F);
                    draw(bufferbuilder, 2, 13, 13, 2, 0, 0, 0, 255);
                    draw(bufferbuilder, 2, 13, i, 1, rgbfordisplay >> 16 & 255, rgbfordisplay >> 8 & 255, rgbfordisplay & 255, 255);
                    GlStateManager.enableBlend();
                    GlStateManager.enableAlpha();
                    GlStateManager.enableTexture2D();
                    GlStateManager.enableLighting();
                }

                EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
                float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());

                if (f3 > 0.0F)
                {
                    GlStateManager.disableLighting();
                    GlStateManager.disableTexture2D();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferBuilder = tessellator.getBuffer();
                    draw(bufferBuilder, 0, MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
                    GlStateManager.enableTexture2D();
                    GlStateManager.enableLighting();
                }
            }

            GlStateManager.popMatrix();
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        }
    }

    private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha)
    {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((x), y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((x + width), y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((x + width), y, 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    public static void renderRect(float x, float y, float width, float height, float z, int color) {
        renderGradientRect(x, y, width, height, z, color, color, false);
    }

    public static void renderGradientRect(float x, float y, float width, float height, float z, int startColor, int endColor, boolean horizontal) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        if (horizontal) {
            buffer.pos(x + width, y, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.pos(x, y, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x, y + height, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x + width, y + height, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tessellator.draw();
        } else {
            buffer.pos(x + width, y, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x, y, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(x, y + height, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.pos(x + width, y + height, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tessellator.draw();
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }


}
