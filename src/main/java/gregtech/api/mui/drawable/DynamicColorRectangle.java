package gregtech.api.mui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Color;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public class DynamicColorRectangle implements IDrawable {

    @NotNull
    private final IntSupplier colorSupplier;

    public DynamicColorRectangle(@NotNull IntSupplier colorSupplier) {
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height, WidgetTheme widgetTheme) {
        if (canApplyTheme()) {
            Color.setGlColor(widgetTheme.getColor());
        } else {
            Color.setGlColorOpaque(Color.WHITE.main);
        }

        GuiDraw.drawRect(x0, y0, width, height, colorSupplier.getAsInt());
    }
}
