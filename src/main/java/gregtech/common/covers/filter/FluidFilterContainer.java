package gregtech.common.covers.filter;

import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FluidFilterContainer extends BaseFilterContainer {

    public FluidFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
    }

    /** @deprecated uses old builtin MUI */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void initUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new gregtech.api.gui.widgets.LabelWidget(10, y, "cover.pump.fluid_filter.title"));
        widgetGroup.accept(new gregtech.api.gui.widgets.SlotWidget(this, 0, 10, y + 15)
                .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT,
                        gregtech.api.gui.GuiTextures.FILTER_SLOT_OVERLAY));

        this.initFilterUI(y + 15, widgetGroup);
        this.blacklistUI(y + 15, widgetGroup, () -> true);
    }

    /** @deprecated uses old builtin MUI */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void initFilterUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new WidgetGroupFluidFilter(y, this::getFilter, this::showGlobalTransferLimitSlider));
    }

    /** @deprecated uses old builtin MUI */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void blacklistUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        gregtech.api.gui.widgets.ServerWidgetGroup blacklistButton = new gregtech.api.gui.widgets.ServerWidgetGroup(
                this::hasFilter);
        blacklistButton.addWidget(new gregtech.api.gui.widgets.ToggleButtonWidget(144, y, 18, 18,
                gregtech.api.gui.GuiTextures.BUTTON_BLACKLIST,
                this::isBlacklistFilter, this::setBlacklistFilter).setPredicate(showBlacklistButton)
                        .setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(blacklistButton);
    }

    @Override
    protected boolean isItemValid(ItemStack stack) {
        var filter = BaseFilter.getFilterFromStack(stack);
        return filter != BaseFilter.ERROR_FILTER && filter.getType() == IFilter.FilterType.FLUID;
    }

    @Override
    protected @NotNull IKey getFilterKey() {
        return IKey.lang(() -> hasFilter() ?
                getFilterStack().getTranslationKey() + ".name" :
                "metaitem.fluid_filter.name");
    }
}
