package gregtech.api.newgui;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import gregtech.api.cover.filter.FilterHolder;

public class FilterPanelSyncHandler extends PanelSyncHandler {

    private final FilterHolder<?, ?> filterHolder;

    public FilterPanelSyncHandler(ModularPanel mainPanel, FilterHolder<?, ?> filterHolder) {
        super(mainPanel);
        this.filterHolder = filterHolder;
    }

    @Override
    public ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
        return this.filterHolder.buildFilterPanel(mainPanel, syncManager, this);
    }
}
