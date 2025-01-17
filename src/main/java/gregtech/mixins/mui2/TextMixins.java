package gregtech.mixins.mui2;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.BaseKey;
import com.cleanroommc.modularui.drawable.text.CompoundKey;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

public abstract class TextMixins {

    @Mixin(value = BaseKey.class, remap = false)
    public static abstract class BaseKeyMixin {

        @WrapOperation(method = "format(Lnet/minecraft/util/text/TextFormatting;)Lcom/cleanroommc/modularui/drawable/text/BaseKey;",
                       at = @At(value = "INVOKE",
                                target = "Lcom/cleanroommc/modularui/drawable/text/FontRenderHelper;addAfter([Lnet/minecraft/util/text/TextFormatting;Lnet/minecraft/util/text/TextFormatting;)V"))
        public void nullSafe(TextFormatting[] state, TextFormatting formatting, Operation<Void> original) {
            if (formatting != null) original.call(state, formatting);
        }
    }

    // todo check if can remove once rc3 is released
    @Mixin(value = CompoundKey.class, remap = false)
    public abstract static class KeyCompMixin extends BaseKey {

        @Redirect(method = "get",
                  at = @At(value = "INVOKE",
                           target = "Lcom/cleanroommc/modularui/api/drawable/IKey;get()Ljava/lang/String;"))
        public String formatTheKeys(IKey key) {
            return key.getFormatted();
        }
    }

    // todo remove once rc3 is released
    @Mixin(value = RichTooltip.class, remap = false)
    public abstract static class TooltipMixin {

        @Shadow
        private boolean dirty;

        @Shadow
        @Final
        private RichText text;

        @Shadow
        private Consumer<RichTooltip> tooltipBuilder;

        @Shadow
        public abstract RichTooltip getThis();

        @Shadow
        public abstract void markDirty();

        /**
         * @author GTCEu - Ghzdude
         * @reason Implement <a href="https://github.com/CleanroomMC/ModularUI/pull/83">MUI2 PR#83</a>
         */
        @Overwrite
        public void buildTooltip() {
            this.dirty = false;
            if (this.tooltipBuilder != null) {
                this.text.clearText();
                this.tooltipBuilder.accept(getThis());
            }
        }

        /**
         * @author GTCEu - Ghzdude
         * @reason Implement <a href="https://github.com/CleanroomMC/ModularUI/pull/83">MUI2 PR#83</a>
         */
        @Overwrite
        public RichTooltip tooltipBuilder(Consumer<RichTooltip> tooltipBuilder) {
            Consumer<RichTooltip> existingBuilder = this.tooltipBuilder;
            if (existingBuilder != null) {
                this.tooltipBuilder = tooltip -> {
                    existingBuilder.accept(getThis());
                    tooltipBuilder.accept(getThis());
                };
            } else {
                this.tooltipBuilder = tooltipBuilder;
            }
            markDirty();
            return getThis();
        }
    }

    @Mixin(value = RichTextWidget.class, remap = false)
    private abstract static class RichTextWidgetMixin extends Widget<RichTextWidget> {

        @Shadow
        private boolean autoUpdate;

        @Shadow
        private boolean dirty;

        @Shadow
        @Final
        private RichText text;

        @Shadow
        private Consumer<RichText> builder;

        /**
         * @author GTCEu - Ghzdude
         * @reason Implement <a href="https://github.com/CleanroomMC/ModularUI/pull/83">MUI2 PR#83</a>
         */
        @Overwrite
        public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
            super.draw(context, widgetTheme);
            if (this.autoUpdate || this.dirty) {
                if (this.builder != null) {
                    this.text.clearText();
                    this.builder.accept(this.text);
                }
                this.dirty = false;
            }
            this.text.drawAtZero(context, getArea(), widgetTheme);
        }
    }
}
