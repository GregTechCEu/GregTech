package gregtech.api.terminal.gui.widgets.guide.configurator;

import com.google.gson.JsonObject;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.SelectorWidget;

import java.awt.*;
import java.util.List;

public class SelectorConfigurator extends ConfiguratorWidget<String>{
    public SelectorConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name, List<String> candidates, String defaultValue) {
        super(group, config, name, defaultValue);
        init(candidates);
    }

    public SelectorConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name, List<String> candidates) {
        super(group, config, name);
        init(candidates);
    }

    protected void init(List<String> candidates){
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

}
