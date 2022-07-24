package gregtech.common.terminal.component.setting;

import gregtech.api.guiOld.Widget;
import gregtech.api.guiOld.resources.IGuiTexture;
import gregtech.api.guiOld.widgets.WidgetGroup;

import java.util.function.Consumer;

public class GuiTextureSetter extends WidgetGroup implements ISetting{
    private final String name;
    private Consumer<IGuiTexture> updated;

    public GuiTextureSetter(String name, Consumer<IGuiTexture> updated) {
        this.name = name;
        this.updated = updated;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IGuiTexture getIcon() {
        return null;
    }

    @Override
    public Widget getWidget() {
        return null;
    }
}
