package gregtech.common.terminal.app.capeselector.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.util.CapesRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.UUID;

public class CapeListWidget extends DraggableScrollableWidgetGroup {
    private final UUID uuid;
    private List<ResourceLocation> capes;
    private int selectedX, selectedY = -1;

    public CapeListWidget(int xPosition, int yPosition, int width, int height, UUID uuid) {
        super(xPosition, yPosition, width * 70 + 42, height * 56 + 12); // Cape banners are 28x44, expanded to 70x56

        this.uuid = uuid;
        capes = CapesRegistry.unlockedCapes(uuid);

        if (capes == null || capes.size() == 0)
            return;

        int rowNumber = 0;
        int i = 0;
        while (true) {
            WidgetGroup row = new WidgetGroup();
            for (int rowPosition = 0; rowPosition < width; rowPosition++) {
                TextureArea capeImage = new SizedTextureArea(capes.get(i), 0.5, 0, 14f / 64, 22f / 32, 28, 44);

                int finalRowPosition = rowPosition;
                int finalRowNumber = rowNumber;
                int finalI = i;
                ClickButtonWidget capeButton = new ClickButtonWidget(xPosition + rowPosition * 70, yPosition + rowNumber * 56, 28, 44, "",
                        (data) -> this.setCape(data, finalRowPosition, finalRowNumber, capes.get(finalI))).setButtonTexture(capeImage)
                        .setShouldClientCallback(true);
                row.addWidget(capeButton);

                if(capes.get(i).equals(CapesRegistry.wornCapes.get(uuid))) { // If this is the cape that the player is wearing right now, select it.
                    selectedX = finalRowPosition;
                    selectedY = finalRowNumber;
                }
                i++;
                if (i == capes.size()) {
                    this.addWidget(row);
                    return;
                }
            }
            this.addWidget(row);
            rowNumber++;
        }
    }

    private void setCape(ClickData data, int x, int y, ResourceLocation cape) {
        if (selectedX == x && selectedY == y) {
            selectedX = -1; // Sets a "not in use" flag.
            CapesRegistry.wornCapes.put(uuid, null);
            return;
        }

        selectedX = x;
        selectedY = y;
        CapesRegistry.wornCapes.put(uuid, cape);
    }

    public List<ResourceLocation> getCapes() {
        return capes;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);

        if (selectedX == -1 || selectedY == -1)
            return;

        // Get selected cape button
        Widget button = ((WidgetGroup) this.getContainedWidgets(false).get(this.selectedY))
                .getContainedWidgets(false).get(this.selectedX);

        drawSelectionOverlay(button.toRectangleBox().x - 6, button.toRectangleBox().y - 6,
                button.toRectangleBox().width + 12, button.toRectangleBox().height + 12); // Add a bit of margin
    }
}
