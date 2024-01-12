package gregtech.common.items.behaviors.filter;

import gregtech.common.covers.filter.FilterTypeRegistry;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;

public class SimpleFilterUIManager extends BaseFilterUIManager{

    @Override
    public ModularPanel buildUI(HandGuiData guiData, GuiSyncManager guiSyncManager) {
        var filter = FilterTypeRegistry.getItemFilterForStack(guiData.getUsedItemStack());
        if (filter == null) return ERROR;

        return filter.createPanel(guiSyncManager).padding(4)
                .child(filter.createWidgets(guiSyncManager).align(Alignment.TopLeft));
    }
}
