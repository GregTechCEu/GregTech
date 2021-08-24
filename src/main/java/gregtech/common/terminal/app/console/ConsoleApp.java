package gregtech.common.terminal.app.console;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalDialogWidget;
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
                    ()-> os.closeApplication(os.getFocusApp(), isClient)).open();
            return this;
        }
        this.addWidget(new MachineSceneWidget(0, 0, 200, 200, os.clickPos, isClient));
        return this;
    }
}
