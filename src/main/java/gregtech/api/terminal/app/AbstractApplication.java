package gregtech.api.terminal.app;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.terminal.gui.widgets.AnimaWidgetGroup;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
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

    public AbstractApplication(String name, IGuiTexture icon) {
        super(Position.ORIGIN, new Size(333, 232));
        this.name = name;
        this.icon = icon;
    }

    public AbstractApplication setOs(TerminalOSWidget os) {
        this.os = os;
        return this;
    }

    public String getRegistryName() {
        return name;
    }

    public String getUnlocalizedName() {
        return "gregtech.terminal.app_name." + name;
    }

    public IGuiTexture getIcon() {
        return icon;
    }

    public abstract AbstractApplication createApp(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt);

    public NBTTagCompound closeApp(boolean isClient, NBTTagCompound nbt) {
        return null;
    }

    public boolean isBackgroundApp() {
        return false;
    }

    public boolean isClientSideApp() {return false;}

    public TerminalOSWidget getOs() {
        return os;
    }

    public List<IMenuComponent> getMenuComponents() {
        return Collections.emptyList();
    }

    public boolean canPlayerUse(EntityPlayer player) {
        return true;
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (!isClientSideApp()) {
            super.writeClientAction(id, packetBufferWriter);
        }
    }
}
