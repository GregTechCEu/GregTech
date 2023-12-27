package gregtech.common.covers.filter;

import com.cleanroommc.modularui.widget.Widget;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ItemFilterWrapper {

    private static final Object MATCH_RESULT_TRUE = new Object();
    private final IDirtyNotifiable dirtyNotifiable;
    private boolean isBlacklistFilter = false;
    private int maxStackSize = 1;
    private ItemFilter currentItemFilter;
    private Runnable onFilterInstanceChange;

    public ItemFilterWrapper(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
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

    public Widget<?> initUI() {
        return getItemFilter().initUI();
    }

    public Widget<CycleButtonWidget> blacklistUI() {
        return new CycleButtonWidget().setEnabledIf(row1 -> getItemFilter() != null)
                .value(new BoolValue.Dynamic(this::isBlacklistFilter, this::setBlacklistFilter))
                .tooltip(tooltip -> tooltip.addLine(IKey.lang("cover.filter.blacklist")));
    }

    public void setItemFilter(ItemFilter itemFilter) {
        this.currentItemFilter = itemFilter;
        if (currentItemFilter != null) {
            currentItemFilter.setDirtyNotifiable(dirtyNotifiable);
        }
        if (onFilterInstanceChange != null) {
            this.onFilterInstanceChange.run();
        }
    }

    public ItemFilter getItemFilter() {
        return currentItemFilter;
    }

    public void setOnFilterInstanceChange(Runnable onFilterInstanceChange) {
        this.onFilterInstanceChange = onFilterInstanceChange;
    }

    public void onFilterInstanceChange() {
        if (currentItemFilter != null) {
            currentItemFilter.setMaxStackSize(getInternalMaxStackSize());
        }
        dirtyNotifiable.markAsDirty();
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        onFilterInstanceChange();
        dirtyNotifiable.markAsDirty();
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        isBlacklistFilter = blacklistFilter;
        onFilterInstanceChange();
        dirtyNotifiable.markAsDirty();
    }

    public boolean isBlacklistFilter() {
        return isBlacklistFilter;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    private int getInternalMaxStackSize() {
        if (isBlacklistFilter()) {
            return 1;
        } else {
            return getMaxStackSize();
        }
    }

    public boolean showGlobalTransferLimitSlider() {
        return isBlacklistFilter() || currentItemFilter == null || currentItemFilter.showGlobalTransferLimitSlider();
    }

    public int getSlotTransferLimit(Object matchSlot, int globalTransferLimit) {
        if (isBlacklistFilter() || currentItemFilter == null) {
            return globalTransferLimit;
        }
        return currentItemFilter.getSlotTransferLimit(matchSlot, globalTransferLimit);
    }

    public Object matchItemStack(ItemStack itemStack) {
        Object originalResult;
        if (currentItemFilter == null) {
            originalResult = MATCH_RESULT_TRUE;
        } else {
            originalResult = currentItemFilter.matchItemStack(itemStack);
        }
        if (isBlacklistFilter()) {
            originalResult = originalResult == null ? MATCH_RESULT_TRUE : null;
        }
        return originalResult;
    }

    public Object matchItemStack(ItemStack itemStack, boolean whitelist) {
        Object originalResult;
        if (currentItemFilter == null) {
            originalResult = MATCH_RESULT_TRUE;
        } else {
            originalResult = currentItemFilter.matchItemStack(itemStack);
        }
        if (!whitelist) {
            originalResult = originalResult == null ? MATCH_RESULT_TRUE : null;
        }
        return originalResult;
    }

    public boolean testItemStack(ItemStack itemStack) {
        return matchItemStack(itemStack) != null;
    }
}
