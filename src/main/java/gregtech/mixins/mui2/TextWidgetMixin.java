package gregtech.mixins.mui2;

import gregtech.api.mui.UnboxFix;

import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = TextWidget.class, remap = false)
public abstract class TextWidgetMixin extends Widget<TextWidget> implements UnboxFix {

    @Unique
    private boolean gregTech$defaultTextColor = true;

    @Unique
    private boolean gregTech$defaultShadow = true;

    @Override
    public void gregTech$useDefaultTextColor(boolean b) {
        gregTech$defaultTextColor = b;
    }

    @Override
    public void gregTech$useDefaultShadow(boolean b) {
        gregTech$defaultShadow = b;
    }

    @Override
    public boolean canHover() {
        return hasTooltip();
    }

    @ModifyReturnValue(method = { "getDefaultHeight", "getDefaultWidth" }, at = @At("TAIL"))
    public int clamp(int r) {
        return Math.max(1, r);
    }

    @ModifyArg(method = "draw",
               at = @At(value = "INVOKE",
                        target = "Lcom/cleanroommc/modularui/drawable/text/TextRenderer;setColor(I)V"))
    public int fixColor(int color, @Local(argsOnly = true) WidgetTheme theme) {
        return gregTech$defaultTextColor ? theme.getTextColor() : color;
    }

    @ModifyArg(method = "draw",
               at = @At(value = "INVOKE",
                        target = "Lcom/cleanroommc/modularui/drawable/text/TextRenderer;setShadow(Z)V"))
    public boolean fixShadow(boolean shadow, @Local(argsOnly = true) WidgetTheme theme) {
        return gregTech$defaultShadow ? theme.getTextShadow() : shadow;
    }
}
