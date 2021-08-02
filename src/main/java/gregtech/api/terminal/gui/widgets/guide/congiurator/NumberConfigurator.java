package gregtech.api.terminal.gui.widgets.guide.congiurator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;

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
        this.addWidget(new RectButtonWidget(0, 11, 20, 20)
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? -100 : -10))
                .setIcon(new TextTexture("-10")));
        this.addWidget(new RectButtonWidget(96, 11, 20, 20)
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? +100 : +10))
                .setIcon(new TextTexture("+10")));
        this.addWidget(new RectButtonWidget(20, 11, 20, 20)
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? -5 : -1))
                .setIcon(new TextTexture("-1")));
        this.addWidget(new RectButtonWidget(76, 11, 20, 20)
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? +5 : +1))
                .setIcon(new TextTexture("+1")));
        this.addWidget(new ImageWidget(40, 11, 36, 20, GuiTextures.DISPLAY));
        this.addWidget(new SimpleTextWidget(58, 21, "", 0xFFFFFF, () -> {
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
