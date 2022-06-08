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
        for (DEVICE device : DEVICE.values()) {
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
            return new ItemStackTexture(DEVICE.values()[slot % DEVICE.values().length].itemStack);
        }
        return new ItemStackTexture(getDevice().itemStack);
    }

    @Override
    public boolean isHardwareAdequate(Hardware demand) {
        if (demand instanceof DeviceHardware && isCreative()) {
            return DEVICE.values()[slot % DEVICE.values().length] == ((DeviceHardware) demand).getDevice();
        }
        return demand instanceof DeviceHardware && ((DeviceHardware) demand).getDevice() == this.getDevice();
    }

    @Override
    public String addInformation() {
        if (isCreative()) {
            return DEVICE.values()[slot % DEVICE.values().length].itemStack.getDisplayName();
        }
        return getDevice().itemStack.getDisplayName();
    }

    public DEVICE getDevice() {
        return DEVICE.values()[getNBT().getInteger("d") % DEVICE.values().length];
    }

    public enum DEVICE{
        PROSPECTOR_LV(MetaItems.PROSPECTOR_LV, "prospector"),
        PROSPECTOR_HV(MetaItems.PROSPECTOR_HV, "advanced_prospector"),
        WIRELESS(MetaItems.WIRELESS, "wireless"),
        CAMERA(MetaItems.CAMERA, "camera"),

        // FIELD GEN
        FIELD_GENERATOR_UV(MetaItems.FIELD_GENERATOR_UV, "field_generator"),

        // SOLAR
        SOLAR_LV(MetaItems.COVER_SOLAR_PANEL_LV, "solar_lv");

        ItemStack itemStack;
        String name;

        DEVICE(ItemStack itemStack, String name){
            this.itemStack = itemStack;
            this.name = name;
        }
        DEVICE(MetaItem<?>.MetaValueItem metaItem, String name){
            this.itemStack = metaItem.getStackForm();
            this.name = name;
        }
        public static DEVICE fromString(String name) {
            for (DEVICE device : values()) {
                if (device.name.equals(name.toLowerCase())) {
                    return device;
                }
            }
            return null;
        }
    }

    public static class DeviceDemand extends DeviceHardware{
        private final DEVICE device;

        public DeviceDemand(DEVICE device) {
            super(0);
            this.device = device;
        }

        public DeviceDemand(String device) {
            super(0);
            this.device = DEVICE.fromString(device);
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
