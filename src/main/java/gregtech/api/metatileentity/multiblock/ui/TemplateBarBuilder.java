package gregtech.api.metatileentity.multiblock.ui;

import com.cleanroommc.modularui.api.value.IDoubleValue;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.widgets.ProgressWidget;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

public final class TemplateBarBuilder {

    ProgressWidget widget = new ProgressWidget();

    TemplateBarBuilder() {}

    public TemplateBarBuilder progress(DoubleSupplier supplier) {
        return value(new DoubleValue.Dynamic(supplier, d -> {}));
    }

    public TemplateBarBuilder value(IDoubleValue<?> value) {
        this.widget.value(value);
        return this;
    }

    public TemplateBarBuilder texture(UITexture texture) {
        this.widget.texture(texture, -1);
        return this;
    }

    public TemplateBarBuilder tooltipBuilder(Consumer<RichTooltip> consumer) {
        this.widget.tooltipAutoUpdate(true);
        this.widget.tooltipBuilder(consumer);
        return this;
    }

    ProgressWidget build() {
        return this.widget;
    }
}
