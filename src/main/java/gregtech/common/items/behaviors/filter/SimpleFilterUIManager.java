package gregtech.common.items.behaviors.filter;

import gregtech.common.covers.filter.FilterTypeRegistry;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

public class SimpleFilterUIManager extends BaseFilterUIManager {

    @Override
    public ModularPanel buildUI(HandGuiData guiData, GuiSyncManager guiSyncManager) {
        var filter = FilterTypeRegistry.getFilterForStack(guiData.getUsedItemStack());
        return createBasePanel(filter.getContainerStack()).padding(4).height(166)
                .child(filter.createWidgets(guiSyncManager).top(22).left(7))
                .child(SlotGroupWidget.playerInventory().left(7));
    }
}
