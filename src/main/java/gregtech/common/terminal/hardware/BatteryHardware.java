package gregtech.common.terminal.hardware;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.terminal.hardware.IHardwareCapability;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/28
 * @Description:
 */
public class BatteryHardware extends Hardware implements IElectricItem, IHardwareCapability {

    protected final List<BiConsumer<ItemStack, Long>> listeners = new ArrayList<>();

    public BatteryHardware() {}

    @Override
    public boolean isHardwareAdequate(Hardware demand) {
        return demand instanceof BatteryHardware && ((BatteryHardware) demand).getTier() <= this.getTier() &&
                this.getCharge() > 0;
    }

    @Override
    public String addInformation() {
        return GTValues.VN[getTier()];
    }

    @Override
    public Hardware createHardware(ItemStack itemStack) {
        return new BatteryHardware();
    }

    @Override
    public NBTTagCompound acceptItemStack(ItemStack itemStack) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null || !electricItem.canProvideChargeExternally() || !electricItem.chargeable()) {
            return null;
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("maxCharge", electricItem.getMaxCharge());
        nbt.setLong("charge", electricItem.getCharge());
        nbt.setInteger("tier", electricItem.getTier());
        return nbt;
    }

    @Override
    public String getRegistryName() {
        return "battery";
    }

    @Override
    public IGuiTexture getIcon() {
        if (!hasHW()) {
            return super.getIcon();
        }
        return new ItemStackTexture(isCreative() ? MetaItems.ULTIMATE_BATTERY.getInfiniteChargedStack() : getItem());
    }

    @Override
    public void addChargeListener(BiConsumer<ItemStack, Long> chargeListener) {
        listeners.add(chargeListener);
    }

    public void setCharge(long change) {
        getNBT().setLong("charge", change);
        listeners.forEach(l -> l.accept(provider.getItemStack(), change));
    }

    @Override
    public long getTransferLimit() {
        return GTValues.V[getTier()];
    }

    @Override
    public long getMaxCharge() {
        return isCreative() ? Long.MAX_VALUE : getNBT().getLong("maxCharge");
    }

    public long getCharge() {
        return isCreative() ? Long.MAX_VALUE : getNBT().getLong("charge");
    }

    @Override
    public boolean canProvideChargeExternally() {
        return false;
    }

    @Override
    public boolean chargeable() {
        return true;
    }

    @Override
    public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
        if (provider.getItemStack().getCount() != 1) {
            return 0L;
        }
        if ((chargerTier >= getTier()) && amount > 0L) {
            long canReceive = getMaxCharge() - getCharge();
            if (!ignoreTransferLimit) {
                amount = Math.min(amount, getTransferLimit());
            }
            long charged = Math.min(amount, canReceive);
            if (!simulate) {
                setCharge(getCharge() + charged);
            }
            return charged;
        }
        return 0;
    }

    @Override
    public long discharge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean externally,
                          boolean simulate) {
        if (provider.getItemStack().getCount() != 1) {
            return 0L;
        }
        if ((!externally || amount == Long.MAX_VALUE) && (chargerTier >= getTier()) && amount > 0L) {
            if (!ignoreTransferLimit) {
                amount = Math.min(amount, getTransferLimit());
            }
            long charge = getCharge();
            long discharged = Math.min(amount, charge);
            if (!simulate) {
                setCharge(charge - discharged);
            }
            return discharged;
        }
        return 0;
    }

    @Override
    public int getTier() {
        return isCreative() ? GTValues.V.length - 1 : getNBT().getInteger("tier");
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability) {
        return capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM;
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability) {
        return capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM ?
                GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM.cast(this) : null;
    }

    @Override
    public ItemStack onHardwareRemoved(ItemStack itemStack) {
        IElectricItem item = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (item instanceof ElectricItem) {
            ((ElectricItem) item).setCharge(getCharge());
        }
        return itemStack;
    }

    public static class BatteryDemand extends BatteryHardware {

        public final int tier;
        public final long cost;

        public BatteryDemand(int tier, long cost) {
            this.tier = tier;
            this.cost = cost;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public long getCharge() {
            return this.getCost();
        }

        public long getCost() {
            return cost;
        }
    }
}
