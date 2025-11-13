package gregtech.common.covers.filter;

import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ItemFilterContainer extends BaseFilterContainer {

    public ItemFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
    }

    /** @deprecated uses old builtin MUI */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void initUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new LabelWidget(10, y, "cover.conveyor.item_filter.title"));
        widgetGroup.accept(new SlotWidget(this, 0, 10, y + 15)
                .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT,
                        gregtech.api.gui.GuiTextures.FILTER_SLOT_OVERLAY));

        this.initFilterUI(y + 38, widgetGroup);
    }

    /** @deprecated uses old builtin MUI */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void initFilterUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new WidgetGroupItemFilter(y, this::getFilter));
    }

    /** @deprecated uses old builtin MUI */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void blacklistUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        ServerWidgetGroup blacklistButton = new ServerWidgetGroup(this::hasFilter);
        blacklistButton.addWidget(new ToggleButtonWidget(144, y, 20, 20, gregtech.api.gui.GuiTextures.BUTTON_BLACKLIST,
                this::isBlacklistFilter, this::setBlacklistFilter).setPredicate(showBlacklistButton)
                        .setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(blacklistButton);
    }

    @Override
    protected boolean isItemValid(ItemStack stack) {
        var filter = BaseFilter.getFilterFromStack(stack);
        return filter != BaseFilter.ERROR_FILTER && filter.getType() == IFilter.FilterType.ITEM;
    }

    @Override
    protected @NotNull IKey getFilterKey() {
        return IKey.lang(() -> hasFilter() ?
                getFilterStack().getTranslationKey() + ".name" :
                "metaitem.item_filter.name");
    }
}
