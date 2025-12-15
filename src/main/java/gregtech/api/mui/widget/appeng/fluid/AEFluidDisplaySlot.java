package gregtech.api.mui.widget.appeng.fluid;

import gregtech.api.mui.sync.appeng.AEFluidSyncHandler;
import gregtech.api.mui.widget.appeng.AEDisplaySlot;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;

import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEFluidDisplaySlot extends AEDisplaySlot<IAEFluidStack> {

    public AEFluidDisplaySlot(int index) {
        super(index);
        tooltipAutoUpdate(true);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        IAEFluidStack stock = getSyncHandler().getStock(index);
        if (stock != null) {
            FluidStack stack = stock.getFluidStack();
            tooltip.addLine(KeyUtil.fluid(stack));
            FluidTooltipUtil.fluidInfo(stack, tooltip, false, true, true);
            tooltip.addLine(FluidTooltipUtil.getFluidModNameKey(stack));
        }
    }

    @Override
    public @NotNull AEFluidSyncHandler getSyncHandler() {
        return (AEFluidSyncHandler) super.getSyncHandler();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof AEFluidSyncHandler;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IAEFluidStack stock = getSyncHandler().getStock(index);
        if (stock != null) {
            GuiDraw.drawFluidTexture(stock.getFluidStack(), 1, 1, getArea().w() - 2, getArea().h() - 2, 0);
            RenderUtil.renderTextFixedCorner(TextFormattingUtil.formatLongToCompactString(stock.getStackSize(), 4), 17d,
                    18d, 0xFFFFFF, true, 0.5f);
        }

        RenderUtil.handleSlotOverlay(this, widgetTheme);
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEFluidStack stock = getSyncHandler().getStock(index);
        return stock == null ? null : stock.getFluidStack();
    }
}
