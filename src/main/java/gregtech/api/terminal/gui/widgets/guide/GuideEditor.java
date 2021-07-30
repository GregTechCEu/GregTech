package gregtech.api.terminal.gui.widgets.guide;

import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class GuideEditor extends WidgetGroup {
    public String json;
    public GuideEditor(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }
}
