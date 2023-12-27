package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.ToggleButtonWidget;

import net.minecraft.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ItemFilterWrapper {
    private ItemFilterContainer container;

    public ItemFilterWrapper(ItemFilterContainer container) {
        this.container = container;
    }

    public void initUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new WidgetGroupItemFilter(y, this::getItemFilter));
    }

    public void blacklistUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        ServerWidgetGroup blacklistButton = new ServerWidgetGroup(() -> getItemFilter() != null);
        blacklistButton.addWidget(new ToggleButtonWidget(144, y, 20, 20, GuiTextures.BUTTON_BLACKLIST,
                this::isBlacklistFilter, this::setBlacklistFilter).setPredicate(showBlacklistButton)
                        .setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(blacklistButton);
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

    public ItemFilter.MatchResult<Integer> matchItemStack(ItemStack itemStack) {
        return container.matchItemStack(itemStack);
    }

    public ItemFilter.MatchResult<Integer> matchItemStack(ItemStack itemStack, boolean whitelist) {
        return container.matchItemStack(itemStack, whitelist);
    }

    public boolean testItemStack(ItemStack itemStack) {
        return container.testItemStack(itemStack);
    }
}
