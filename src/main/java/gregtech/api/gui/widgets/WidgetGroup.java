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

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
    }

    @Override
    public void removeWidget(Widget widget) {
        super.removeWidget(widget);
    }

    @Override
    public void clearAllWidgets() {
        super.clearAllWidgets();
    }
}
