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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class TerminalOSWidget extends AbstractWidgetGroup {
    private IGuiTexture background;
    public final List<AbstractApplication> openedApps;
    public AbstractApplication focusApp;
    public final TerminalMenuWidget menu;
    public final TerminalDesktopWidget desktop;
    public List<AbstractApplication> waitToRemoved;
    private NBTTagCompound tabletNBT;

    public TerminalOSWidget(int xPosition, int yPosition, int width, int height, NBTTagCompound tabletNBT) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.openedApps = new ArrayList<>();
        this.desktop = new TerminalDesktopWidget(Position.ORIGIN, new Size(333, 232), this);
        this.menu = new TerminalMenuWidget(Position.ORIGIN, new Size(31, 232), this).setBackground(GuiTextures.TERMINAL_MENU);
        this.addWidget(desktop);
        this.addWidget(menu);
        this.waitToRemoved = new ArrayList<>();
        this.tabletNBT = tabletNBT;
    }

    public TerminalOSWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public void installApplication(AbstractApplication application){
        desktop.installApplication(application);
    }

    public void openApplication(AbstractApplication application, boolean isClient) {
        if (focusApp != null ) {
            closeApplication(focusApp, isClient);
        }
        for (AbstractApplication app : openedApps) {
            if (app.getClass() == application.getClass()) {
                focusApp = app;
                maximizeApplication(app, isClient);
                return;
            }
        }
        String name = application.getName();
        if (!tabletNBT.hasKey(name)) {
            tabletNBT.setTag(name, new NBTTagCompound());
        }
        AbstractApplication app = application.createApp(isClient, tabletNBT.getCompoundTag(application.getName()));
        if (app != null) {
            openedApps.add(app);
            desktop.addWidget(app);
            focusApp = app;
            maximizeApplication(app, isClient);
        }
    }

    public void maximizeApplication(AbstractApplication application, boolean isClient) {
        application.setActive(true);
        if (isClient) {
            application.maximizeApp(app->desktop.hideDesktop());
            if (!menu.isHide) {
                menu.hideMenu();
            }
        }
        desktop.hideDesktop();
    }

    public void minimizeApplication(AbstractApplication application, boolean isClient) {
        if (application != null) {
            if (application.isBackgroundApp()) {
                application.setActive(false);
            }
            if (isClient) {
                application.minimizeApp(null);
            }
            if(focusApp == application) {
                focusApp = null;
            }
            desktop.showDesktop();
        }
    }

    public void closeApplication(AbstractApplication application, boolean isClient) {
        if (application != null) {
            String name = application.getName();
            if (!tabletNBT.hasKey(name)) {
                tabletNBT.setTag(name, new NBTTagCompound());
            }
            application.closeApp(isClient, tabletNBT.getCompoundTag(name));
            if (isClient) {
                application.minimizeApp(waitToRemoved::add);
            } else {
                waitToRemoved.add(application);
            }
            openedApps.remove(application);
            if(focusApp == application) {
                focusApp = null;
            }
            desktop.showDesktop();
        }
    }

    public void homeTrigger(boolean isClient) {
        if(isClient) {
            if (menu.isHide) {
                menu.showMenu();
            } else {
                menu.hideMenu();
            }
        }
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

    @Override
    public void updateScreen() {
        if (waitToRemoved.size() > 0) {
            waitToRemoved.forEach(desktop::removeWidget);
        }
        waitToRemoved.clear();
        super.updateScreen();
    }

    @Override
    public void detectAndSendChanges() {
        if (waitToRemoved.size() > 0) {
            waitToRemoved.forEach(desktop::removeWidget);
        }
        waitToRemoved.clear();
        super.detectAndSendChanges();
    }
}
