package gregtech.mixins.mui2;

import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.TextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

// todo remove both mixins when mui2 is updated past rc4
@Mixin(value = TextWidget.class, remap = false)
public class TextWidgetMixin extends Widget<TextWidget> {

    @Shadow
    private Integer color;

    @Shadow
    private Boolean shadow;

    /**
     * @author Ghzdude
     * @reason fix unboxing
     */
    @Overwrite
    public int getColor() {
        return this.color == null ? getWidgetTheme(getContext().getTheme()).getTextColor() : this.color;
    }

    /**
     * @author Ghzdude
     * @reason fix unboxing
     */
    @Overwrite
    public boolean isShadow() {
        return this.shadow == null ? getWidgetTheme(getContext().getTheme()).getTextShadow() : this.shadow;
    }

    @Mixin(value = TextRenderer.class, remap = false)
    public static class TextRenderMixin {

        /**
         * Adjusts the area {@code SHARED} call in {@link TextRenderer#drawScrolling}
         * specifically targeting the y and height of the area
         */
        @ModifyArgs(method = "drawScrolling",
                    at = @At(value = "INVOKE",
                             target = "Lcom/cleanroommc/modularui/widget/sizer/Area;set(IIII)V"))
        public void adjustArea(Args args) {
            args.set(1, -500);
            args.set(3, 1000);
        }
    }
}
