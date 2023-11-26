package gregtech.common.terminal.component;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.terminal.os.menu.IMenuComponent;

import java.util.function.Consumer;

public class ClickComponent implements IMenuComponent {

    private IGuiTexture icon;
    private String hoverText;
    private Consumer<Widget.ClickData> consumer;

    public ClickComponent setIcon(IGuiTexture icon) {
        this.icon = icon;
        return this;
    }

    public ClickComponent setHoverText(String hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    public ClickComponent setClickConsumer(Consumer<Widget.ClickData> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public IGuiTexture buttonIcon() {
        return icon;
    }

    @Override
    public String hoverText() {
        return hoverText;
    }

    @Override
    public void click(Widget.ClickData clickData) {
        if (consumer != null) {
            consumer.accept(clickData);
        }
    }
}
