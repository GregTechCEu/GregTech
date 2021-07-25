package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import java.util.function.Supplier;

public class TerminalMenu extends AbstractWidgetGroup {
    private IGuiTexture background;
    private Widget activeContent;
    private CircleButton activeButton;
    public TerminalMenu(int xPosition, int yPosition, int width, int height) {
        super(new Position(xPosition, yPosition), new Size(width, height));
    }

    public TerminalMenu setBackGround(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public TerminalMenu addApp(IGuiTexture icon, String nameKey, Supplier<Widget> content){
        CircleButton button = new CircleButton(27,40,12).setIcon(icon).setHoverText(nameKey);
        button.setClickListener(clickData -> {
                    if (button != activeButton) {
                        removeWidget(activeContent);
                        if(content != null) {
                            activeContent = content.get();
                            addWidget(activeContent);
                        }
                        activeButton.setFillColors(0xffffffff);
                        activeButton = button;
                        activeButton.setFillColors(0xFF929292);
                    }
                });
        this.addWidget(button);
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        background.draw(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height);
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
