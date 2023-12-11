package gregtech.common.terminal.app.guideeditor.widget.configurator;

import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.SelectorWidget;
import gregtech.api.terminal.os.TerminalTheme;

import com.google.gson.JsonObject;

import java.util.List;

public class SelectorConfigurator extends ConfiguratorWidget<String> {

    public SelectorConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name,
                                List<String> candidates, String defaultValue) {
        super(group, config, name, defaultValue);
        init(candidates);
    }

    public SelectorConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name,
                                List<String> candidates) {
        super(group, config, name);
        init(candidates);
    }

    protected void init(List<String> candidates) {
        this.addWidget(new SelectorWidget(0, 15, 80, 20, candidates, -1, () -> {
            if (config.get(name).isJsonNull()) {
                return defaultValue;
            }
            return config.get(name).getAsString();
        }, true)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor())
                .setIsUp(true)
                .setOnChanged(this::updateValue));
    }
}
