package gregtech.api.mui.widget.appeng;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class AEStackPreviewWidget<T extends IAEStack<T>> extends Widget<AEStackPreviewWidget<T>>
                                          implements JeiIngredientProvider {

    @NotNull
    protected final Supplier<T> stackToDraw;

    public AEStackPreviewWidget(@NotNull Supplier<T> stackToDraw) {
        this.stackToDraw = stackToDraw;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        draw(stackToDraw.get(), 1, 1, getArea().w() - 2, getArea().h() - 2);
    }

    public abstract void draw(@Nullable T stackToDraw, int x, int y, int width, int height);
}
