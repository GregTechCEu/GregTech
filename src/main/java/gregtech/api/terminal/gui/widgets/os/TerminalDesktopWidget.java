package gregtech.api.terminal.gui.widgets.os;

import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class TerminalDesktopWidget extends WidgetGroup {
    private final TerminalOSWidget os;
    public TerminalDesktopWidget(Position position, Size size, TerminalOSWidget os) {
        super(position, size);
        this.os = os;
    }
}
