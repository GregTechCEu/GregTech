package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class TerminalMenuWidget extends AbstractWidgetGroup {
    private int appCount;
    private IGuiTexture background;
    private final WidgetGroup activeContent;
    private CircleButtonWidget activeButton;
    public TerminalMenuWidget(int xPosition, int yPosition, int width, int height) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.activeContent = new WidgetGroup(new Position(27,-19), new Size(300, 232));
        this.addWidget(activeContent);
    }

    public TerminalMenuWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public void addApp(AbstractApplication application){
        int x = this.getSize().width / 2;
        int r = 12;
        int y = appCount * (2 * r + 4) + r;
        CircleButtonWidget button = new CircleButtonWidget(x,y,r).setIcon(application.getIcon()).setHoverText(application.getName());
        button.setClickListener(clickData -> {
                    if (button != activeButton) {
                        activeContent.clearAllWidgets();
                        application.loadApp(activeContent, clickData.isClient);
                        if (activeButton != null) {
                            activeButton.setFillColors(0xffffffff);
                        }
                        activeButton = button;
                        activeButton.setFillColors(0xFFAAF1DB);
                    }
                });
        this.addWidget(button);
        appCount++;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if( background != null) {
            background.draw(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
