package gregtech.api.mui.widget.appeng;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public class AEStackPreviewWidget extends Widget<AEStackPreviewWidget> {

    @NotNull
    AEDrawFunction drawFunction;

    public AEStackPreviewWidget(@NotNull AEDrawFunction drawFunction) {
        this.drawFunction = drawFunction;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        drawFunction.draw(1, 1, getArea().w() - 1, getArea().h() - 1);
    }

    @FunctionalInterface
    public interface AEDrawFunction {

        void draw(int x, int y, int width, int height);
    }
}
