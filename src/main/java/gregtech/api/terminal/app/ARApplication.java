package gregtech.api.terminal.app;

import gregtech.api.terminal.os.SystemCall;
import gregtech.common.items.behaviors.TerminalBehaviour;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/13
 * @Description: Application for AR.
 *               When AR is running, {@link #tickAR(EntityPlayer)} and {@link #drawARScreen(RenderWorldLastEvent)} will
 *               be called when you hold the terminal in one of your hands.
 *               Therefore, at most one AR app is active on the terminal at any one time. And when you open the terminal
 *               GUI it automatically closes the currently running AR.
 *               You have access to the app's NBT of the handheld terminal when the AR is active, to load configs, init
 *               and so on.
 *               Don't try to write NBT, you should always be aware that the AR is running on the client side.
 *               if you really want to do something on the server side when AR is running, plz send packets. Because
 *               it's always running on the client side!!!!!!!!!!
 *               (If you need data from NBT, dont forget to write nbt when closeApp {@link #closeApp()})
 */
public abstract class ARApplication extends AbstractApplication {

    protected ItemStack heldStack;

    public ARApplication(String name) {
        super(name);
    }

    @Override
    public int getAppTier() {
        if (nbt != null) {
            if (os != null) {
                return super.getAppTier();
            } else if (TerminalBehaviour.isCreative(heldStack)) {
                return getMaxTier();
            }
            return Math.min(nbt.getInteger("_tier"), getMaxTier());
        }
        return 0;
    }

    @SideOnly(Side.CLIENT)
    public final void setAROpened(ItemStack heldStack) {
        this.heldStack = heldStack;
        this.nbt = heldStack.getOrCreateSubCompound("terminal").getCompoundTag(getRegistryName());
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
            SystemCall.SHUT_DOWN.call(getOs(), true);
        }
    }

    /**
     * this will be fired every time you first switch selected slot to the held terminal.
     */
    @SideOnly(Side.CLIENT)
    public void onAROpened() {}

    /**
     * this will be fired when switch the current slot (terminal) to other slots or open this terminal.
     */
    @SideOnly(Side.CLIENT)
    public void onARClosed() {
        nbt = null;
        heldStack = null;
    }

    /**
     * Be careful! do not try to use non-static field or call a non-static function here.
     * This method is called with the registered instance.
     * {@link gregtech.api.terminal.TerminalRegistry#registerApp(AbstractApplication)}
     */
    @SideOnly(Side.CLIENT)
    public void tickAR(EntityPlayer player) {}

    /**
     * Be careful! do not try to use non-static field or call a non-static function here.
     * This method is called with the registered instance.
     * {@link gregtech.api.terminal.TerminalRegistry#registerApp(AbstractApplication)}
     */
    @SideOnly(Side.CLIENT)
    public abstract void drawARScreen(RenderWorldLastEvent event);
}
