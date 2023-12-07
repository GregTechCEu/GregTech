package gregtech.common.terminal.app.guideeditor.widget.configurator;

import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalTheme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NumberConfigurator extends ConfiguratorWidget<Integer> {

    public NumberConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name) {
        super(group, config, name);
    }

    public NumberConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, String name, int defaultValue) {
        super(group, config, name, defaultValue);
    }

    protected void init() {
        int y = 15;
        this.addWidget(new RectButtonWidget(0, y, 20, 20)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor())
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? -100 : -10))
                .setIcon(new TextTexture("-10", -1)));
        this.addWidget(new RectButtonWidget(96, y, 20, 20)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor())
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? +100 : +10))
                .setIcon(new TextTexture("+10", -1)));
        this.addWidget(new RectButtonWidget(20, y, 20, 20)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor())
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? -5 : -1))
                .setIcon(new TextTexture("-1", -1)));
        this.addWidget(new RectButtonWidget(76, y, 20, 20)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor())
                .setClickListener(data -> adjustTransferRate(data.isShiftClick ? +5 : +1))
                .setIcon(new TextTexture("+1", -1)));
        this.addWidget(new ImageWidget(40, y, 36, 20, TerminalTheme.COLOR_B_2));
        this.addWidget(new SimpleTextWidget(58, 25, "", 0xFFFFFF, () -> {
            JsonElement element = config.get(name);
            if (element.isJsonNull()) {
                return Integer.toString(defaultValue);
            }
            return element.getAsString();
        }, true));
    }

    private void adjustTransferRate(int added) {
        JsonElement element = config.get(name);
        int num = 0;
        if (!element.isJsonNull()) {
            num = element.getAsInt();
        } else {
            num = defaultValue;
        }
        updateValue(num + added);
    }
}
