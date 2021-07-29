package gregtech.api.terminal.gui.widgets.os;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import javafx.application.Application;
import javafx.geometry.Pos;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class TerminalOSWidget extends AbstractWidgetGroup {
    private IGuiTexture background;
    private final List<AbstractApplication> openedApps;
    private final TerminalMenuWidget menu;
    private final TerminalDesktopWidget desktop;

    public TerminalOSWidget(int xPosition, int yPosition, int width, int height) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.openedApps = new ArrayList<>();
        this.desktop = new TerminalDesktopWidget(Position.ORIGIN, new Size(333, 232), this);
        this.menu = new TerminalMenuWidget(Position.ORIGIN, new Size(35, 232), this).setBackground(GuiTextures.TERMINAL_MENU);
        this.addWidget(desktop);
        this.addWidget(menu);
    }

    public TerminalOSWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public void installApplication(AbstractApplication application){
        menu.addApp(application);
    }

    public void openApplication(AbstractApplication application, boolean isClient) {
        for (AbstractApplication app : openedApps) {
            if (app.getClass() == application.getClass()) {
                app.setVisible(true);
                return;
            }
        }
        AbstractApplication app = application.openApp(isClient, null);
        openedApps.add(app);
        desktop.addWidget(app);
        menu.hideMenu();
    }

    public void closeApplication(AbstractApplication application, boolean isClient) {
        desktop.removeWidget(application);
        application.closeApp(isClient, null);
    }

    public void backToHome() {
        List<AbstractApplication> close = new ArrayList<>();
        for (AbstractApplication app : openedApps) {
            if (app.isBackgroundApp()) {
                app.setVisible(false);
            } else {
                close.add(app);
                closeApplication(app, isClientSide());
            }
        }
        close.forEach(openedApps::remove);
        menu.showMenu();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position position = getPosition();
        Size size = getSize();
        if( background != null) {
            background.draw(position.x, position.y, size.width, size.height);
        } else {
            drawGradientRect(position.x, position.y, size.width, size.height, -1, -1);
        }
        RenderUtil.useScissor(position.x, position.y, size.width, size.height, ()->{
            super.drawInBackground(mouseX, mouseY, partialTicks, context);
        });
    }
}
