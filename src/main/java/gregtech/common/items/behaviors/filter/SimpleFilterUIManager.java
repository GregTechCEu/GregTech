package gregtech.common.items.behaviors.filter;

import gregtech.common.covers.filter.BaseFilter;

import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

public class SimpleFilterUIManager extends BaseFilterUIManager {

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager panelSyncManager,
                                UISettings settings) {
        var filter = BaseFilter.getFilterFromStack(guiData.getUsedItemStack());
        return createBasePanel(filter.getContainerStack())
                .height(166)
                .child(filter.createWidgets(panelSyncManager)
                        .top(22)
                        .left(7))
                .child(SlotGroupWidget.playerInventory(true));
    }
}
