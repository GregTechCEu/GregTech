package gregtech.common.items.behaviors.filter;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;

import gregtech.common.covers.filter.FilterTypeRegistry;

public class SmartFilterUIManager extends BaseFilterUIManager {

    @Override
    public ModularPanel buildUI(HandGuiData guiData, GuiSyncManager guiSyncManager) {
        var filter = FilterTypeRegistry.getItemFilterForStack(guiData.getUsedItemStack());
        return createBasePanel(filter.getContainerStack())
                .child(IKey.str("Settings").asWidget()
                        .margin(4).height(14).align(Alignment.TopLeft))
                .child(filter.createWidgets(guiSyncManager).left(7).top(18));
    }
}
