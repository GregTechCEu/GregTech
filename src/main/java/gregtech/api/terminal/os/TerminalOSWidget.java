package gregtech.api.terminal.os;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.terminal.hardware.HardwareProvider;
import gregtech.api.terminal.os.menu.TerminalMenuWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;
import gregtech.common.items.behaviors.TerminalBehaviour;
import gregtech.common.terminal.app.settings.widgets.OsSettings;
import gregtech.common.terminal.hardware.BatteryHardware;
import gregtech.common.terminal.hardware.DeviceHardware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TerminalOSWidget extends AbstractWidgetGroup {
    public static final TextureArea TERMINAL_FRAME = TextureArea.fullImage("textures/gui/terminal/terminal_frame.png");
    public static final TextureArea TERMINAL_HOME = TextureArea.fullImage("textures/gui/terminal/terminal_home.png");
    public static final int DEFAULT_WIDTH = 333;
    public static final int DEFAULT_HEIGHT = 232;

    private IGuiTexture background;
    private AbstractApplication focusApp;
    public final NBTTagCompound tabletNBT;
    public final List<AbstractApplication> openedApps;
    public final List<AbstractApplication> installedApps;
    public final TerminalMenuWidget menu;
    public final TerminalDesktopWidget desktop;
    public final TerminalHomeButtonWidget home;
    public final BlockPos clickPos;
    public final ItemStack itemStack;
    public final HardwareProvider hardwareProvider;
    private int tickCounter;
    private long lastCharge;
    private boolean maximize;
    private boolean showMenuHover = false;

    public TerminalOSWidget(int xPosition, int yPosition, ItemStack itemStack) {
        super(new Position(xPosition, yPosition), new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        this.background = TerminalTheme.WALL_PAPER;
        this.openedApps = new ArrayList<>();
        this.installedApps = new ArrayList<>();
        this.desktop = new TerminalDesktopWidget(Position.ORIGIN, new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT), this);
        this.menu = new TerminalMenuWidget(Position.ORIGIN, new Size(31, DEFAULT_HEIGHT), this).setBackground(TerminalTheme.COLOR_B_2);
        this.home = new TerminalHomeButtonWidget(this);
        this.addWidget(desktop);
        this.addWidget(menu);
        this.addWidget(home);
        this.itemStack = itemStack;
        this.tabletNBT = itemStack.getOrCreateSubCompound("terminal");
        this.tabletNBT.removeTag("_ar");
        this.hardwareProvider = itemStack.getCapability(GregtechCapabilities.CAPABILITY_HARDWARE_PROVIDER, null);
        if (TerminalBehaviour.isCreative(itemStack)) {
            TerminalRegistry.getAllApps().forEach(this::installApplication);
        } else {
            TerminalRegistry.getDefaultApps().forEach(this::installApplication);
            NBTTagList installed = tabletNBT.getTagList("_installed", Constants.NBT.TAG_STRING);
            for (NBTBase nbtBase : installed) {
                if (nbtBase instanceof NBTTagString) {
                    AbstractApplication app = TerminalRegistry.getApplication(((NBTTagString) nbtBase).getString());
                    if (app != null) {
                        installApplication(app);
                    }
                }
            }
        }
        if (tabletNBT.hasKey("_click")) {
            clickPos = NBTUtil.getPosFromTag(tabletNBT.getCompoundTag("_click"));
        } else {
            clickPos = null;
        }
    }

    public ModularUI getModularUI() {
        return this.gui;
    }

    public TerminalOSWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public AbstractApplication getFocusApp() {
        return focusApp;
    }

    public List<Hardware> getHardware() {
        if (hardwareProvider == null) {
            return Collections.emptyList();
        }
        return hardwareProvider.getHardware();
    }

    public <T extends Hardware> List<T> getHardware(Class<T> clazz) {
        return getHardware().stream().filter(hw -> hw.getClass() == clazz).map(hw -> (T) hw).collect(Collectors.toList());
    }

    public void installApplication(AbstractApplication application) {
        desktop.installApplication(application);
        installedApps.add(application);
    }

    public void openApplication(AbstractApplication application, boolean isClient) {
        desktop.removeAllDialogs();
        NBTTagCompound nbt = tabletNBT.getCompoundTag(application.getRegistryName());
        if (!TerminalBehaviour.isCreative(itemStack)) {
            List<Hardware> hwDemand = TerminalRegistry.getAppHardwareDemand(application.getRegistryName(), Math.min(nbt.getInteger("_tier"), application.getMaxTier()));
            List<Hardware> unMatch = hwDemand.stream().filter(demand -> getHardware().stream().noneMatch(hw -> hw.isHardwareAdequate(demand))).collect(Collectors.toList());
            if (unMatch.size() > 0) {
                if (isClient) {
                    StringBuilder tooltips = new StringBuilder("\n");
                    for (Hardware match : unMatch) {
                        String info = match.addInformation();
                        String name = match.getLocalizedName();
                        if (info == null) {
                            tooltips.append(name);
                        } else if (match instanceof BatteryHardware) {
                            IElectricItem energyItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                            if (energyItem != null && energyItem.getCharge() <= 0) {
                                tooltips.append(I18n.format("terminal.battery.low_energy"));
                            } else {
                                tooltips.append(String.format("%s (%s+)", name, info));
                            }
                        } else {
                            tooltips.append(String.format("%s (%s)", name, info));
                        }
                        tooltips.append(" ");
                    }
                    TerminalDialogWidget.showInfoDialog(this,
                            "terminal.component.error",
                            I18n.format("terminal.os.hw_demand") + tooltips).setClientSide().open();
                }
                return;
            }
        }
        if (!application.canPlayerUse(gui.entityPlayer)) {
            return;
        }
        if (focusApp != null) {
            closeApplication(focusApp, isClient);
        }
        for (AbstractApplication app : openedApps) {
            if (app.getRegistryName().equals(application.getRegistryName()) && application.canLaunchConcurrently(app)) {
                app.onOSSizeUpdate(this.getSize().width, this.getSize().height);
                maximizeApplication(app, isClient);
                return;
            }
        }
        AbstractApplication app = application.createAppInstance(this, isClient, nbt);
        if (app != null) {
            desktop.addWidget(app);
            app.setOs(this).initApp();
            app.onOSSizeUpdate(this.getSize().width, this.getSize().height);
            openedApps.add(app);
            maximizeApplication(app, isClient);
        }
    }

    public void maximizeApplication(AbstractApplication application, boolean isClient) {
        application.setActive(true);
        if (isClient) {
            application.maximizeWidget(app -> desktop.hideDesktop());
            if (!menu.isHide) {
                menu.hideMenu();
            }
        }
        focusApp = application;
        menu.loadComponents(focusApp);
        desktop.hideDesktop();
    }

    public void minimizeApplication(AbstractApplication application, boolean isClient) {
        desktop.removeAllDialogs();
        if (application != null) {
            if (focusApp == application) {
                if (isClient) {
                    application.minimizeWidget(app -> {
                        if (!application.isBackgroundApp()) {
                            application.setActive(false);
                        }
                    });
                } else if (!application.isBackgroundApp()) {
                    application.setActive(false);
                }
                focusApp = null;
            }
            menu.removeComponents();
            desktop.showDesktop();
        }
    }

    public void closeApplication(AbstractApplication application, boolean isClient) {
        desktop.removeAllDialogs();
        if (application != null) {
            String appName = application.getRegistryName();
            NBTTagCompound synced = application.closeApp();

            if (synced != null && !synced.isEmpty()) {
                tabletNBT.setTag(appName, synced);
                if (application.isClientSideApp() && isClient) {
                    writeClientAction(-2, buffer -> {
                        buffer.writeString(appName);
                        buffer.writeCompoundTag(synced);
                    });
                }
            }

            if (isClient && focusApp == application) {
                application.minimizeWidget(desktop::waitToRemoved);
            } else {
                desktop.waitToRemoved(application);
            }
            openedApps.remove(application);
            if (focusApp == application) {
                focusApp = null;
            }
            menu.removeComponents();
            desktop.showDesktop();
        }
    }

    public void callMenu(boolean isClient) {
        if (isClient) {
            if (menu.isHide) {
                menu.showMenu();
            } else {
                menu.hideMenu();
            }
        }
    }

    public void shutdown(boolean isClient) {
        if (isClient) {
            NBTTagCompound nbt = new NBTTagCompound();
            for (AbstractApplication openedApp : openedApps) {
                String appName = openedApp.getRegistryName();
                NBTTagCompound synced = openedApp.closeApp();
                if (synced != null && !synced.isEmpty()) {
                    tabletNBT.setTag(appName, synced);
                    if (openedApp.isClientSideApp()) {//if its a clientSideApp and the nbt not null, meaning this nbt should be synced to the server side.
                        nbt.setTag(appName, synced);
                    }
                }
            }
            writeClientAction(-1, buffer -> buffer.writeCompoundTag(nbt));
        } else { //request shutdown from the server side
            writeUpdateInfo(-2, packetBuffer -> {
            });
        }
    }

    protected void openDialog(TerminalDialogWidget widget) {
        if (isRemote()) {
            widget.onOSSizeUpdate(getSize().width, getSize().height);
            widget.maximizeWidget(null);
        } else if (widget.isClient()) {
            return;
        }
        desktop.addWidget(widget);
    }

    protected void closeDialog(TerminalDialogWidget widget) {
        if (isRemote()) {
            widget.minimizeWidget(desktop::waitToRemoved);
        } else if (!widget.isClient()) {
            desktop.waitToRemoved(widget);
        }
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
                NBTTagCompound data = openedApp.closeApp();
                if (data != null && !data.isEmpty()) {
                    tabletNBT.setTag(appName, data);
                } else if (nbt != null && openedApp.isClientSideApp() && nbt.hasKey(appName)) {
                    tabletNBT.setTag(appName, nbt.getCompoundTag(appName));
                }
            }
            this.getModularUI().entityPlayer.closeScreen(); // must close tablet from server side.
        } else if (id == -2) { // closeApp sync
            String appName = buffer.readString(32767);
            NBTTagCompound nbt = null;
            try {
                nbt = buffer.readCompoundTag();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nbt != null) {
                tabletNBT.setTag(appName, nbt);
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -1) { // disCharge
            long charge = buffer.readLong();
            IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electricItem instanceof BatteryHardware) {
                ((BatteryHardware) electricItem).setCharge(charge);
            }
            if (charge <= 0) {
                List<AbstractApplication> toClosed = new LinkedList<>();
                for (AbstractApplication openedApp : openedApps) {
                    TerminalRegistry.getAppHardwareDemand(openedApp.getRegistryName(), openedApp.getAppTier()).stream()
                            .filter(i -> i instanceof BatteryHardware).findFirst()
                            .ifPresent(x -> toClosed.add(openedApp));
                }
                for (AbstractApplication close : toClosed) {
                    this.closeApplication(close, true);
                }
                TerminalDialogWidget.showInfoDialog(this, "terminal.component.warning", "terminal.battery.low_energy").setClientSide().open();
            }
        } else if (id == -2) { // shutdown
            shutdown(true);
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        tickCounter++;
        if (background != null) {
            background.updateTick();
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        tickCounter++;
        if (tickCounter % 20 == 0) {
            long energyStore = disCharge();
            if (lastCharge != energyStore) {
                lastCharge = energyStore;
                writeUpdateInfo(-1, packetBuffer -> packetBuffer.writeLong(lastCharge));
            }
        }
    }

    private long disCharge() {
        IElectricItem electricItem = hardwareProvider.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null && !TerminalBehaviour.isCreative(itemStack)) {
            AtomicLong costs = new AtomicLong(0);
            List<AbstractApplication> charged = new ArrayList<>();
            for (AbstractApplication openedApp : openedApps) {
                TerminalRegistry.getAppHardwareDemand(openedApp.getRegistryName(), openedApp.getAppTier()).stream()
                        .filter(i -> i instanceof BatteryHardware).findFirst()
                        .ifPresent(battery -> {
                            costs.addAndGet(((BatteryHardware) battery).getCharge());
                            charged.add(openedApp);
                        });
            }
            for (DeviceHardware hardware : getHardware(DeviceHardware.class)) {
                if (hardware.getDevice() == DeviceHardware.DEVICE.SOLAR_LV) {
                    costs.addAndGet(-200);
                }
            }
            if (costs.get() > 0 && electricItem.discharge(costs.get(), 999, true, false, false) != costs.get()) {
                charged.forEach(app -> closeApplication(app, false));
            } else if (costs.get() < 0) {
                electricItem.charge(-costs.get(), 999, true, false);
            }
            return electricItem.getCharge();
        }
        return lastCharge;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position position = getPosition();
        Size size = getSize();

        // show menu when mouse near the left edge
        if ((focusApp == null || focusApp.canOpenMenuOnEdge()) && isMouseOver(position.x, position.y, 7, size.height, mouseX, mouseY)) {
            if (menu.isHide && !showMenuHover) {
                menu.showMenu();
                showMenuHover = true;
            }
        } else if (!menu.isHide && showMenuHover && !isMouseOver(position.x - 10, position.y, 41, size.height, mouseX, mouseY)) {
            menu.hideMenu();
            showMenuHover = false;
        }

        if (background != null) {
            background.draw(position.x, position.y, size.width, size.height);
        } else {
            drawGradientRect(position.x, position.y, size.width, size.height, -1, -1);
        }
        if (maximize) {
            desktop.drawInBackground(mouseX, mouseY, partialTicks, context);
            if (menu.isVisible()) {
                menu.drawInBackground(mouseX, mouseY, partialTicks, context);
            }
        } else {
            RenderUtil.useScissor(position.x, position.y, size.width, size.height, () -> {
                desktop.drawInBackground(mouseX, mouseY, partialTicks, context);
                if (menu.isVisible()) {
                    menu.drawInBackground(mouseX, mouseY, partialTicks, context);
                }
            });
            TERMINAL_FRAME.draw(position.x - 12, position.y - 11, 380, 256);
        }
        home.drawInBackground(mouseX, mouseY, partialTicks, context);
    }

    boolean waitShutdown;

    @Override
    public boolean keyTyped(char charTyped, int keyCode) {
        if (waitShutdown && (keyCode == 1 || Minecraft.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(keyCode))) {
            shutdown(true);
            return true;
        }
        if (super.keyTyped(charTyped, keyCode)) {
            return true;
        }
        if (keyCode == 1 || Minecraft.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) { // hook esc and e
            waitShutdown = true;
            if (!OsSettings.DOUBLE_CHECK) {
                shutdown(true);
                return true;
            }
            TerminalDialogWidget.showConfirmDialog(this, "terminal.component.warning", "terminal.os.shutdown_confirm", result -> {
                if (result) {
                    shutdown(true);
                } else {
                    waitShutdown = false;
                }
            }).setClientSide().open();
            return true;
        }
        waitShutdown = false;
        return false;
    }

    public boolean isMaximize() {
        return maximize;
    }

    private void updateOSSize() {
        int osWidth = getSize().width;
        int osHeight = getSize().height;
        if (this.maximize && (osWidth != gui.getScreenWidth() || osHeight != gui.getScreenHeight())) {
            osWidth = gui.getScreenWidth();
            osHeight = gui.getScreenHeight();
        } else if (!this.maximize && (osWidth != DEFAULT_WIDTH || osHeight != DEFAULT_HEIGHT)) {
            osWidth = DEFAULT_WIDTH;
            osHeight = DEFAULT_HEIGHT;
        } else {
            return;
        }
        this.setSize(new Size(osWidth, osHeight));
        this.desktop.setSize(new Size(osWidth, osHeight));
        this.menu.setSize(new Size(31, osHeight));
        this.home.setSelfPosition(this.maximize ? new Position((osWidth - this.home.getSize().width) / 2, osHeight - this.home.getSize().height - 10) : new Position(340, 104));
        this.home.setIcon(this.maximize ? TERMINAL_HOME : null);
        gui.setSize(this.maximize ? osWidth : 380, this.maximize ? osHeight : 256);
        if (this.focusApp != null) {
            this.focusApp.onOSSizeUpdate(osWidth, osHeight);
        }
        for (Widget widget : desktop.widgets) {
            if (widget instanceof TerminalDialogWidget) {
                ((TerminalDialogWidget) widget).onOSSizeUpdate(osWidth, osHeight);
            }
        }
    }

    public void maximize(boolean isClient) {
        if (isClient) {
            this.maximize = !this.maximize;
            updateOSSize();
        }
    }

    @Override
    public void setParentPosition(Position parentPosition) {
        if (this.maximize) {
            super.setParentPosition(parentPosition.subtract(this.getSelfPosition()));
            if (isRemote()) {
                updateOSSize();
            }
        } else {
            super.setParentPosition(parentPosition);
        }
    }
}
