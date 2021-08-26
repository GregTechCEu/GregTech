package gregtech.api.terminal.os;

import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class TerminalDesktopWidget extends WidgetGroup {
    private final TerminalOSWidget os;
    private final WidgetGroup appDiv;

    public TerminalDesktopWidget(Position position, Size size, TerminalOSWidget os) {
        super(position, size);
        this.os = os;
        this.appDiv = new WidgetGroup();
        this.addWidget(appDiv);
    }

    public void installApplication(AbstractApplication application){
        int r = 12;
        int index = appDiv.widgets.size();
        int x = this.getSize().width / 2 + (3 * r) * (index % 7 - 3);
        int y = (index / 7) * (3 * r) + 40;
        CircleButtonWidget button = new CircleButtonWidget(x,y)
                .setColors(TerminalTheme.COLOR_B_2.getColor(),
                        TerminalTheme.COLOR_F_1.getColor(),
                        TerminalTheme.COLOR_B_2.getColor())
                .setIcon(application.getIcon())
                .setHoverText(application.getUnlocalizedName());
        button.setClickListener(clickData -> os.openApplication(application, clickData.isClient));
        appDiv.addWidget(button);
    }

    public void showDesktop() {
        appDiv.setActive(true);
        appDiv.setVisible(true);
    }

    public void hideDesktop() {
        appDiv.setActive(false);
        appDiv.setVisible(false);
    }
}
