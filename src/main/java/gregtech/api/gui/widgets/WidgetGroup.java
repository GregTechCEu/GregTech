package gregtech.api.gui.widgets;

import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class WidgetGroup extends AbstractWidgetGroup {

    public WidgetGroup() {
        this(Position.ORIGIN);
    }

    public WidgetGroup(Position position) {
        super(position);
    }

    public WidgetGroup(Position position, Size size) {
        super(position, size);
    }

    public WidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
    }

    @Override
    public void removeWidget(Widget widget) {
        super.removeWidget(widget);
    }

    @Override
    public void waitToRemoved(Widget widget) {
        super.waitToRemoved(widget);
    }

    @Override
    public void clearAllWidgets() {
        super.clearAllWidgets();
    }
}
