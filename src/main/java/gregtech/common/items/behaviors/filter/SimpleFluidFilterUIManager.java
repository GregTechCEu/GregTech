package gregtech.common.items.behaviors.filter;

import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.mui.GTGuis;
import gregtech.common.covers.filter.BaseFilter;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

import gregtech.common.covers.filter.readers.SimpleFluidFilterReader;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class SimpleFluidFilterUIManager extends BaseFilterUIManager {

//    @Override
//    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
//        var filter = BaseFilter.getFilterFromStack(guiData.getUsedItemStack());
//        return createBasePanel(filter.getContainerStack())
//                .height(166)
//                .child(filter.createWidgets(guiSyncManager)
//                        .top(22)
//                        .left(7))
//                .child(SlotGroupWidget.playerInventory(true));
//    }

    @Override
    public @NotNull ModularPanel createPopupPanel(ItemStack stack, PanelSyncManager syncManager, String panelName) {
        return super.createPopupPanel(stack, syncManager, panelName)
//        return GTGuis.createPopupPanel(panelName, 98, 81, false)
                .padding(4)
//                .child(CoverWithUI.createTitleRow(stack))
//                .child(createWidgets(stack, syncManager).top(22))
                ;
    }

    @Override
    public @NotNull ModularPanel createPanel(ItemStack stack, PanelSyncManager syncManager) {
        return GTGuis.createPanel(stack, 176, 168);
    }

    @Override
    public @NotNull Widget<?> createWidgets(ItemStack stack, PanelSyncManager syncManager) {
        SimpleFluidFilterReader filterReader = (SimpleFluidFilterReader) getFilterReader(stack);
        return Flow.row().coverChildrenHeight().widthRel(1f)
                .child(SlotGroupWidget.builder()
                        .matrix("FFF",
                                "FFF",
                                "FFF")
                        .key('F', i -> new GTFluidSlot()
                                .syncHandler(GTFluidSlot.sync(filterReader.getFluidTank(i))
                                        .phantom(true)
                                        .showAmountOnSlot(filterReader::shouldShowAmount)
                                        .showAmountInTooltip(filterReader::shouldShowAmount)))
                        .build().marginRight(4))
                .child(createBlacklistUI(stack));
    }
}
