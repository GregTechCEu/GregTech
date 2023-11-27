package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

/**
 * @author brachy84
 */
public class DrawableWidget extends Widget {

    private BackgroundDrawer backgroundDrawer;
    private ForegroundDrawer foregroundDrawer;

    public DrawableWidget(Position selfPosition, Size size) {
        super(selfPosition, size);
    }

    public DrawableWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public DrawableWidget setBackgroundDrawer(BackgroundDrawer backgroundDrawer) {
        this.backgroundDrawer = backgroundDrawer;
        return this;
    }

    public DrawableWidget setForegroundDrawer(ForegroundDrawer foregroundDrawer) {
        this.foregroundDrawer = foregroundDrawer;
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (backgroundDrawer != null)
            backgroundDrawer.draw(mouseX, mouseY, partialTicks, context, this);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (foregroundDrawer != null)
            foregroundDrawer.draw(mouseX, mouseY, this);
    }

    @FunctionalInterface
    public interface BackgroundDrawer {

        void draw(int mouseX, int mouseY, float partialTicks, IRenderContext context, Widget widget);
    }

    @FunctionalInterface
    public interface ForegroundDrawer {

        void draw(int mouseX, int mouseY, Widget widget);
    }
}
