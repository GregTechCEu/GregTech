package gregtech.api.terminal.app;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.terminal.gui.widgets.AnimaWidgetGroup;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.items.behaviors.TerminalBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractApplication extends AnimaWidgetGroup {
    protected final String name;
    protected final IGuiTexture icon;
    protected TerminalOSWidget os;
    protected boolean isClient;
    protected NBTTagCompound nbt;

    public AbstractApplication(String name, IGuiTexture icon) {
        super(Position.ORIGIN, new Size(333, 232));
        this.name = name;
        this.icon = icon;
    }

    public AbstractApplication setOs(TerminalOSWidget os) {
        this.os = os;
        return this;
    }

    /**
     * App Name
     */
    public String getRegistryName() {
        return name;
    }

    public String getUnlocalizedName() {
        return "gregtech.terminal.app_name." + name;
    }

    /**
     * App Icon
     */
    public IGuiTexture getIcon() {
        return icon;
    }

    /**
     * Will be called when try to open this app. you should return an instance here.
     * It's probably best not to initialize your app here.
     */
    public AbstractApplication createAppInstance(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        try {
            AbstractApplication app = this.getClass().newInstance();
            app.isClient = isClient;
            app.nbt = nbt;
            return app;
        } catch (InstantiationException | IllegalAccessException e) {
            GTLog.logger.error("Error while create default app. {}", this.getClass(), e);
        }
        return null;
    }

    /**
     * init app here. you have access to os, isClient, nbt.
     */
    public AbstractApplication initApp() {
        return this;
    }

    /**
     * you should store the persistent data for both side here.
     * @return nbt data. if its a clientSideApp and the nbt not null, this nbt should be synced to the server side.
     */
    public NBTTagCompound closeApp() {
        return null;
    }

    /**
     * Whether the app can run in the background when minimized.
     */
    public boolean isBackgroundApp() {
        return false;
    }

    /**
     * If it is a client side app, will block all action packets sent from client.
     * If the app doesn't require server execution, it better be a client side app.
     * For details about data synchronization, see {@link #closeApp()}
     */
    public boolean isClientSideApp() {return false;}

    public TerminalOSWidget getOs() {
        return os;
    }

    /**
     * Add components to menu bar.
     * @see IMenuComponent
     */
    public List<IMenuComponent> getMenuComponents() {
        return Collections.emptyList();
    }

    /**
     * Whether the player can open this app.
     */
    public boolean canPlayerUse(EntityPlayer player) {
        return true;
    }

    /**
     * App Current Tier. Creative Terminal(return max tier)
     */
    public final int getAppTier() {
        if (nbt != null) {
            if (TerminalBehaviour.isCreative(getOs().itemStack)) {
                return getMaxTier();
            }
            return Math.min(nbt.getInteger("_tier"), getMaxTier());
        }
        return 0;
    }

    /**
     * App Max Tier
     */
    public int getMaxTier() {
        return 0;
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (!isClientSideApp()) {
            super.writeClientAction(id, packetBufferWriter);
        }
    }
}
