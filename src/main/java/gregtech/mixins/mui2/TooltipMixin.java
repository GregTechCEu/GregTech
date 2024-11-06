package gregtech.mixins.mui2;

import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(value = RichTooltip.class, remap = false)
public abstract class TooltipMixin {

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
        this.text.clearText();
        if (this.tooltipBuilder != null) {
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

    @Mixin(value = RichTextWidget.class, remap = false)
    private static abstract class RichTextWidgetMixin extends Widget<RichTextWidget> {

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
