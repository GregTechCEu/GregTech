package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class CircleButton extends Widget {
    int x, y, r;
    public CircleButton(int x, int y, int r) {
        super(new Position(x - r, y - r), new Size(2 * r, 2 * r));
        this.r = r;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        int r = this.getSize().getHeight() / 2;
        int x = this.getPosition().x + r;
        int y = this.getPosition().y + r;

        RenderUtil.renderCircle(x, y, r, 0xffff0000, 24);
        RenderUtil.renderCircle(x, y, r - 2, 0xffffffff, 24);

    }
}
