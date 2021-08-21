package gregtech.api.terminal.os.menu;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;

public interface IMenuComponent {
    default IGuiTexture buttonIcon() {
        return new ColorRectTexture(0);
    }
    default String hoverText() {
        return null;
    }
    default void click(Widget.ClickData clickData){}
}
