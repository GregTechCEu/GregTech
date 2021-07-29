package gregtech.api.terminal.app;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractApplication extends WidgetGroup {
    protected final String name;
    protected final IGuiTexture icon;

    public AbstractApplication (String name, IGuiTexture icon) {
        super(Position.ORIGIN, new Size(333, 232));
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public IGuiTexture getIcon() {
        return icon;
    }

    public abstract AbstractApplication openApp(boolean isClient, NBTTagCompound nbt);

    public void closeApp(boolean isClient, NBTTagCompound nbt) {

    }

    public boolean isBackgroundApp() {
        return false;
    }

}
