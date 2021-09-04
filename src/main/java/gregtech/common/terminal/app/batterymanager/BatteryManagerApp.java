package gregtech.common.terminal.app.batterymanager;

import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.items.MetaItems;

public class BatteryManagerApp extends AbstractApplication {
    public BatteryManagerApp() {
        super("battery", new ItemStackTexture(MetaItems.BATTERY_HV_SODIUM.getStackForm()));
    }

    @Override
    public AbstractApplication initApp() {
        this.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
        this.addWidget(new BatteryWidget(10, 10, 200, 200, getOs()));
        return this;
    }


}
