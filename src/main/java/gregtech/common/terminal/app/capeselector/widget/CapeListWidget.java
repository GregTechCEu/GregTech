package gregtech.common.terminal.app.capeselector.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.util.CapesRegistry;
import gregtech.client.utils.RenderUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CapeListWidget extends DraggableScrollableWidgetGroup {

    private final UUID uuid;
    private List<ResourceLocation> capes;
    private int selectedX, selectedY = -1;

    public CapeListWidget(int xPosition, int yPosition, int width, int height, UUID uuid) {
        super(xPosition, yPosition, width * 70, height * 56); // Cape banners are 28x44, expanded to 70x56
        this.uuid = uuid;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (capes == null) {
            updateCapeCandidates(CapesRegistry.getUnlockedCapes(uuid));
            writeUpdateInfo(-1, buf -> {
                buf.writeVarInt(capes.size());
                capes.stream().map(ResourceLocation::toString).forEach(buf::writeString);
            });
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -1) {
            capes = new ArrayList<>();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                capes.add(new ResourceLocation(buffer.readString(Short.MAX_VALUE)));
            }
            updateCapeCandidates(capes);
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    private void updateCapeCandidates(List<ResourceLocation> capes) {
        this.capes = capes;
        int width = (getSize().width) / 70;
        int rowNumber = 0;
        if (capes == null || capes.isEmpty()) return;
        int i = 0;
        while (true) {
            WidgetGroup row = new WidgetGroup();
            for (int rowPosition = 0; rowPosition < width; rowPosition++) {
                TextureArea capeImage = new SizedTextureArea(capes.get(i), 0.5, 0, 14f / 64, 22f / 32, 28, 44);

                int finalRowPosition = rowPosition;
                int finalRowNumber = rowNumber;
                int finalI = i;
                ClickButtonWidget capeButton = new ClickButtonWidget(rowPosition * 70 + 21, rowNumber * 56, 28, 44, "",
                        (data) -> this.setCape(finalRowPosition, finalRowNumber, capes.get(finalI)))
                                .setButtonTexture(capeImage)
                                .setShouldClientCallback(true);
                row.addWidget(capeButton);

                if (capes.get(i).equals(CapesRegistry.getPlayerCape(uuid))) { // If this is the cape that the player is
                                                                              // wearing right now, select it.
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

    private void setCape(int x, int y, ResourceLocation cape) {
        if (selectedX == x && selectedY == y) {
            selectedX = -1; // Sets a "not in use" flag.
            cape = null;
        } else {
            selectedX = x;
            selectedY = y;
        }
        if (!isRemote()) {
            CapesRegistry.giveCape(uuid, cape);
        }
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
        Widget button = ((WidgetGroup) this.widgets.get(this.selectedY)).widgets.get(this.selectedX);

        RenderUtil.useScissor(getPosition().x, getPosition().y, getSize().width - yBarWidth,
                getSize().height - xBarHeight, () -> {
                    drawSelectionOverlay(button.getPosition().x - 6, button.getPosition().y - 6,
                            button.getSize().width + 12, button.getSize().height + 12); // Add a bit of margin
                });
    }
}
