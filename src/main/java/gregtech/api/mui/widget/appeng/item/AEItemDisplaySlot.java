package gregtech.api.mui.widget.appeng.item;

import gregtech.api.mui.sync.appeng.AEItemSyncHandler;
import gregtech.api.mui.widget.appeng.AEDisplaySlot;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEItemDisplaySlot extends AEDisplaySlot<IAEItemStack> {

    public AEItemDisplaySlot(int index) {
        super(index);
        tooltipAutoUpdate(true);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        WrappedItemStack stock = (WrappedItemStack) getSyncHandler().getStock(index);
        if (stock != null) {
            tooltip.addFromItem(stock.getDefinition());
        }
    }

    @Override
    public @NotNull AEItemSyncHandler getSyncHandler() {
        return (AEItemSyncHandler) super.getSyncHandler();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof AEItemSyncHandler;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        WrappedItemStack stock = (WrappedItemStack) getSyncHandler().getStock(index);
        if (stock != null) {
            ItemStack stack = stock.createItemStack();
            RenderUtil.renderItem(stack, 1, 1, 16f, 16f);
            RenderUtil.renderTextFixedCorner(TextFormattingUtil.formatLongToCompactString(stock.getStackSize(), 4), 17d,
                    18d, 0xFFFFFF, true, 0.5f);
        }

        RenderUtil.handleSlotOverlay(this, widgetTheme);
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEItemStack stock = getSyncHandler().getStock(index);
        return stock == null ? null : stock.createItemStack();
    }
}
