package gregtech.api.mui.widget.appeng;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class AEStackPreviewWidget<T extends IAEStack<T>> extends Widget<AEStackPreviewWidget<T>>
                                          implements RecipeViewerIngredientProvider {

    @NotNull
    protected final Supplier<T> stackToDraw;

    public AEStackPreviewWidget(@NotNull Supplier<T> stackToDraw) {
        this.stackToDraw = stackToDraw;
        tooltipAutoUpdate(true);
        tooltipBuilder(this::buildTooltip);
    }

    protected abstract void buildTooltip(@NotNull RichTooltip tooltip);

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        draw(stackToDraw.get(), 1, 1, getArea().w() - 2, getArea().h() - 2);
    }

    public abstract void draw(@Nullable T stackToDraw, int x, int y, int width, int height);
}
