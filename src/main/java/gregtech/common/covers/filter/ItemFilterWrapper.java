package gregtech.common.covers.filter;

import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.MultiChildWidget;
import com.cleanroommc.modularui.common.widget.Widget;
import gregtech.api.guiOld.GuiTextures;
import gregtech.api.guiOld.widgets.ServerWidgetGroup;
import gregtech.api.guiOld.widgets.ToggleButtonWidget;
import gregtech.api.util.IDirtyNotifiable;
import net.minecraft.item.ItemStack;

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

    public void initUI(int y, Consumer<gregtech.api.guiOld.Widget> widgetGroup) {
        widgetGroup.accept(new WidgetGroupItemFilter(y, this::getItemFilter));
    }

    public void blacklistUI(int y, Consumer<gregtech.api.guiOld.Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        ServerWidgetGroup blacklistButton = new ServerWidgetGroup(() -> getItemFilter() != null);
        blacklistButton.addWidget(new ToggleButtonWidget(144, y, 20, 20, GuiTextures.BUTTON_BLACKLIST,
                this::isBlacklistFilter, this::setBlacklistFilter).setPredicate(showBlacklistButton).setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(blacklistButton);
    }

    public Widget createFilterUI(UIBuildContext buildContext) {
        return new MultiChildWidget()
                .addChild(new CycleButtonWidget()
                        .setToggle(this::isBlacklistFilter, this::setBlacklistFilter)
                        .setTexture(gregtech.api.gui.GuiTextures.BUTTON_BLACKLIST)
                        .setBackground(gregtech.api.gui.GuiTextures.BASE_BUTTON)
                        .setSize(18, 18)
                        .setPos(126, 0));
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
