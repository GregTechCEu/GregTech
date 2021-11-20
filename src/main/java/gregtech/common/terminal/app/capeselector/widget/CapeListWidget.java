package gregtech.common.terminal.app.capeselector.widget;

import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ScrollableListWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.UnlockedCapesRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CapeListWidget extends ScrollableListWidget {
    public CapeListWidget(int xPosition, int yPosition, int width, int height, UUID uuid) {
        super(xPosition, yPosition, width * 40, height * 56); // Cape banners are 28x44, expanded to 40x56

        List<ResourceLocation> capes = UnlockedCapesRegistry.unlockedCapes(uuid);

        if(capes.size() == 0)
            return;

        int rowNumber = 0;
        int i = 0;
        while (true) {
            WidgetGroup row = new WidgetGroup();
            for (int rowPosition = 0; rowPosition < width; rowPosition++) {
                TextureArea capeImage = new SizedTextureArea(capes.get(i), 0.5, 0, 14f / 64, 22f / 32, 28, 44);
                ClickButtonWidget capeButton = new ClickButtonWidget(xPosition + rowPosition * 40, yPosition + rowNumber * 56, 28, 44, "", (data) -> this.setCape(data)).setButtonTexture(capeImage);
                row.addWidget(capeButton);

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

    private void setCape(ClickData data) {

    }


}
