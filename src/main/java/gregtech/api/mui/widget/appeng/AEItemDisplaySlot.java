package gregtech.api.mui.widget.appeng;

import gregtech.api.mui.sync.appeng.AEItemSyncHandler;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEItemDisplaySlot extends AEDisplaySlot<IAEItemStack> {

    public AEItemDisplaySlot() {
        super();
        size(18, 18);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        IAEItemStack stack = getSyncHandler().getStock();
        if (stack != null) {
            tooltip.addFromItem(stack.createItemStack());
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
        IAEItemStack stock = getSyncHandler().getStock();
        if (stock != null) {
            ItemStack stack = stock.createItemStack();
            if (!stack.isEmpty()) {
                stack.setCount(1);
                RenderUtil.renderItem(stack, 1, 1, 16f, 16f);
            }

            String amount = TextFormattingUtil.formatLongToCompactString(stock.getStackSize(), 4);
            RenderUtil.renderTextFixedCorner(amount, 17d, 18d, 0xFFFFFF, true, 0.5f);
        }
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEItemStack stock = getSyncHandler().getStock();
        return stock == null ? null : stock.createItemStack();
    }
}
