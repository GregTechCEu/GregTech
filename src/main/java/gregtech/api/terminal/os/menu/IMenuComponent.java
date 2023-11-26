package gregtech.api.terminal.os.menu;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;

public interface IMenuComponent {

    /**
     * Component Icon
     */
    default IGuiTexture buttonIcon() {
        return new ColorRectTexture(0);
    }

    /**
     * Component Hover Text
     */
    default String hoverText() {
        return null;
    }

    /**
     * Click Event. Side see {@link Widget.ClickData#isClient}
     */
    default void click(Widget.ClickData clickData) {}
}
