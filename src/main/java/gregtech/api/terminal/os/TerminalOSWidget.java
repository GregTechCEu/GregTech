package gregtech.api.terminal.os;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.menu.TerminalMenuWidget;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TerminalOSWidget extends AbstractWidgetGroup {
    private IGuiTexture background;
    public final List<AbstractApplication> openedApps;
    public AbstractApplication focusApp;
    public final TerminalMenuWidget menu;
    public final TerminalDesktopWidget desktop;
    private final NBTTagCompound tabletNBT;

    public TerminalOSWidget(int xPosition, int yPosition, int width, int height, NBTTagCompound tabletNBT) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.openedApps = new ArrayList<>();
        this.desktop = new TerminalDesktopWidget(Position.ORIGIN, new Size(333, 232), this);
        this.menu = new TerminalMenuWidget(Position.ORIGIN, new Size(31, 232), this).setBackground(TerminalTheme.COLOR_B_2);
        this.addWidget(desktop);
        this.addWidget(menu);
        this.tabletNBT = tabletNBT;
        TerminalRegistry.getDefaultApps().forEach(name-> installApplication(TerminalRegistry.getApplication(name)));
        NBTTagList installed = tabletNBT.getTagList("installed", 8);
        for (NBTBase nbtBase : installed) {
            if (nbtBase instanceof NBTTagString) {
                AbstractApplication app = TerminalRegistry.getApplication(((NBTTagString) nbtBase).getString());
                if (app != null) {
                    installApplication(app);
                }
            }
        }
    }

    public ModularUI getModularUI(){
        return this.gui;
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
                maximizeApplication(app, isClient);
                return;
            }
        }
        AbstractApplication app = application.createApp(this, isClient, tabletNBT.getCompoundTag(application.getRegistryName())).setOs(this);
        if (app != null) {
            openedApps.add(app);
            desktop.addWidget(app);
            maximizeApplication(app, isClient);
        }
    }

    public void maximizeApplication(AbstractApplication application, boolean isClient) {
        application.setActive(true);
        if (isClient) {
            application.maximizeWidget(app->desktop.hideDesktop());
            if (!menu.isHide) {
                menu.hideMenu();
            }
        }
        focusApp = application;
        menu.loadComponents(focusApp);
        desktop.hideDesktop();
    }

    public void minimizeApplication(AbstractApplication application, boolean isClient) {
        if (application != null) {
            if (application.isBackgroundApp()) {
                application.setActive(false);
            }
            if (isClient) {
                application.minimizeWidget(null);
            }
            if(focusApp == application) {
                focusApp = null;
            }
            menu.removeComponents();
            desktop.showDesktop();
        }
    }

    public NBTTagCompound closeApplication(AbstractApplication application, boolean isClient) {
        if (application != null) {
            NBTTagCompound nbt = application.closeApp(isClient, tabletNBT.getCompoundTag(application.getRegistryName()));
            if (nbt != null) {
                tabletNBT.setTag(application.getRegistryName(), nbt);
            }
            if (isClient) {
                application.minimizeWidget(desktop::waitToRemoved);
            } else {
                desktop.waitToRemoved(application);
            }
            openedApps.remove(application);
            if(focusApp == application) {
                focusApp = null;
            }
            menu.removeComponents();
            desktop.showDesktop();
            return nbt;
        }
        return null;
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

    @SideOnly(Side.CLIENT)
    private void shutdown() {
        NBTTagCompound nbt = new NBTTagCompound();
        for (AbstractApplication openedApp : openedApps) {
            String appName = openedApp.getRegistryName();
            NBTTagCompound synced = openedApp.closeApp(true, tabletNBT.getCompoundTag(appName));
            if (synced != null && !synced.isEmpty()) {
                tabletNBT.setTag(appName, synced);
                if (openedApp.isClientSideApp()) {//if its a clientSideApp and the nbt not null, meaning this nbt should be synced to the server side.
                    nbt.setTag(appName, synced);
                }
            }
        }
        writeClientAction(-1, buffer -> buffer.writeCompoundTag(nbt));
    }

    protected TerminalDialogWidget openDialog(TerminalDialogWidget widget) {
        if (isRemote()) {
            widget.maximizeWidget(null);
        } else if(widget.isClient()) {
            return widget;
        }
        desktop.addWidget(widget);
        return widget;
    }

    protected TerminalDialogWidget closeDialog(TerminalDialogWidget widget) {
        if (isRemote()) {
            widget.minimizeWidget(desktop::waitToRemoved);
        } else if(widget.isClient()) {
            desktop.waitToRemoved(widget);
        }
        return widget;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) { //shutdown
            NBTTagCompound nbt = null;
            try {
                nbt = buffer.readCompoundTag();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (AbstractApplication openedApp : openedApps) {
                String appName = openedApp.getRegistryName();
                NBTTagCompound data = openedApp.closeApp(false, tabletNBT.getCompoundTag(appName));
                if (data != null && !data.isEmpty()) {
                    tabletNBT.setTag(appName, data);
                } else if (nbt != null && openedApp.isClientSideApp() && nbt.hasKey(appName)) {
                    tabletNBT.setTag(appName, nbt.getCompoundTag(appName));
                }
            }
            this.getModularUI().entityPlayer.closeScreen(); // must close tablet from server side.
        }
        super.handleClientAction(id, buffer);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if( background != null) {
            background.updateTick();
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

    boolean waitShutdown;
    @Override
    public boolean keyTyped(char charTyped, int keyCode) {
        if (keyCode == 1) { // hook esc
            if (waitShutdown) {
                shutdown();
            } else {
                waitShutdown = true;
                TerminalDialogWidget.showConfirmDialog(this, "terminal.component.warning", "terminal.os.shutdown_confirm", result->{
                    if (result) {
                        shutdown();
                    } else {
                        waitShutdown = false;
                    }
                }).setClientSide().open();
            }
            return true;
        }
        waitShutdown = false;
        return super.keyTyped(charTyped, keyCode);
    }
}
