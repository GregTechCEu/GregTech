package gregtech.api.terminal.app;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;

public abstract class AbstractApplication {
    protected final String name;
    protected final IGuiTexture icon;

    public AbstractApplication (String name, IGuiTexture icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public IGuiTexture getIcon() {
        return icon;
    }

    public void loadApp(WidgetGroup group, boolean isClient) {

    }

    public void unloadApp(boolean isClient) {

    }

}
