package gregtech.common.terminal.app.guideeditor.widget.configurator;

import gregtech.api.terminal.gui.widgets.ColorWidget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;

import com.google.gson.JsonObject;

public class ColorConfigurator extends ConfiguratorWidget<Integer> {

    public ColorConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name, int defaultValue) {
        super(group, config, name, defaultValue);
    }

    public ColorConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name) {
        super(group, config, name);
    }

    protected void init() {
        this.addWidget(new ColorWidget(0, 15, 85, 10).setColorSupplier(() -> {
            if (config.get(name).isJsonNull()) {
                return defaultValue;
            }
            return config.get(name).getAsInt();
        }, true).setOnColorChanged(this::updateValue));
    }
}
