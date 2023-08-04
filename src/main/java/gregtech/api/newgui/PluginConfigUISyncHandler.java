package gregtech.api.newgui;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import gregtech.api.items.behavior.MonitorPluginBaseBehavior;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;

import java.util.function.Supplier;

public class PluginConfigUISyncHandler extends PanelSyncHandler {

    private final MetaTileEntityMonitorScreen screen;
    private final Supplier<MonitorPluginBaseBehavior> pluginGetter;

    public PluginConfigUISyncHandler(ModularPanel mainPanel, MetaTileEntityMonitorScreen screen, Supplier<MonitorPluginBaseBehavior> pluginGetter) {
        super(mainPanel);
        this.screen = screen;
        this.pluginGetter = pluginGetter;
    }

    @Override
    public ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
        ModularPanel panel = this.pluginGetter.get().createPluginConfigUI(syncManager, this.screen, null)
                .relative(mainPanel)
                .top(0)
                .rightRel(1f);
        return panel.child(new ButtonWidget<>()
                        .size(10)
                        .top(5).right(5)
                        .overlay(IKey.str("x"))
                        .onMousePressed(mouseButton -> {
                            panel.animateClose();
                            return true;
                        }));
    }
}
