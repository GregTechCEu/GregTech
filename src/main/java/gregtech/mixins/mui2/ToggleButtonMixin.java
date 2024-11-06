package gregtech.mixins.mui2;

import gregtech.api.mui.StateOverlay;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.widgets.AbstractCycleButtonWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ToggleButton.class, remap = false)
public class ToggleButtonMixin extends AbstractCycleButtonWidget<ToggleButton> implements StateOverlay {

    public ToggleButton overlay(boolean selected, IDrawable... overlay) {
        this.overlay = addToArray(this.overlay, overlay, selected ? 1 : 0);
        return getThis();
    }

    public ToggleButton hoverOverlay(boolean selected, IDrawable... overlay) {
        this.hoverOverlay = addToArray(this.hoverOverlay, overlay, selected ? 1 : 0);
        return getThis();
    }
}
