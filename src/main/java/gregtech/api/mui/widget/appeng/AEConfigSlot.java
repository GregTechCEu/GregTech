package gregtech.api.mui.widget.appeng;

import gregtech.api.mui.GTGuiTextures;

import net.minecraft.client.renderer.GlStateManager;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public abstract class AEConfigSlot<T extends IAEStack<T>> extends Widget<AEConfigSlot<T>>
                                  implements JeiIngredientProvider {

    protected final boolean isStocking;
    protected final BooleanSupplier isAutoPull;

    private static final IDrawable normalBackground = IDrawable.of(GTGuiTextures.SLOT, GTGuiTextures.CONFIG_ARROW_DARK);
    private static final IDrawable autoPullBackground = IDrawable.of(GTGuiTextures.SLOT_DARK,
            GTGuiTextures.CONFIG_ARROW);

    public AEConfigSlot(boolean isStocking, BooleanSupplier isAutoPull) {
        this.isStocking = isStocking;
        this.isAutoPull = isAutoPull;
        size(18);
    }

    @Override
    public void onInit() {
        tooltipBuilder(this::buildTooltip);
    }

    // TODO: change tooltip when autopull is on
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        tooltip.addLine(IKey.lang("gregtech.gui.config_slot"));
        if (isStocking) {
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set_only"));
        } else {
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set"));
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.scroll"));
        }
        tooltip.addLine(IKey.lang("gregtech.gui.config_slot.remove"));
    }

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

    @Override
    public @Nullable IDrawable getBackground() {
        return isAutoPull.getAsBoolean() ? autoPullBackground : normalBackground;
    }
}
