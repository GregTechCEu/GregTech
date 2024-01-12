package gregtech.common.items.behaviors.filter;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;

import gregtech.common.covers.filter.FilterTypeRegistry;

public class SmartFilterUIManager extends BaseFilterUIManager{

    @Override
    public ModularPanel buildUI(HandGuiData guiData, GuiSyncManager guiSyncManager) {
        var filter = FilterTypeRegistry.getItemFilterForStack(guiData.getUsedItemStack());
        return filter.createPanel(guiSyncManager).padding(4)
                .child(filter.createWidgets(guiSyncManager));
    }
}
