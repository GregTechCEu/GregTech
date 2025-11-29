package gregtech.api.mui.widget.appeng.item;

import gregtech.api.mui.widget.appeng.AEStackPreviewWidget;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

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
        if (stackToDraw.get() instanceof WrappedItemStack wrappedItemStack) {
            tooltip.addFromItem(wrappedItemStack.getDefinition());
        }
    }

    @Override
    public void draw(@Nullable IAEItemStack stackToDraw, int x, int y, int width, int height) {
        if (stackToDraw instanceof WrappedItemStack wrappedItemStack) {
            RenderUtil.drawItemStack(wrappedItemStack.getDefinition(), x, y, false);
        }
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEItemStack stack = stackToDraw.get();
        return stack == null ? null : stack.createItemStack();
    }
}
