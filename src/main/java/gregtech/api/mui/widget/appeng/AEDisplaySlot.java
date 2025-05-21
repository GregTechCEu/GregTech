package gregtech.api.mui.widget.appeng;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public abstract class AEDisplaySlot<T extends IAEStack<T>> extends Widget<AEDisplaySlot<T>>
                                   implements JeiIngredientProvider {

    public AEDisplaySlot() {
        super();
    }

    @Override
    public void onInit() {
        tooltipBuilder(this::buildTooltip);
    }

    protected void buildTooltip(@NotNull RichTooltip tooltip) {}
}
