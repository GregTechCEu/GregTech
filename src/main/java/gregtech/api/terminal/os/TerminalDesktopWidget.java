package gregtech.api.terminal.os;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedList;
import java.util.List;

public class TerminalDesktopWidget extends WidgetGroup {

    private final TerminalOSWidget os;
    private final WidgetGroup appDiv;
    private final List<Widget> topWidgets;
    private int rowCount = 7;

    public TerminalDesktopWidget(Position position, Size size, TerminalOSWidget os) {
        super(position, size);
        this.os = os;
        this.appDiv = new WidgetGroup();
        this.addWidget(appDiv);
        this.topWidgets = new LinkedList<>();
    }

    public void installApplication(AbstractApplication application) {
        int r = 12;
        int index = appDiv.widgets.size();
        int x = this.getSize().width / 2 + (3 * r) * (index % rowCount - rowCount / 2);
        int y = (index / rowCount) * (3 * r) + 40;
        CircleButtonWidget button = new CircleButtonWidget(x, y)
                .setColors(TerminalTheme.COLOR_B_2.getColor(),
                        application.getThemeColor(),
                        TerminalTheme.COLOR_B_2.getColor())
                .setIcon(application.getIcon())
                .setHoverText(application.getUnlocalizedName());
        button.setClickListener(clickData -> os.openApplication(application, clickData.isClient));
        appDiv.addWidget(button);
    }

    @SideOnly(Side.CLIENT)
    public void addTopWidget(Widget widget) {
        topWidgets.add(widget);
    }

    @SideOnly(Side.CLIENT)
    public void removeTopWidget(Widget widget) {
        topWidgets.remove(widget);
    }

    @SideOnly(Side.CLIENT)
    private static boolean topWidgetsMouseOver(Widget widget, int mouseX, int mouseY) {
        if (widget.isMouseOverElement(mouseX, mouseY)) {
            return true;
        }
        if (widget instanceof WidgetGroup) {
            for (Widget child : ((WidgetGroup) widget).widgets) {
                if (child.isVisible() && topWidgetsMouseOver(child, mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        boolean isBlocked = false;
        for (Widget topWidget : topWidgets) {
            if (topWidgetsMouseOver(topWidget, mouseX, mouseY)) {
                isBlocked = true;
                break;
            }
        }
        for (Widget widget : widgets) {
            if (widget.isVisible() && !(isBlocked && widget instanceof AbstractApplication)) {
                widget.drawInForeground(mouseX, mouseY);
            }
        }
    }

    public void showDesktop() {
        appDiv.setActive(true);
        appDiv.setVisible(true);
    }

    public void hideDesktop() {
        appDiv.setActive(false);
        appDiv.setVisible(false);
    }

    public void removeAllDialogs() {
        for (Widget widget : widgets) {
            if (widget instanceof TerminalDialogWidget) {
                ((TerminalDialogWidget) widget).close();
            }
        }
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
        this.rowCount = (size.width - 81) / 36;
        int r = 12;
        for (int i = appDiv.widgets.size() - 1; i >= 0; i--) {
            Widget widget = appDiv.widgets.get(i);
            int x = this.getSize().width / 2 + (3 * r) * (i % rowCount - rowCount / 2);
            int y = (i / rowCount) * (3 * r) + 40;
            widget.setSelfPosition(new Position(x - r, y - r));
        }
    }
}
