package gregtech.common.terminal.app.settings.widgets;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class ControlSettings extends AbstractWidgetGroup {
    final TerminalOSWidget os;

    public ControlSettings(TerminalOSWidget os) {
        super(Position.ORIGIN, new Size(323, 212));
        this.os = os;

    }
}
