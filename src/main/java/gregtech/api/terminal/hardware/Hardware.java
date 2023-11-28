package gregtech.api.terminal.hardware;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.IGuiTexture;

import net.minecraft.client.resources.I18n;
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
public abstract class Hardware {

    protected HardwareProvider provider;

    public abstract String getRegistryName();

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        return I18n.format("terminal.hw." + getRegistryName());
    }

    @SideOnly(Side.CLIENT)
    public IGuiTexture getIcon() {
        return GuiTextures.ICON_REMOVE;
    }

    /**
     * Check whether the current hardware (this) meets requirement (demand);
     */
    public boolean isHardwareAdequate(Hardware demand) {
        return this.getClass() == demand.getClass() || this.getRegistryName().equals(demand.getRegistryName());
    }

    /**
     * Check whether the terminal has this hardware.
     */
    public final boolean hasHW() {
        return provider != null && provider.hasHardware(getRegistryName());
    }

    public final ItemStack getItem() {
        return provider.getHardwareItem(getRegistryName());
    }

    /**
     * Returns the NBT of the this hardware.
     */
    public final NBTTagCompound getNBT() {
        return provider.getHardwareNBT(getRegistryName());
    }

    /**
     * Check whether the terminal is in creative mode.
     */
    public final boolean isCreative() {
        return provider != null && provider.isCreative();
    }

    /**
     * information added to tooltips
     * 
     * @return null->nothing added.
     */
    @SideOnly(Side.CLIENT)
    public String addInformation() {
        return null;
    }

    /**
     * Create the hardware instance, NOTE!!! do not check nbt or anything here. Terminal has not been initialized here.
     * 
     * @param itemStack terminal
     * @return instance
     */
    protected abstract Hardware createHardware(ItemStack itemStack);

    /**
     * Use the item to install this hardware.
     * 
     * @return The NBT of the hardware is returned if the item is valid, otherwise NULL is returned
     */
    public abstract NBTTagCompound acceptItemStack(ItemStack itemStack);

    /**
     * Called when the hardware is removed and back to the player inventory.
     * 
     * @param itemStack (original one)
     * @return result
     */
    public ItemStack onHardwareRemoved(ItemStack itemStack) {
        return itemStack;
    }
}
