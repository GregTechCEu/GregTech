package gregtech.api.gui.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.common.gui.impl.FakeModularUIContainerClipboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Optional;

import static gregtech.api.gui.impl.ModularUIGui.*;

@SideOnly(Side.CLIENT)
public class FakeModularGui implements IRenderContext {
    public final ModularUI modularUI;
    public FakeModularUIContainerClipboard container;
    protected Minecraft mc;
    protected FontRenderer fr;

    public FakeModularGui(ModularUI modularUI, FakeModularUIContainerClipboard fakeModularUIContainer){
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
            Widget widget = modularUI.guiWidgets.get(Optional.of(widgetId));
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
        int mouseX = (int) ((x / scale) + (halfW > halfH? 0: (halfW - halfH)));
        int mouseY = (int) ((y / scale) + (halfH > halfW? 0: (halfH - halfW)));

        GlStateManager.translate(-scale * halfW, -scale * halfH, 0.01);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.depthMask(false);

        drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        GlStateManager.scale(1, 1, 0);
        drawGuiContainerForegroundLayer(mouseX, mouseY);

        for (int i = 0; i < this.container.inventorySlots.size(); ++i) {
            Slot slot = this.container.inventorySlots.get(i);
            if (!slot.getStack().isEmpty() && slot.xPos < mouseX && mouseX < slot.xPos + 18 && slot.yPos < mouseY && mouseY < slot.yPos + 18) {
                Widget.drawSolidRect(slot.xPos, slot.yPos, 18, 18, 0X8fffffff);
                renderToolTip(slot.getStack(), slot.xPos, slot.yPos);
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        GlStateManager.disableLighting();
    }

    protected void renderToolTip(ItemStack stack, int x, int y) {
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.preItemToolTip(stack);
        GuiUtils.drawHoveringText(this.getItemToolTip(stack), x, y, modularUI.getScreenWidth(), modularUI.getScreenHeight(), -1, (font == null ? fr : font));
        net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
    }

    protected List<String> getItemToolTip(ItemStack itemStack) {
        List<String> list = itemStack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
        list.set(0, itemStack.getItem().getForgeRarity(itemStack).getColor() + list.get(0));
        for (int i = 1; i < list.size(); ++i) {
            list.set(i, TextFormatting.GRAY + list.get(i));
        }
        return list;
    }

    public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
        modularUI.backgroundPath.draw(0, 0, modularUI.getWidth(), modularUI.getHeight());
        for (Widget widget : modularUI.guiWidgets.values()) {
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            GlStateManager.enableBlend();
            widget.drawInBackground(mouseX, mouseY, this);
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

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return modularUI.guiWidgets.values().stream().anyMatch(widget -> widget.mouseClicked(mouseX, mouseY, mouseButton));
    }
}
