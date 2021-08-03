package gregtech.api.terminal.gui.widgets.guide.configurator;

import com.google.gson.JsonObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;

import java.awt.*;

public class BooleanConfigurator extends ConfiguratorWidget{
    private boolean defaultValue;

    public BooleanConfigurator(int x, int y, JsonObject config, String name) {
        super(x, y, config, name, false);
        init();
    }

    public BooleanConfigurator(int x, int y, JsonObject config, String name, boolean defaultValue) {
        super(x, y, config, name, true);
        this.defaultValue = defaultValue;
        init();
    }

    private void init(){
        this.addWidget(new RectButtonWidget(0, 15, 10, 10, 2)
                .setToggleButton(new ColorRectTexture(new Color(198, 198, 198).getRGB()), (c, p)-> update(p))
                .setValueSupplier(true, ()->{
                    if(config.get(name).isJsonNull()) {
                        return defaultValue;
                    }
                    return config.get(name).getAsBoolean();
                })
                .setColors(new Color(0, 0, 0, 74).getRGB(),
                        new Color(128, 255, 128).getRGB(),
                        new Color(255, 255, 255, 0).getRGB())
                .setIcon(new ColorRectTexture(new Color(0, 0, 0, 74).getRGB())));
    }

    private void update(boolean bool) {
        config.addProperty(name, bool);
        update();
    }
}
