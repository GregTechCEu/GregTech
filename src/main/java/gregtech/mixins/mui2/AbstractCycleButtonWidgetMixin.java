package gregtech.mixins.mui2;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.AbstractCycleButtonWidget;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(value = AbstractCycleButtonWidget.class, remap = false)
public class AbstractCycleButtonWidgetMixin<W extends AbstractCycleButtonWidget<W>> extends Widget<W> {

    @Shadow
    protected IDrawable[] hoverBackground;

    @Shadow
    protected IDrawable[] hoverOverlay;

    @ModifyReturnValue(method = "getCurrentOverlay", at = @At(value = "RETURN", ordinal = 0))
    public IDrawable fixOverlay(IDrawable original,
                                @Local(argsOnly = true) ITheme theme,
                                @Local(argsOnly = true) WidgetTheme widgetTheme) {
        return original != IDrawable.NONE ? original : super.getCurrentOverlay(theme, widgetTheme);
    }

    @ModifyReturnValue(method = "getCurrentBackground", at = @At(value = "RETURN", ordinal = 0))
    public IDrawable fixBackground(IDrawable original,
                                   @Local(argsOnly = true) ITheme theme,
                                   @Local(argsOnly = true) WidgetTheme widgetTheme) {
        return original != IDrawable.NONE ? original : super.getCurrentBackground(theme, widgetTheme);
    }

    @Override
    public W disableHoverBackground() {
        if (this.hoverBackground != null)
            Arrays.fill(this.hoverBackground, IDrawable.NONE);
        return getThis();
    }

    @Override
    public W disableHoverOverlay() {
        if (this.hoverOverlay != null)
            Arrays.fill(this.hoverOverlay, IDrawable.NONE);
        return getThis();
    }
}
