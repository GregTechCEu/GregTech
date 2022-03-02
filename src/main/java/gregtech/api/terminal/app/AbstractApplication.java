package gregtech.api.terminal.app;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.gui.widgets.AnimaWidgetGroup;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.common.items.behaviors.TerminalBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractApplication extends AnimaWidgetGroup {
    protected final String name;
    protected TerminalOSWidget os;
    protected boolean isClient;
    protected NBTTagCompound nbt;

    public AbstractApplication(String name) {
        super(Position.ORIGIN, new Size(TerminalOSWidget.DEFAULT_WIDTH, TerminalOSWidget.DEFAULT_HEIGHT));
        this.name = name;
    }

    public AbstractApplication setOs(TerminalOSWidget os) {
        this.os = os;
        return this;
    }

    public boolean canOpenMenuOnEdge() {
        return true;
    }

    /**
     * App theme color
     */
    public int getThemeColor() {
        return name.hashCode() | 0xff000000;
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
        return TextureArea.fullImage("textures/gui/terminal/" + name + "/icon.png");
    }

    /**
     * App Description
     */
    @SideOnly(Side.CLIENT)
    public String getDescription() {
        if (I18n.hasKey("terminal." + getRegistryName() + ".description")) {
            return I18n.format("terminal." + getRegistryName() + ".description").replaceAll("\\\\n", "\n");
        }
        return I18n.format("terminal.app_name.description");
    }

    /**
     * App Profile
     */
    @SideOnly(Side.CLIENT)
    public IGuiTexture getProfile() {
        if (ResourceHelper.isResourceExist("textures/gui/terminal/" + name + "/profile.png")) {
            return TextureArea.fullImage("textures/gui/terminal/" + name + "/profile.png");
        }
        return getIcon();
    }

    /**
     * App Banner
     */
    @SideOnly(Side.CLIENT)
    public IGuiTexture getBanner() {
        if (ResourceHelper.isResourceExist("textures/gui/terminal/" + name + "/banner.png")) {
            return TextureArea.fullImage("textures/gui/terminal/" + name + "/banner.png");
        }
        return null;
    }

    /**
     * App Information for each tier
     */
    @SideOnly(Side.CLIENT)
    public String getTierInformation(int tier) {
        if (I18n.hasKey("terminal." + name + ".tier." + tier)) {
            return I18n.format("terminal." + name + ".tier." + tier);
        }
        return I18n.format("terminal.app_name.tier", tier);
    }

    /**
     * Will be called when try to open this app. you should return an instance here.
     * Due to INative's poor synchronization, do not add the INativeWidget {@link gregtech.api.gui.INativeWidget} here.
     * Instead, It's probably best not to initialize your app here. initialize should in initApp {@link #initApp()}
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
    public int getAppTier() {
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

    /**
     * read NBT from the local config folder.
     */
    protected void loadLocalConfig(Consumer<NBTTagCompound> reader) {
        if (isClient && reader != null) {
            NBTTagCompound nbt = null;
            try {
                nbt = CompressedStreamTools.read(new File(TerminalRegistry.TERMINAL_PATH, String.format("config/%S.nbt", getRegistryName())));
            } catch (IOException e) {
                GTLog.logger.error("error while loading local nbt for {}", getRegistryName(), e);
            }
            if (nbt == null) {
                nbt = new NBTTagCompound();
            }
            reader.accept(nbt);
        }
    }

    /**
     * Write NBT to the local config folder.
     */
    protected void saveLocalConfig(Consumer<NBTTagCompound> writer) {
        if (isClient && writer != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            try {
                writer.accept(nbt);
                if (!nbt.isEmpty()) {
                    CompressedStreamTools.safeWrite(nbt, new File(TerminalRegistry.TERMINAL_PATH, String.format("config/%S.nbt", getRegistryName())));
                }
            } catch (IOException e) {
                GTLog.logger.error("error while saving local nbt for {}", getRegistryName(), e);
            }
        }
    }

    /**
     * Fired when you open this app or terminal's size updated. (maximize)
     */
    public void onOSSizeUpdate(int width, int height) {
        setSelfPosition(Position.ORIGIN.add(new Position((width - getSize().width) / 2, (height - getSize().height) / 2)));
    }

    public boolean canLaunchConcurrently(AbstractApplication application) {
        return true;
    }
}
