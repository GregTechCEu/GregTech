package gregtech.common.terminal.app.console;

import gregtech.api.guiOld.GuiTextures;
import gregtech.api.guiOld.widgets.ImageWidget;
import gregtech.api.guiOld.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.MachineSceneWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import net.minecraft.tileentity.TileEntity;

public class ConsoleApp extends AbstractApplication {

    public ConsoleApp() {
        super("console");
    }

    private MetaTileEntityHolder getMTE() {
        if (os.clickPos != null) {
            TileEntity te = os.getModularUI().entityPlayer.world.getTileEntity(os.clickPos);
            if (te instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) te).isValid()) {
                return (MetaTileEntityHolder) te;
            }
        }
        return null;
    }

    @Override
    public AbstractApplication initApp() {
        if (getMTE() == null) { // 333 232
            TerminalDialogWidget.showInfoDialog(os,
                    "terminal.dialog.notice",
                    "terminal.console.notice",
                    () -> os.closeApplication(this, isClient)).open();
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
