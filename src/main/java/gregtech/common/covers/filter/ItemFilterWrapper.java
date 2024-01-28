package gregtech.common.covers.filter;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @deprecated use {@link ItemFilterContainer}
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2.10")
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
        container.setFilter(itemFilter);
    }

    public ItemFilter getItemFilter() {
        return container.getFilter();
    }

    public void setMaxStackSize(int maxStackSize) {
        container.setMaxTransferSize(maxStackSize);
    }

    public int getMaxStackSize() {
        return container.getMaxTransferSize();
    }

    public boolean showGlobalTransferLimitSlider() {
        return container.showGlobalTransferLimitSlider();
    }

    public int getSlotTransferLimit(int matchSlot, int globalTransferLimit) {
        return container.getTransferLimit(matchSlot);
    }

    public boolean testItemStack(ItemStack itemStack) {
        return container.test(itemStack);
    }
}
