package gregtech.api.gui.impl;

import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

@SideOnly(Side.CLIENT)
public class FakeModularGui implements IRenderContext {

    public final ModularUI modularUI;
    public FakeModularGuiContainer container;
    protected Minecraft mc;
    protected FontRenderer fr;

    public FakeModularGui(ModularUI modularUI, FakeModularGuiContainer fakeModularUIContainer) {
        this.modularUI = modularUI;
        this.container = fakeModularUIContainer;
        this.modularUI.updateScreenSize(this.modularUI.getWidth(), this.modularUI.getHeight());
        this.mc = Minecraft.getMinecraft();
        this.fr = mc.fontRenderer;
    }

    public void updateScreen() {
        modularUI.guiWidgets.values().forEach(Widget::updateScreen);
    }

    public void handleWidgetUpdate(int windowId, int widgetId, PacketBuffer updateData) {
        if (windowId == container.windowId) {
            Widget widget = modularUI.guiWidgets.get(widgetId);
            int updateId = updateData.readVarInt();
            if (widget != null) {
                widget.readUpdateInfo(updateId, updateData);
            }
        }
    }

    public void drawScreen(double x, double y, float partialTicks) {
        float halfW = modularUI.getWidth() / 2f;
        float halfH = modularUI.getHeight() / 2f;
        float scale = 0.5f / Math.max(halfW, halfH);
        int mouseX = (int) ((x / scale) + (halfW > halfH ? 0 : (halfW - halfH)));
        int mouseY = (int) ((y / scale) + (halfH > halfW ? 0 : (halfH - halfW)));
        GlStateManager.translate(-scale * halfW, -scale * halfH, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.color(modularUI.getRColorForOverlay(), modularUI.getGColorForOverlay(),
                modularUI.getBColorForOverlay(), 1.0F);
        modularUI.backgroundPath.draw(0, 0, modularUI.getWidth(), modularUI.getHeight());
        GlStateManager.translate(0, 0, 0.001);
        GlStateManager.depthMask(false);

        drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        for (int i = 0; i < this.container.inventorySlots.size(); ++i) {
            renderSlot(this.container.inventorySlots.get(i), fr);
        }

        GlStateManager.scale(1, 1, 0);
        drawGuiContainerForegroundLayer(mouseX, mouseY);

        for (int i = 0; i < this.container.inventorySlots.size(); ++i) {
            Slot slot = this.container.inventorySlots.get(i);
            if (!slot.getStack().isEmpty() && slot.xPos < mouseX && mouseX < slot.xPos + 18 && slot.yPos < mouseY &&
                    mouseY < slot.yPos + 18) {
                renderToolTip(slot.getStack(), slot.xPos, slot.yPos);
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        GlStateManager.disableLighting();
    }

    public static void renderSlot(Slot slot, FontRenderer fr) {
        ItemStack stack = slot.getStack();
        if (!stack.isEmpty() && slot.isEnabled()) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1, 1, 0.00001);
            GlStateManager.translate(slot.xPos, slot.yPos, 0);
            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);
            renderItem.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, stack, 0, 0, null);
            String text = stack.getCount() > 1 ? Integer.toString(stack.getCount()) : null;

            if (!stack.isEmpty()) {
                if (stack.getCount() != 1) {
                    String s = text == null ? String.valueOf(stack.getCount()) : text;
                    GlStateManager.disableLighting();
                    GlStateManager.disableBlend();
                    fr.drawStringWithShadow(s, (float) (17 - fr.getStringWidth(s)), (float) 9, 16777215);
                    GlStateManager.enableLighting();
                    GlStateManager.enableBlend();
                }

                if (stack.getItem().showDurabilityBar(stack)) {
                    GlStateManager.disableLighting();
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableAlpha();
                    GlStateManager.disableBlend();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    double health = stack.getItem().getDurabilityForDisplay(stack);
                    int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
                    int i = Math.round(13.0F - (float) health * 13.0F);
                    draw(bufferbuilder, 2, 13, 13, 2, 0, 0, 0, 255);
                    draw(bufferbuilder, 2, 13, i, 1, rgbfordisplay >> 16 & 255, rgbfordisplay >> 8 & 255,
                            rgbfordisplay & 255, 255);
                    GlStateManager.enableBlend();
                    GlStateManager.enableAlpha();
                    GlStateManager.enableTexture2D();
                    GlStateManager.enableLighting();
                }

                EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
                float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker()
                        .getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());

                if (f3 > 0.0F) {
                    GlStateManager.disableLighting();
                    GlStateManager.disableTexture2D();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferBuilder = tessellator.getBuffer();
                    draw(bufferBuilder, 0, MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255,
                            255, 255, 127);
                    GlStateManager.enableTexture2D();
                    GlStateManager.enableLighting();
                }
            }

            GlStateManager.popMatrix();
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        }
    }

    private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue,
                             int alpha) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((x), y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((x + width), y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((x + width), y, 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    protected void renderToolTip(ItemStack stack, int x, int y) {
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.preItemToolTip(stack);
        GuiUtils.drawHoveringText(this.getItemToolTip(stack), x, y, modularUI.getScreenWidth(),
                modularUI.getScreenHeight(), -1, (font == null ? fr : font));
        net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
    }

    protected List<String> getItemToolTip(ItemStack itemStack) {
        List<String> list = itemStack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ?
                ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
        list.set(0, itemStack.getItem().getForgeRarity(itemStack).getColor() + list.get(0));
        for (int i = 1; i < list.size(); ++i) {
            list.set(i, TextFormatting.GRAY + list.get(i));
        }
        return list;
    }

    public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(modularUI.getRColorForOverlay(), modularUI.getGColorForOverlay(),
                modularUI.getBColorForOverlay(), 1.0F);
        for (Widget widget : modularUI.guiWidgets.values()) {
            GlStateManager.pushMatrix();
            GlStateManager.color(modularUI.getRColorForOverlay(), modularUI.getGColorForOverlay(),
                    modularUI.getBColorForOverlay());
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            widget.drawInBackground(mouseX, mouseY, partialTicks, this);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        for (Widget widget : modularUI.guiWidgets.values()) {
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            widget.drawInForeground(mouseX, mouseY);
            GlStateManager.popMatrix();
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if (widget.isVisible() && widget.isActive() && !(widget instanceof INativeWidget) &&
                    widget.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
    }
}
