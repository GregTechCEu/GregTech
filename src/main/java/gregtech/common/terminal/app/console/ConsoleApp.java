package gregtech.common.terminal.app.console;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.common.terminal.app.console.widget.MachineConsoleWidget;
import gregtech.common.terminal.app.console.widget.MachineSceneWidget;
import net.minecraft.nbt.NBTTagCompound;

public class ConsoleApp extends AbstractApplication {
    public static final TextureArea ICON = TextureArea.fullImage("textures/gui/terminal/console/icon.png");

    public ConsoleApp() {
        super("console", ICON);
    }

    @Override
    protected AbstractApplication initApp(boolean isClient, NBTTagCompound nbt) {
        if (os.clickPos == null) { // 333 232
            TerminalDialogWidget.showInfoDialog(os,
                    "terminal.dialog.notice",
                    "terminal.console.notice",
                    () -> os.closeApplication(os.getFocusApp(), isClient)).open();
            return this;
        }
        MachineConsoleWidget consoleWidget = new MachineConsoleWidget(200, 16, 133, 200);
        this.addWidget(consoleWidget);
        if (isClient) {
            this.addWidget(0, new MachineSceneWidget(0, 16, 200, 200, os.clickPos).setOnSelected(consoleWidget::setFocus));
            this.addWidget(new ImageWidget(0, 0, 333, 16, GuiTextures.UI_FRAME_SIDE_UP));
            this.addWidget(new ImageWidget(0, 216, 333, 16, GuiTextures.UI_FRAME_SIDE_DOWN));
        } else {
            this.addWidget(0, new WidgetGroup()); // placeholder
        }
        return this;
    }
}
