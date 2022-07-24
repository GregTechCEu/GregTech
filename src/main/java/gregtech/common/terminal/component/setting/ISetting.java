package gregtech.common.terminal.component.setting;

import gregtech.api.guiOld.Widget;
import gregtech.api.guiOld.resources.IGuiTexture;

public interface ISetting {
    String getName();
    IGuiTexture getIcon();
    Widget getWidget();
}
