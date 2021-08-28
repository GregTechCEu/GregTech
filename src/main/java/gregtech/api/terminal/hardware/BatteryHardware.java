package gregtech.api.terminal.hardware;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public class BatteryHardware extends Hardware implements IElectricItem {
    protected final List<BiConsumer<ItemStack, Long>> listeners = new ArrayList<>();

    public BatteryHardware() {
    }

    @Override
    public boolean isHardwareAdequate(Hardware demand) {
        return demand instanceof BatteryHardware && ((BatteryHardware) demand).getTier() < this.getTier();
    }

    @Override
    public Hardware createHardware(ItemStack itemStack) {
        return new BatteryHardware();
    }

    @Override
    public String getRegistryName() {
        return "battery";
    }

    @Override
    public void addChargeListener(BiConsumer<ItemStack, Long> chargeListener) {
        listeners.add(chargeListener);
    }

    protected void setCharge(long change) {
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
    public long discharge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
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
    public boolean hasCapability(@Nonnull Capability<?> capability) {
        return capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability) {
        return capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM ? (T) this : null;
    }
}
