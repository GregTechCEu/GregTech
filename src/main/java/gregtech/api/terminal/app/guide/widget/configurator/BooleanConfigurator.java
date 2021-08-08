package gregtech.api.terminal.app.guide.widget.configurator;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;

import java.awt.*;

public class BooleanConfigurator extends ConfiguratorWidget<Boolean>{

    public BooleanConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name) {
        super(group, config, name);
    }

    public BooleanConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name, boolean defaultValue) {
        super(group, config, name, defaultValue);
    }

    protected void init(){
        this.addWidget(new RectButtonWidget(0, 15, 10, 10, 2)
                .setToggleButton(new ColorRectTexture(new Color(198, 198, 198).getRGB()), (c, p)->updateValue(p))
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
}
