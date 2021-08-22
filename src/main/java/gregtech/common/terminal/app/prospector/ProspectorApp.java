package gregtech.common.terminal.app.prospector;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.terminal.app.prospector.widget.WidgetOreList;
import gregtech.common.terminal.app.prospector.widget.WidgetProspectingMap;
import net.minecraft.nbt.NBTTagCompound;

public class ProspectorApp extends AbstractApplication {

    public ProspectorApp() {
        super("prospector", GuiTextures.SCANNER_OVERLAY);
    }

    @Override
    public AbstractApplication createApp(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) { //333, 232
        ProspectorApp app = new ProspectorApp();
        int chunkRadius = 7;
        int offset = (232 - 32 * 7 + 16) / 2;
        WidgetOreList widgetItemFluidList = new WidgetOreList(32 * chunkRadius - 16, offset, 333 - 32 * chunkRadius  + 16, 232 - 2 * offset);
        WidgetProspectingMap widgetProspectingMap = new WidgetProspectingMap(0, offset, chunkRadius, widgetItemFluidList,
                0, 1);
        app.addWidget(new ImageWidget(0, 0, 333, 232, TerminalTheme.COLOR_B_2));
        app.addWidget(widgetProspectingMap);
        app.addWidget(widgetItemFluidList);
        return app;
    }
}
