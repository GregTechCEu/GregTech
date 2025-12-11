package gregtech.common.items.behaviors.filter;

import gregtech.api.mui.GTGuis;
import gregtech.common.covers.filter.readers.SimpleFluidFilterReader;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

public class SimpleFluidFilterUIManager extends BaseFilterUIManager {

    @Override
    public @NotNull ModularPanel createPopupPanel(ItemStack stack, PanelSyncManager syncManager, String panelName) {
        return super.createPopupPanel(stack, syncManager, panelName)
                .padding(4);
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
