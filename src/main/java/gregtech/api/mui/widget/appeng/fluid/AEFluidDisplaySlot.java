package gregtech.api.mui.widget.appeng.fluid;

import gregtech.api.mui.sync.appeng.AEFluidSyncHandler;
import gregtech.api.mui.widget.appeng.AEDisplaySlot;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
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
            tooltip.addLine(IKey.str(stack.getLocalizedName()));
            tooltip.addLine(IKey.str("%,d L", stack.amount));

            for (String fluidTooltip : FluidTooltipUtil.getFluidTooltip(stack)) {
                if (fluidTooltip.isEmpty()) continue;
                tooltip.addLine(IKey.str(fluidTooltip));
            }

            GTFluidSlot.addIngotMolFluidTooltip(stack, tooltip);
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
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        IAEFluidStack stock = getSyncHandler().getStock(index);
        if (stock != null) {
            FluidStack stack = stock.getFluidStack();
            RenderUtil.drawFluidForGui(stack, stack.amount, 1, 1, 17, 17);

            String amount = TextFormattingUtil.formatLongToCompactString(stack.amount, 4);
            RenderUtil.renderTextFixedCorner(amount, 17d, 18d, 0xFFFFFF, true, 0.5f);
        }

        if (isHovering()) {
            drawSlotOverlay();
        }
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEFluidStack stock = getSyncHandler().getStock(index);
        return stock == null ? null : stock.getFluidStack();
    }
}
