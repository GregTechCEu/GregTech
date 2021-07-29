package gregtech.api.terminal.gui.widgets.os;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import net.minecraft.client.renderer.GlStateManager;

public class TerminalMenuWidget extends WidgetGroup {
    private Interpolator interpolator;
    private int appCount;
    private IGuiTexture background;
    private CircleButtonWidget activeButton;
    private final TerminalOSWidget os;
    public boolean isHide;


    public TerminalMenuWidget(Position position, Size size, TerminalOSWidget os) {
        super(position, size);
        this.os = os;
    }

    public TerminalMenuWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public void addApp(AbstractApplication application){
        int x = this.getSize().width / 2;
        int r = 12;
        int y = appCount * (2 * r + 4) + r + 20;
        CircleButtonWidget button = new CircleButtonWidget(x,y,r).setIcon(application.getIcon()).setHoverText(application.getName());
        button.setClickListener(clickData -> {
            if (button != activeButton) {
                os.openApplication(application, clickData.isClient);
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

    public void hideMenu() {
        if (!isHide && interpolator == null) {
            int y = getSelfPosition().y;
            interpolator = new Interpolator(getSelfPosition().x, getSelfPosition().x - getSize().width, 10, Eases.EaseLinear,
                    value-> setSelfPosition(new Position(value.intValue(), y)),
                    value-> interpolator = null);
            interpolator.start();
            isHide = true;
        }
    }

    public void showMenu() {
        if (isHide && interpolator == null) {
            int y = getSelfPosition().y;
            interpolator = new Interpolator(getSelfPosition().x, getSelfPosition().x + getSize().width, 10, Eases.EaseLinear,
                    value-> setSelfPosition(new Position(value.intValue(), y)),
                    value-> interpolator = null);
            interpolator.start();
            isHide = false;
        }
    }

    @Override
    public void updateScreen() {
        if(interpolator != null) interpolator.update();
        super.updateScreen();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        GlStateManager.color(1,1,1,0.5f);
        if( background != null) {
            background.draw(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height);
        } else {
            drawGradientRect(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height, 0xff000000, 0xff000000);
        }
        GlStateManager.color(1,1,1,1);
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
