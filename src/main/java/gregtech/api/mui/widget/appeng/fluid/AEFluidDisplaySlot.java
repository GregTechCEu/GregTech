package gregtech.api.mui.widget.appeng.fluid;

import appeng.api.storage.data.IAEFluidStack;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.screen.RichTooltip;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import gregtech.api.mui.sync.appeng.AEFluidSyncHandler;
import gregtech.api.mui.widget.appeng.AEDisplaySlot;

import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEFluidDisplaySlot extends AEDisplaySlot<IAEFluidStack> {

    public AEFluidDisplaySlot() {
        super();
        tooltipAutoUpdate(true);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        IAEFluidStack stock = getSyncHandler().getStock();
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
        IAEFluidStack stock = getSyncHandler().getStock();
        if (stock != null) {
            FluidStack stack = stock.getFluidStack();
            RenderUtil.drawFluidForGui(stack, stack.amount, 1, 1, 17, 17);

            String amount = TextFormattingUtil.formatLongToCompactString(stock.getStackSize(), 4);
            RenderUtil.renderTextFixedCorner(amount, 17d, 18d, 0xFFFFFF, true, 0.5f);
        }
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEFluidStack stock = getSyncHandler().getStock();
        return stock == null ? null : stock.getFluidStack();
    }
}
