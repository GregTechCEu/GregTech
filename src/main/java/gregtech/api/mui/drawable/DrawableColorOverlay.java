package gregtech.api.mui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import java.util.function.Supplier;

public class DrawableColorOverlay implements IDrawable {

    private final Supplier<Boolean> drawOverlay;
    private final Supplier<Integer> OVERLAY_COLOR;

    public DrawableColorOverlay(Supplier<Boolean> drawOverlay, Supplier<Integer> color) {
        this.drawOverlay = drawOverlay;
        this.OVERLAY_COLOR = color;
    }

    public DrawableColorOverlay(Supplier<Boolean> drawOverlay) {
        this(drawOverlay, () -> 0x80404040);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (drawOverlay.get()) {
            GuiDraw.drawRect(x, y, width, height, OVERLAY_COLOR.get());
        }
    }
}
