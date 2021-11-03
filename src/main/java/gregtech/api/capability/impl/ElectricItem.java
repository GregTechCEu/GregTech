package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ElectricItem implements IElectricItem, ICapabilityProvider {

    protected final ItemStack itemStack;

    protected final long maxCharge;
    protected final int tier;

    protected final boolean chargeable;
    protected final boolean canProvideEnergyExternally;

    protected final List<BiConsumer<ItemStack, Long>> listeners = new ArrayList<>();

    public ElectricItem(ItemStack itemStack, long maxCharge, int tier, boolean chargeable, boolean canProvideEnergyExternally) {
        this.itemStack = itemStack;
        this.maxCharge = maxCharge;
        this.tier = tier;
        this.chargeable = chargeable;
        this.canProvideEnergyExternally = canProvideEnergyExternally;
    }

    @Override
    public void addChargeListener(BiConsumer<ItemStack, Long> chargeListener) {
        listeners.add(chargeListener);
    }

    public void setCharge(long change) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        //noinspection ConstantConditions
        itemStack.getTagCompound().setLong("Charge", change);
        listeners.forEach(l -> l.accept(itemStack, change));
    }

    public void setMaxChargeOverride(long maxCharge) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        //noinspection ConstantConditions
        itemStack.getTagCompound().setLong("MaxCharge", maxCharge);
    }

    @Override
    public long getTransferLimit() {
        return GTValues.V[getTier()];
    }

    @Override
    public long getMaxCharge() {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null)
            return maxCharge;
        if (tagCompound.hasKey("MaxCharge", NBT.TAG_LONG))
            return tagCompound.getLong("MaxCharge");
        return maxCharge;
    }

    public long getCharge() {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null)
            return 0;
        if (tagCompound.getBoolean("Infinite"))
            return getMaxCharge();
        return Math.min(tagCompound.getLong("Charge"), getMaxCharge());
    }

    public void setInfiniteCharge(boolean infiniteCharge) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        //noinspection ConstantConditions
        itemStack.getTagCompound().setBoolean("Infinite", infiniteCharge);
        listeners.forEach(l -> l.accept(itemStack, getMaxCharge()));
    }

    @Override
    public boolean canProvideChargeExternally() {
        return this.canProvideEnergyExternally;
    }

    @Override
    public boolean chargeable() {
        return chargeable;
    }

    @Override
    public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
        if (itemStack.getCount() != 1) {
            return 0L;
        }
        if ((chargeable || amount == Long.MAX_VALUE) && (chargerTier >= tier) && amount > 0L) {
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
        if (itemStack.getCount() != 1) {
            return 0L;
        }
        if ((canProvideEnergyExternally || !externally || amount == Long.MAX_VALUE) && (chargerTier >= tier) && amount > 0L) {
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
        return tier;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM ? (T) this : null;
    }
}
