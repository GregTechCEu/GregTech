package gregtech.api.mui.widget.appeng;

import net.minecraft.client.renderer.GlStateManager;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public abstract class AEDisplaySlot<T extends IAEStack<T>> extends Widget<AEDisplaySlot<T>>
                                   implements JeiIngredientProvider {

    protected final int index;

    public AEDisplaySlot(int index) {
        this.index = index;
        size(18);
    }

    @Override
    public void onInit() {
        tooltipBuilder(this::buildTooltip);
    }

    protected void buildTooltip(@NotNull RichTooltip tooltip) {}

    protected void drawSlotOverlay() {
        GlStateManager.colorMask(true, true, true, false);
        GuiDraw.drawRect(1, 1, 16, 16, getSlotHoverColor());
        GlStateManager.colorMask(true, true, true, true);
    }

    public int getSlotHoverColor() {
        WidgetTheme theme = getWidgetTheme(getContext().getTheme());
        if (theme instanceof WidgetSlotTheme slotTheme) {
            return slotTheme.getSlotHoverColor();
        }
        return ITheme.getDefault().getItemSlotTheme().getSlotHoverColor();
    }
}
