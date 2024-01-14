package gregtech.common.covers.filter;

import net.minecraft.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Deprecated
public class ItemFilterWrapper {
    private final ItemFilterContainer container;

    public ItemFilterWrapper(ItemFilterContainer container) {
        this.container = container;
    }

    public void initUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        container.initUI(y, widgetGroup);
    }

    public void blacklistUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        container.blacklistUI(y, widgetGroup, showBlacklistButton);
    }

    public void setItemFilter(ItemFilter itemFilter) {
        container.setItemFilter(itemFilter);
    }

    public ItemFilter getItemFilter() {
        return container.getItemFilter();
    }

    public void setOnFilterInstanceChange(Runnable onFilterInstanceChange) {
        this.container.setOnFilterInstanceChange(onFilterInstanceChange);
    }

    public void onFilterInstanceChange() {
        this.container.onFilterInstanceChange();
    }

    public void setMaxStackSize(int maxStackSize) {
        container.setMaxStackSize(maxStackSize);
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        container.setBlacklistFilter(blacklistFilter);
    }

    public boolean isBlacklistFilter() {
        return container.isBlacklistFilter();
    }

    public int getMaxStackSize() {
        return container.getMaxStackSize();
    }

    public boolean showGlobalTransferLimitSlider() {
        return container.showGlobalTransferLimitSlider();
    }

    public int getSlotTransferLimit(int matchSlot, int globalTransferLimit) {
        return container.getSlotTransferLimit(matchSlot);
    }

    public boolean testItemStack(ItemStack itemStack) {
        return container.testItemStack(itemStack);
    }
}
