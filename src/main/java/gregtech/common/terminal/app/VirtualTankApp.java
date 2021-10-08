package gregtech.common.terminal.app;

import gregtech.api.gui.GuiTextures;
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
        this.addWidget(new LabelWidget(0, 0, "terminal.vtank_viewer.title", -1));
        this.addWidget(new RectButtonWidget(200, 0, 100, 18, 3)
        .setClickListener(this::onRefreshClick));
        widgetGroup = new DraggableScrollableWidgetGroup(5, 30, 300, 200);
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
            widgetGroup.addWidget(new LabelWidget(30, cy + 5, key, -1));
            widgetGroup.addWidget(new LabelWidget(0, cy + 5, String.valueOf(VirtualTankRegistry.getRefs(key)), -1));
            cy += 20;
        }
    }

    private void onRefreshClick(ClickData clickData) {
        refresh();
    }
}
