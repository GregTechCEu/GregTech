package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class RectButtonWidget extends CircleButtonWidget{
    public RectButtonWidget(int x, int y, int width, int height) {
        this(x, y, width, height,2);
    }

    public RectButtonWidget(int x, int y, int width, int height, int border) {
        super(x, y);
        setSelfPosition(new Position(x, y));
        setSize(new Size(width, height));
        this.border = border;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = this.getPosition().x;
        int y = this.getPosition().y;
        int width = this.getSize().width;
        int height = this.getSize().height;

        drawSolidRect(x, y, width, height, colors[0]);
        isHover = this.isMouseOverElement(mouseX, mouseY);
        if (isHover || hoverTick != 0) {
            float per = Math. min ((hoverTick + partialTicks) / 8, 1);
            drawSolidRect(x, y, (int) (width * per), border, colors[1]);
            drawSolidRect(x + width - border, y, border, (int) (height * per), colors[1]);
            drawSolidRect((int) ((1 - per) * width) + x, y + height - border, (int) (width * per), border, colors[1]);
            drawSolidRect(x, (int) ((1 - per) * height) + y, border, (int) (height * per), colors[1]);
        }
        drawSolidRect(x + border, y + border, width - 2 * border, height - 2 * border, colors[2]);
        if (icon != null) {
            icon.draw(x + border, y + border, width - 2 * border, height - 2 * border);
        }
    }
}
