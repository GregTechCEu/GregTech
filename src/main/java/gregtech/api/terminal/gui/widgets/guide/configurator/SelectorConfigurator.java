package gregtech.api.terminal.gui.widgets.guide.configurator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.gui.widgets.SelectorWidget;

import java.awt.*;
import java.util.List;

public class SelectorConfigurator extends ConfiguratorWidget{
    private final List<String> candidates;
    private String defaultValue;
    public SelectorConfigurator(int x, int y, JsonObject config, String name, List<String> candidates, String defaultValue) {
        super(x, y, config, name, true);
        this.defaultValue = defaultValue;
        this.candidates = candidates;
        init();
    }

    public SelectorConfigurator(int x, int y, JsonObject config, String name, List<String> candidates) {
        super(x, y, config, name, false);
        this.candidates = candidates;
        init();
    }

    private void init(){
        this.addWidget(new SelectorWidget(0, 15, 80, 20, candidates, -1, ()->{
            if(config.get(name).isJsonNull()) {
                return defaultValue;
            }
            return config.get(name).getAsString();
        }, true)
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setIsUp(true)
                .setOnChanged(this::updateValue));
    }

    private void updateValue(String selected) {
        config.addProperty(name, selected);
        update();
    }
}
