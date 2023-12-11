package gregtech.api.gui;

import java.awt.*;

/**
 * Provides GUI and screen sizes for aligning of widgets
 * according to the screen configuration and relative coordinates
 */
public interface ISizeProvider {

    /**
     * @return current screen width
     */
    int getScreenWidth();

    /**
     * @return current screen height
     */
    int getScreenHeight();

    /**
     * @return width of the GUI the widget is located in
     *         if the widget is located in the sub interface, then width
     *         and height will be the sub interface's holder dimensions
     */
    int getWidth();

    /**
     * @return height of the GUI the widget is located in
     *         if the widget is located in the sub interface, then height
     *         and width will be the sub interface's holder dimensions
     */
    int getHeight();

    default int getGuiLeft() {
        return (getScreenWidth() - getWidth()) / 2;
    }

    default int getGuiTop() {
        return (getScreenHeight() - getHeight()) / 2;
    }

    default Rectangle toScreenCoords(Rectangle widgetRect) {
        return new Rectangle(getGuiLeft() + widgetRect.x, getGuiTop() + widgetRect.y, widgetRect.width,
                widgetRect.height);
    }
}
