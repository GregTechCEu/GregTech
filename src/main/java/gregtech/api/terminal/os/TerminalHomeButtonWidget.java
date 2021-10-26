package gregtech.api.terminal.os;

import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.input.Mouse;

public class TerminalHomeButtonWidget extends CircleButtonWidget {
    private final TerminalOSWidget os;
    private int mouseClickTime = -1;

    public TerminalHomeButtonWidget(TerminalOSWidget os) {
        super(351, 115, 11, 2, 18);
        this.os = os;
        this.setColors(0, TerminalTheme.COLOR_F_1.getColor(), 0);
    }

    private void click(ClickData clickData) {
        os.callMenu(clickData.isClient);
    }

    private void doubleClick(ClickData clickData) {
        SystemCall.MINIMIZE_FOCUS_APP.call(os, clickData.isClient);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            click(clickData);
        } else if (id == 2) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            doubleClick(clickData);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (mouseClickTime > 5) { // click
            ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), false);
            writeClientAction(1, clickData::writeToBuf);
            playButtonClickSound();
            click(new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), true));
            mouseClickTime = -1;
        } else if (mouseClickTime > -1) {
            mouseClickTime++;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (mouseClickTime == -1) {
                mouseClickTime = 0;
            } else if (mouseClickTime <= 5) { // double click
                ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), false);
                writeClientAction(2, clickData::writeToBuf);
                playButtonClickSound();
                doubleClick(new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown(), true));
                mouseClickTime = -1;
            }
            return true;
        }
        return false;
    }

}
