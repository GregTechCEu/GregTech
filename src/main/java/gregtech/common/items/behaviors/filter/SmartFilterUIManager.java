package gregtech.common.items.behaviors.filter;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.common.covers.filter.BaseFilter;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

import gregtech.common.covers.filter.SmartItemFilter;

import gregtech.common.covers.filter.readers.SmartItemFilterReader;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class SmartFilterUIManager extends BaseFilterUIManager {

//    @Override
//    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
//        var filter = BaseFilter.getFilterFromStack(guiData.getUsedItemStack());
//        return createBasePanel(filter.getContainerStack()).height(166)
//                .child(filter.createWidgets(guiSyncManager).left(7).top(22))
//                .child(SlotGroupWidget.playerInventory(true));
//    }

    @Override
    public @NotNull ModularPanel createPopupPanel(ItemStack stack, PanelSyncManager syncManager, String panelName) {
        return super.createPopupPanel(stack, syncManager, panelName)
                .width(98 + 27)
//        return GTGuis.createPopupPanel(panelName, 98 + 27, 81, false)
//                .child(CoverWithUI.createTitleRow(stack))
//                .child(createWidgets(stack, syncManager).top(22).left(4))
                ;
    }

    @Override
    public @NotNull ModularPanel createPanel(ItemStack stack, PanelSyncManager syncManager) {
        return GTGuis.createPanel("smart_item_filter", 100, 100).padding(7);
    }

    @Override
    public @NotNull Widget<?> createWidgets(ItemStack stack, PanelSyncManager syncManager) {
        SmartItemFilterReader filterReader = (SmartItemFilterReader) getFilterReader(stack);
        var filterMode = new EnumSyncValue<>(SmartItemFilter.SmartFilteringMode.class, filterReader::getFilteringMode,
                filterReader::setFilteringMode);
        syncManager.syncValue("filter_mode", filterMode);

        return Flow.row().coverChildren()
                .child(Flow.column().coverChildren().marginRight(4)
                        .child(createFilterModeButton(filterMode, SmartItemFilter.SmartFilteringMode.ELECTROLYZER))
                        .child(createFilterModeButton(filterMode, SmartItemFilter.SmartFilteringMode.CENTRIFUGE))
                        .child(createFilterModeButton(filterMode, SmartItemFilter.SmartFilteringMode.SIFTER)))
                .child(createBlacklistUI(stack));
    }

    private Widget<ToggleButton> createFilterModeButton(EnumSyncValue<SmartItemFilter.SmartFilteringMode> value,
                                                        SmartItemFilter.SmartFilteringMode mode) {
        return new ToggleButton().height(18).width(18 * 5)
                .value(boolValueOf(value, mode))
                .background(GTGuiTextures.MC_BUTTON)
                .selectedBackground(GTGuiTextures.MC_BUTTON_DISABLED)
                .overlay(IKey.lang(mode.getName()).color(Color.WHITE.darker(1)));
    }

    protected <T extends Enum<T>> BoolValue.Dynamic boolValueOf(EnumSyncValue<T> syncValue, T value) {
        return new BoolValue.Dynamic(() -> syncValue.getValue() == value, $ -> syncValue.setValue(value));
    }
}
