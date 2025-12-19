package gregtech.api.mui.widget.appeng.item;

import gregtech.api.mui.widget.appeng.AEStackPreviewWidget;
import gregtech.client.utils.RenderUtil;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.screen.RichTooltip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

class AEItemStackPreviewWidget extends AEStackPreviewWidget<IAEItemStack> {

    public AEItemStackPreviewWidget(@NotNull Supplier<IAEItemStack> stackToDraw) {
        super(stackToDraw);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        IAEItemStack stack = stackToDraw.get();
        if (stack == null) return;
        tooltip.addFromItem(stack.getDefinition());
    }

    @Override
    public void draw(@Nullable IAEItemStack stackToDraw, int x, int y, int width, int height) {
        if (stackToDraw == null) return;
        RenderUtil.drawItemStack(stackToDraw.getDefinition(), x, y, false);
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEItemStack stack = stackToDraw.get();
        return stack == null ? null : stack.createItemStack();
    }
}
