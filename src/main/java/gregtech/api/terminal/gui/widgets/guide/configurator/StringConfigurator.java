package gregtech.api.terminal.gui.widgets.guide.configurator;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;

import java.awt.*;

public class StringConfigurator extends ConfiguratorWidget{
    private String defaultValue = "";
    private TextFieldWidget textFieldWidget;

    public StringConfigurator(int x, int y, JsonObject config, String name) {
        super(x, y, config, name, false);
        init();
        textFieldWidget.setCurrentString(config.get(name).isJsonNull() ? defaultValue : config.get(name).getAsString());
    }

    public StringConfigurator(int x, int y, JsonObject config, String name, String defaultValue) {
        super(x, y, config, name, true);
        this.defaultValue = defaultValue;
        init();
        textFieldWidget.setCurrentString(config.get(name).isJsonNull() ? defaultValue : config.get(name).getAsString());
    }

    private void init() {
        this.addWidget(new RectButtonWidget(76, 15, 40, 20)
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setClickListener(data -> updateString())
                .setIcon(new TextTexture("Update", -1)));
        textFieldWidget = new TextFieldWidget(0, 15, 76, 20, new ColorRectTexture(0x9f000000), null, null)
                .setMaxStringLength(Integer.MAX_VALUE)
                .setValidator(s->true);
        this.addWidget(textFieldWidget);
    }

    private void updateString() {
        config.addProperty(name, textFieldWidget.getCurrentString());
        update();
    }

    @Override
    protected void onDefault() {
        textFieldWidget.setCurrentString(defaultValue);
    }
}
