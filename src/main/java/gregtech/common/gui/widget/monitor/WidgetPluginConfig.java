package gregtech.common.gui.widget.monitor;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.entity.player.InventoryPlayer;

public class WidgetPluginConfig extends WidgetGroup {

    protected TextureArea textureArea;
    int width, height;

    public WidgetPluginConfig setSize(int width, int height) {
        setSize(new Size(width, height));
        this.width = width;
        this.height = height;
        if (this.gui != null) {
            setSelfPosition(new Position((gui.getWidth() - width) / 2, (gui.getHeight() - height) / 2));
            onPositionUpdate();
        }
        return this;
    }

    @Override
    public void setGui(ModularUI gui) {
        super.setGui(gui);
        if (gui == null) return;
        setSelfPosition(new Position((gui.getWidth() - width) / 2, (gui.getHeight() - height) / 2));
        onPositionUpdate();
    }

    public WidgetPluginConfig setBackGround(TextureArea textureArea) {
        this.textureArea = textureArea;
        return this;
    }

    public WidgetPluginConfig widget(Widget widget) {
        addWidget(widget);
        return this;
    }

    public void removePluginWidget() {
        clearAllWidgets();
    }

    public WidgetPluginConfig bindPlayerInventory(InventoryPlayer inventoryPlayer, TextureArea imageLocation, int x,
                                                  int y) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.widget((new SlotWidget(inventoryPlayer, col + (row + 1) * 9, x + col * 18, y + row * 18))
                        .setBackgroundTexture(new TextureArea[] { imageLocation }).setLocationInfo(true, false));
            }
        }

        return this.bindPlayerHotbar(inventoryPlayer, imageLocation, x, y + 58);
    }

    public WidgetPluginConfig bindPlayerHotbar(InventoryPlayer inventoryPlayer, TextureArea imageLocation, int x,
                                               int y) {
        for (int slot = 0; slot < 9; ++slot) {
            this.widget((new SlotWidget(inventoryPlayer, slot, x + slot * 18, y))
                    .setBackgroundTexture(new TextureArea[] { imageLocation }).setLocationInfo(true, true));
        }

        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (widgets.size() > 0 && textureArea != null) {
            Position pos = this.getPosition();
            Size size = this.getSize();
            textureArea.draw(pos.x, pos.y, size.width, size.height);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
