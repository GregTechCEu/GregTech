package gregtech.api.terminal.gui.widgets.guide.congiurator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;

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
        this.addWidget(new RectButtonWidget(76, 11, 40, 20)
                .setClickListener(data -> updateString())
                .setIcon(new TextTexture("Update")));
        textFieldWidget = new TextFieldWidget(0, 11, 76, 20, true, null, null)
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
