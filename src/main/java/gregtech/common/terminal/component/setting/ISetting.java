package gregtech.common.terminal.component.setting;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;

public interface ISetting {
    String getName();
    IGuiTexture getIcon();
    Widget getWidget();
}
