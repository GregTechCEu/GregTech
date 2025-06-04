package gregtech.api.mui.widget.appeng.fluid;

import gregtech.api.mui.widget.appeng.AEStackPreviewWidget;
import gregtech.api.util.KeyUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.RichTooltip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

class AEFluidStackPreviewWidget extends AEStackPreviewWidget<IAEFluidStack> {

    public AEFluidStackPreviewWidget(@NotNull Supplier<IAEFluidStack> stackToDraw) {
        super(stackToDraw);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        if (stackToDraw.get() instanceof WrappedFluidStack wrappedFluidStack) {
            KeyUtil.fluidInfo(wrappedFluidStack.getDelegate(), tooltip, false, true, false);
        }
    }

    @Override
    public void draw(@Nullable IAEFluidStack stackToDraw, int x, int y, int width, int height) {
        if (stackToDraw instanceof WrappedFluidStack wrappedFluidStack) {
            GuiDraw.drawFluidTexture(wrappedFluidStack.getDelegate(), x, y, width, height, 0.0f);
        }
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEFluidStack stack = stackToDraw.get();
        return stack == null ? null : stack.getFluidStack();
    }
}
