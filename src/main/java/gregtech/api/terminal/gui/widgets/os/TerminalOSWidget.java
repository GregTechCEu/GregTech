package gregtech.api.terminal.gui.widgets.os;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import javafx.application.Application;
import javafx.geometry.Pos;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class TerminalOSWidget extends AbstractWidgetGroup {
    private IGuiTexture background;
    private final List<AbstractApplication> apps;
    private final TerminalMenuWidget menu;
    private final TerminalDesktopWidget desktop;

    public TerminalOSWidget(int xPosition, int yPosition, int width, int height) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.apps = new ArrayList<>();
        this.desktop = new TerminalDesktopWidget(new Position(0,0), new Size(333, 232), this);
        this.menu = new TerminalMenuWidget(new Position(0,0), new Size(35, 232), this).setBackground(GuiTextures.TERMINAL_MENU);
        this.addWidget(desktop);
        this.addWidget(menu);
    }

    public TerminalOSWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public void installApplication(AbstractApplication application){
        apps.add(application);
        menu.addApp(application);
    }

    public void openApplication(AbstractApplication application, boolean isClient) {
        desktop.clearAllWidgets();
        application.loadApp(desktop, isClient);
    }

    public void backToHome() {
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else {
            if (menu.isHide) {
                menu.showMenu();
            } else {
                menu.hideMenu();
            }
        }
        return true;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if( background != null) {
            background.draw(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height);
        } else {
            drawGradientRect(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height, -1, -1);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
