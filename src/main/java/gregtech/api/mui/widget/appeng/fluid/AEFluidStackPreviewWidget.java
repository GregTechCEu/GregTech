package gregtech.api.mui.widget.appeng.fluid;

import gregtech.api.mui.widget.appeng.AEStackPreviewWidget;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.KeyUtil;

import net.minecraftforge.fluids.FluidStack;

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
        IAEFluidStack stack = stackToDraw.get();
        if (stack == null) return;

        FluidStack fluidStack = stack.getFluidStack();
        tooltip.addLine(KeyUtil.fluid(fluidStack));
        FluidTooltipUtil.fluidInfo(fluidStack, tooltip, false, true, false);
        tooltip.addLine(FluidTooltipUtil.getFluidModNameKey(fluidStack));
    }

    @Override
    public void draw(@Nullable IAEFluidStack stackToDraw, int x, int y, int width, int height) {
        if (stackToDraw == null) return;
        GuiDraw.drawFluidTexture(stackToDraw.getFluidStack(), x, y, width, height, 0.0f);
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEFluidStack stack = stackToDraw.get();
        return stack == null ? null : stack.getFluidStack();
    }
}
