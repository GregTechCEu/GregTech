package gregtech.api.terminal.gui.widgets.guide.configurator;

import com.google.gson.JsonObject;
import gregtech.api.terminal.gui.widgets.ColorWidget;

public class ColorConfigurator extends ConfiguratorWidget{
    private int defaultValue;

    public ColorConfigurator(int x, int y, JsonObject config, String name, int defaultValue) {
        super(x, y, config, name, true);
        this.defaultValue = defaultValue;
        init();
    }

    public ColorConfigurator(int x, int y, JsonObject config, String name) {
        super(x, y, config, name, false);
        init();
    }

    private void init(){
        this.addWidget(new ColorWidget(0, 15, 85, 10).setColorSupplier(()->{
            if(config.get(name).isJsonNull()) {
                return defaultValue;
            }
            return config.get(name).getAsInt();
        },true).setOnColorChanged(this::update));
    }

    private void update(int color) {
        config.addProperty(name, color);
        update();
    }

}
