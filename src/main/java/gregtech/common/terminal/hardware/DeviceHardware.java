package gregtech.common.terminal.hardware;

import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.common.items.MetaItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DeviceHardware extends Hardware {
    private final int slot;

    public DeviceHardware(int slot) {
        this.slot = slot;
    }

    @Override
    public String getRegistryName() {
        return "device" + slot;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getLocalizedName() {
        return I18n.format("terminal.hw.device") + " " + slot;
    }

    @Override
    protected Hardware createHardware(ItemStack itemStack) {
        return new DeviceHardware(slot);
    }

    @Override
    public NBTTagCompound acceptItemStack(ItemStack itemStack) {
        for (DEVICE device : DEVICE.VALUES) {
            if (device.itemStack.isItemEqual(itemStack)) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("d", device.ordinal());
                return nbt;
            }
        }
        return null;
    }

    @Override
    public IGuiTexture getIcon() {
        if (!hasHW()) {
            return super.getIcon();
        }
        if (isCreative()) {
            return new ItemStackTexture(DEVICE.VALUES[slot % DEVICE.VALUES.length].itemStack);
        }
        return new ItemStackTexture(getDevice().itemStack);
    }

    @Override
    public boolean isHardwareAdequate(Hardware demand) {
        if (demand instanceof DeviceHardware && isCreative()) {
            return DEVICE.VALUES[slot % DEVICE.VALUES.length] == ((DeviceHardware) demand).getDevice();
        }
        return demand instanceof DeviceHardware && ((DeviceHardware) demand).getDevice() == this.getDevice();
    }

    @Override
    public String addInformation() {
        if (isCreative()) {
            return DEVICE.VALUES[slot % DEVICE.VALUES.length].itemStack.getDisplayName();
        }
        return getDevice().itemStack.getDisplayName();
    }

    public DEVICE getDevice() {
        return DEVICE.VALUES[getNBT().getInteger("d")];
    }

    public enum DEVICE{
        SCANNER(MetaItems.SCANNER),
        WIRELESS(MetaItems.WIRELESS),
        CAMERA(MetaItems.CAMERA),

        // SOLAR
        SOLAR_LV(MetaItems.COVER_SOLAR_PANEL_LV);

        public static final DEVICE[] VALUES;

        static {
            VALUES = values();
        }

        ItemStack itemStack;

        DEVICE(ItemStack itemStack){
            this.itemStack = itemStack;
        }
        DEVICE(MetaItem<?>.MetaValueItem metaItem){
            this.itemStack = metaItem.getStackForm();
        }
    }

    public static class DeviceDemand extends DeviceHardware{
        private final DEVICE device;

        public DeviceDemand(DEVICE device) {
            super(0);
            this.device = device;
        }

        @Override
        public String getLocalizedName() {
            return I18n.format("terminal.hw.device");
        }

        @Override
        public DEVICE getDevice() {
            return device;
        }
    }
}
