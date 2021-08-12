package gregtech.api.terminal.app.recipegraph.widget;

import gregtech.api.gui.Widget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.os.TerminalTheme;

public class RGContainer extends DraggableScrollableWidgetGroup {
    public RGContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setDraggable(true);
        this.setXScrollBarHeight(4);
        this.setYScrollBarWidth(4);
        this.setXBarStyle(null, TerminalTheme.COLOR_F_1);
        this.setYBarStyle(null, TerminalTheme.COLOR_F_1);
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if(widget.isVisible() && widget.isActive() && widget.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                return true;
            }
        }
        return false;
    }
}
