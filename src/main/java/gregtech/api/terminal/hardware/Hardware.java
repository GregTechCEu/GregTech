package gregtech.api.terminal.hardware;

import gregtech.common.items.behaviors.TerminalBehaviour;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/27
 * @Description: Hardware
 */
public abstract class Hardware  {
    protected HardwareProvider provider;

    public abstract String getRegistryName();

    public final String getUnlocalizedName() {
        return "terminal.hw." + getRegistryName();
    }

    /***
     * Check whether the current hardware (this) meets requirement (demand);
     */
    public boolean isHardwareAdequate(Hardware demand) {
        return this.getClass() == demand.getClass() || this.getRegistryName().equals(demand.getRegistryName());
    }

    /**
     * Check whether the terminal has the hardware.
     */
    public final boolean hasHW() {
        return provider.hasHardware(getRegistryName());
    }

    /**
     * Returns the NBT of the this hardware.
     */
    public final NBTTagCompound getNBT() {
        return provider.getHardwareNBT(getRegistryName());
    }

    /**
     * Check whether the terminal is creative mode.
     */
    public final boolean isCreative(){
        return TerminalBehaviour.isCreative(provider.getItemStack());
    }

    /**
     * information added in tooltips
     * @return null->nothing added.
     */
    @SideOnly(Side.CLIENT)
    public String addInformation() {
        return null;
    }

    /**
     * Create the hardware instance, NOTE!!! do not check nbt or anything here. Terminal has not been initialized here.
     * @param itemStack terminal
     * @return instance
     */
    protected abstract Hardware createHardware(ItemStack itemStack);
}
