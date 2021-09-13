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
 * When AR is active, {@link #tickAR(EntityPlayer)} and {@link #drawARScreen(RenderWorldLastEvent)} will be called when you hold the terminal in one of your hands.
 * Therefore, at most one AR app is active on the terminal at any one time. And when you open the terminal GUI it automatically closes the currently active AR.
 * if you want to do something on the server during active mode, plz send packets. Because it's always running on the client side.
 * But you have access to the NBT of the handheld terminal when the AR is active, to load configs, init and so on.
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

    /**
     * open Camera for this AR and terminal shutdown.
     * then, this AR will be in active.
     * It is best to call it on both sides to make sure when you always have an active terminal until this AR is closed.
     */
    protected final void openAR() {
        os.tabletNBT.setString("_ar", getRegistryName());
        if (isClient) {
            getOs().shutdown();
        }
    }

    /**
     * Be careful not to try to use non-static field or call a non-static function here.
     * This method is called with the registered instance. {@link gregtech.api.terminal.TerminalRegistry#registerApp(AbstractApplication)}
     */
    @SideOnly(Side.CLIENT)
    public void tickAR(EntityPlayer tickEvent) {
    }

    /**
     * Be careful not to try to use non-static field or call a non-static function here.
     * This method is called with the registered instance. {@link gregtech.api.terminal.TerminalRegistry#registerApp(AbstractApplication)}
     */
    @SideOnly(Side.CLIENT)
    public abstract void drawARScreen(RenderWorldLastEvent event);

}
