package gregtech.common.terminal.app;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.VirtualTankRegistry;
import net.minecraftforge.fluids.IFluidTank;

import java.util.Map;
import java.util.stream.Collectors;

public class VirtualTankApp extends AbstractApplication {

    private WidgetGroup widgetGroup;
    private Map<String, IFluidTank> tankMap;

    public VirtualTankApp() {
        super("vtank_viewer");
    }

    @Override
    public AbstractApplication initApp() {
        this.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
        this.addWidget(new LabelWidget(10, 10, "terminal.vtank_viewer.title", -1));
        this.addWidget(new RectButtonWidget(216, 7, 110, 18)
                .setClickListener(this::onRefreshClick)
                .setIcon(new TextTexture("terminal.vtank_viewer.refresh", -1))
                .setFill(TerminalTheme.COLOR_B_2.getColor()));
        widgetGroup = new DraggableScrollableWidgetGroup(10, 30, 313, 195)
                .setDraggable(true)
                .setYScrollBarWidth(3)
                .setYBarStyle(null, TerminalTheme.COLOR_F_1);
        this.addWidget(widgetGroup);
        refresh();
        return this;
    }

    private void refresh() {
        tankMap = VirtualTankRegistry.getTankMap();
        widgetGroup.clearAllWidgets();
        int cy = 0;
        for (String key : tankMap.keySet().stream().sorted().collect(Collectors.toList())) {
            widgetGroup.addWidget(new TankWidget(tankMap.get(key), 5, cy, 18, 18)
                    .setAlwaysShowFull(true)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
            widgetGroup.addWidget(new LabelWidget(33, cy + 5, key, -1)
                    .setWidth(180));
            widgetGroup.addWidget(new LabelWidget(216, cy + 5, "terminal.vtank_viewer.refs", -1,
                    // since for gameplay usage, -1 and 0 refs are equivalent, and -1 could be a bit confusing
                    new Object[]{Math.max(VirtualTankRegistry.getRefs(key), 0)})
                    .setWidth(101));
            cy += 23;
        }
    }

    private void onRefreshClick(ClickData clickData) {
        refresh();
    }
}
