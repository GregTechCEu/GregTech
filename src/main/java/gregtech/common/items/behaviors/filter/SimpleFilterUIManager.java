package gregtech.common.items.behaviors.filter;

import gregtech.common.covers.filter.FilterTypeRegistry;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.layout.Column;

public class SimpleFilterUIManager extends BaseFilterUIManager{

    @Override
    public ModularPanel buildUI(HandGuiData guiData, GuiSyncManager guiSyncManager) {
        var filter = FilterTypeRegistry.getItemFilterForStack(guiData.getUsedItemStack());
        return createBasePanel(filter.getContainerStack())
                .child(new Column().coverChildren().top(4).left(4)
                        .child(IKey.str("Settings").asWidget())
                        .child(filter.createWidgets(guiSyncManager)));
    }
}
