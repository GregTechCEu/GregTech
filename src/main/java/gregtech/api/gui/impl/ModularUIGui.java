package gregtech.api.gui.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.IScissored;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.net.PacketUIWidgetUpdate;
import gregtech.api.util.RenderUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Rectangle;
import java.io.IOException;

public class ModularUIGui extends GuiContainer implements IRenderContext {

    private final ModularUI modularUI;
    private static float rColorForOverlay = ((ConfigHolder.U.GT5u.defaultPaintingColor >> 16) & 0xff) / 255.0F;
    private static float gColorForOverlay = ((ConfigHolder.U.GT5u.defaultPaintingColor >> 8) & 0xff) / 255.0F;
    private static float bColorForOverlay = (ConfigHolder.U.GT5u.defaultPaintingColor & 0xff) / 255.0F;

    public ModularUI getModularUI() {
        return modularUI;
    }

    public ModularUIGui(ModularUI modularUI) {
        super(new ModularUIContainer(modularUI));
        this.modularUI = modularUI;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.xSize = modularUI.getWidth();
        this.ySize = modularUI.getHeight();
        super.initGui();
        this.modularUI.updateScreenSize(width, height);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        modularUI.guiWidgets.values().forEach(Widget::updateScreen);
    }

    public void handleWidgetUpdate(PacketUIWidgetUpdate packet) {
        if (packet.windowId == inventorySlots.windowId) {
            Widget widget = modularUI.guiWidgets.get(packet.widgetId);
            int updateId = packet.updateData.readVarInt();
            if (widget != null) {
                widget.readUpdateInfo(updateId, packet.updateData);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hoveredSlot = null;
        drawDefaultBackground();

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
            Slot slot = this.inventorySlots.inventorySlots.get(i);
            Rectangle scissor = null;
            if (slot instanceof IScissored) {
                scissor = ((IScissored) slot).getScissor();
                if (scissor != null) {
                    RenderUtil.pushScissorFrame(scissor.x, scissor.y, scissor.width, scissor.height);
                }
            }
            if (slot.isEnabled()) {
                this.drawSlotContents(slot);
            }
            if (isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY) && slot.isEnabled()) {
                renderSlotOverlay(slot);
                setHoveredSlot(slot);
            }
            if (scissor != null) {
                RenderUtil.popScissorFrame();
            }
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();

        drawGuiContainerForegroundLayer(mouseX, mouseY);

        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0F);
        RenderHelper.enableGUIStandardItemLighting();

        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawForeground(this, mouseX, mouseY));

        GlStateManager.enableDepth();
        renderItemStackOnMouse(mouseX, mouseY);
        renderReturningItemStack();

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();

        renderHoveredToolTip(mouseX, mouseY);
    }


    public void setHoveredSlot(Slot hoveredSlot) {
        this.hoveredSlot = hoveredSlot;
    }

    public void drawSlotContents(Slot slot) {
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawSlot(slot);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
    }

    public void renderSlotOverlay(Slot slot) {
        GlStateManager.disableDepth();
        int slotX = slot.xPos;
        int slotY = slot.yPos;
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
    }

    private void renderItemStackOnMouse(int mouseX, int mouseY) {
        InventoryPlayer inventory = this.mc.player.inventory;
        ItemStack itemStack = this.draggedStack.isEmpty() ? inventory.getItemStack() : this.draggedStack;

        if (!itemStack.isEmpty()) {
            int dragOffset = this.draggedStack.isEmpty() ? 8 : 16;
            if (!this.draggedStack.isEmpty() && this.isRightMouseClick) {
                itemStack = itemStack.copy();
                itemStack.setCount(MathHelper.ceil((float) itemStack.getCount() / 2.0F));

            } else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
                itemStack = itemStack.copy();
                itemStack.setCount(this.dragSplittingRemnant);
            }
            this.drawItemStack(itemStack, mouseX - guiLeft - 8, mouseY - guiTop - dragOffset, null);
        }
    }

    private void renderReturningItemStack() {
        if (!this.returningStack.isEmpty()) {
            float partialTicks = (float) (Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;
            if (partialTicks >= 1.0F) {
                partialTicks = 1.0F;
                this.returningStack = ItemStack.EMPTY;
            }
            int deltaX = this.returningStackDestSlot.xPos - this.touchUpX;
            int deltaY = this.returningStackDestSlot.yPos - this.touchUpY;
            int currentX = this.touchUpX + (int) ((float) deltaX * partialTicks);
            int currentY = this.touchUpY + (int) ((float) deltaY * partialTicks);
            //noinspection ConstantConditions
            this.drawItemStack(this.returningStack, currentX, currentY, null);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        modularUI.guiWidgets.values().forEach(widget -> {
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            widget.drawInForeground(mouseX, mouseY);
            GlStateManager.popMatrix();
        });
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
        modularUI.backgroundPath.draw(guiLeft, guiTop, xSize, ySize);
        modularUI.guiWidgets.values().forEach(widget -> {
            GlStateManager.pushMatrix();
            GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
            GlStateManager.enableBlend();
            widget.drawInBackground(mouseX, mouseY, this);
            GlStateManager.popMatrix();
        });
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheelMovement = Mouse.getEventDWheel();
        if (wheelMovement != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            mouseWheelMove(mouseX - guiLeft, mouseY, wheelMovement);
        }
    }

    protected void mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        //noinspection ResultOfMethodCallIgnored
        modularUI.guiWidgets.values().stream().anyMatch(widget -> widget.mouseWheelMove(mouseX, mouseY, wheelDelta));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        boolean result = modularUI.guiWidgets.values().stream().anyMatch(widget -> widget.mouseClicked(mouseX, mouseY, mouseButton));
        if (!result) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        boolean result = modularUI.guiWidgets.values().stream().anyMatch(widget ->
            widget.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
        if (!result) {
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        boolean result = modularUI.guiWidgets.values().stream().anyMatch(widget -> widget.mouseReleased(mouseX, mouseY, state));
        if (!result) {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean result = modularUI.guiWidgets.values().stream().anyMatch(widget -> widget.keyTyped(typedChar, keyCode));
        if (!result) {
            super.keyTyped(typedChar, keyCode);
        }
    }

}
