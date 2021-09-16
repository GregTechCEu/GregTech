package gregtech.api.terminal.app;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/13
 * @Description: Application for AP.
 * When AR is running, {@link #tickAR(EntityPlayer)} and {@link #drawARScreen(RenderWorldLastEvent)} will be called when you hold the terminal in one of your hands.
 * Therefore, at most one AR app is active on the terminal at any one time. And when you open the terminal GUI it automatically closes the currently running AR.
 * You have access to the app's NBT of the handheld terminal when the AR is active, to load configs, init and so on.
 * Don't try to write NBT, you should always be aware that the AR is running on the client side.
 * if you really want to do something on the server side when AR is running, plz send packets. Because it's always running on the client side!!!!!!!!!!
 * (If you need data from NBT, dont forget to write nbt when closeApp {@link #closeApp()})
 */
public abstract class ARApplication extends AbstractApplication{
    protected NBTTagCompound nbtTag;

    public ARApplication(String name) {
        super(name);
    }

    public final void setNBTDuringAROpened(NBTTagCompound nbtTag) {
        this.nbtTag = nbtTag;
    }

    @Override
    public AbstractApplication initApp() {
        openAR();
        return this;
    }

    /**
     * open Camera for this AR and shutdown.
     * then, this AR will be in active and running on the client side.
     * It is best to call it on both sides.
     */
    protected final void openAR() {
        os.tabletNBT.setString("_ar", getRegistryName());
        if (isClient) {
            getOs().shutdown();
        }
    }

    /**
     * Be careful! do not try to use non-static field or call a non-static function here.
     * This method is called with the registered instance. {@link gregtech.api.terminal.TerminalRegistry#registerApp(AbstractApplication)}
     */
    @SideOnly(Side.CLIENT)
    public void tickAR(EntityPlayer tickEvent) {
    }

    /**
     * Be careful! do not try to use non-static field or call a non-static function here.
     * This method is called with the registered instance. {@link gregtech.api.terminal.TerminalRegistry#registerApp(AbstractApplication)}
     */
    @SideOnly(Side.CLIENT)
    public abstract void drawARScreen(RenderWorldLastEvent event);

}
