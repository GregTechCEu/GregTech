package gregtech.api.terminal.gui.widgets.guide.configurator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;

import java.awt.*;

public class NumberConfigurator extends ConfiguratorWidget{
    private int defaultValue;

    public NumberConfigurator(int x, int y, JsonObject config, String name) {
        super(x, y, config, name, false);
        init(0);
    }

    public NumberConfigurator(int x, int y, JsonObject config, String name, int defaultValue) {
        super(x, y, config, name, true);
        init(defaultValue);
    }

    private void init(int defaultValue){
        this.defaultValue = defaultValue;
        int y = 15;
        this.addWidget(new RectButtonWidget(0, y, 20, 20)
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? -100 : -10))
                .setIcon(new TextTexture("-10", -1)));
        this.addWidget(new RectButtonWidget(96, y, 20, 20)
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? +100 : +10))
                .setIcon(new TextTexture("+10", -1)));
        this.addWidget(new RectButtonWidget(20, y, 20, 20)
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? -5 : -1))
                .setIcon(new TextTexture("-1", -1)));
        this.addWidget(new RectButtonWidget(76, y, 20, 20)
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? +5 : +1))
                .setIcon(new TextTexture("+1", -1)));
        this.addWidget(new ImageWidget(40, y, 36, 20, new ColorRectTexture(0x9f000000)));
        this.addWidget(new SimpleTextWidget(58, 25, "", 0xFFFFFF, () -> {
            JsonElement element = config.get(name);
            if (element.isJsonNull()) {
                return Integer.toString(defaultValue);
            }
            return element.getAsString();
        }, true));
    }

    private void adjustTransferRate(int added) {
        JsonElement element = config.get(name);
        int num = defaultValue;
        if (!element.isJsonNull()) {
            num = element.getAsInt();
        }
        config.addProperty(name, num + added);
        update();
    }
}
