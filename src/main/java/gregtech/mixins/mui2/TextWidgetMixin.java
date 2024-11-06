package gregtech.mixins.mui2;

import com.cleanroommc.modularui.widgets.TextWidget;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TextWidget.class, remap = false)
public class TextWidgetMixin {

    @ModifyReturnValue(method = { "getDefaultHeight", "getDefaultWidth" }, at = @At("TAIL"))
    public int clamp(int r) {
        return Math.max(1, r);
    }
}
