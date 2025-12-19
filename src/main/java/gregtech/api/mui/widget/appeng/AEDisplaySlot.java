package gregtech.api.mui.widget.appeng;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public abstract class AEDisplaySlot<T extends IAEStack<T>> extends Widget<AEDisplaySlot<T>>
                                   implements RecipeViewerIngredientProvider {

    protected final int index;

    public AEDisplaySlot(int index) {
        this.index = index;
        size(18);
        tooltipBuilder(this::buildTooltip);
    }

    protected abstract void buildTooltip(@NotNull RichTooltip tooltip);
}
