package gregtech.common.items.behaviors.filter;

import gregtech.common.covers.filter.FilterTypeRegistry;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

public class SmartFilterUIManager extends BaseFilterUIManager {

    @Override
    public ModularPanel buildUI(HandGuiData guiData, GuiSyncManager guiSyncManager) {
        var filter = FilterTypeRegistry.getItemFilterForStack(guiData.getUsedItemStack());
        return createBasePanel(filter.getContainerStack()).height(166)
                .child(filter.createWidgets(guiSyncManager).left(7).top(22))
                .child(SlotGroupWidget.playerInventory().bottom(7).left(7));
    }
}
