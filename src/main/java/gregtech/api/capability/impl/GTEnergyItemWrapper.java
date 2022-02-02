package gregtech.api.capability.impl;

import gregtech.api.capability.FeCompat;
import gregtech.api.capability.IElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.BiConsumer;

public class GTEnergyItemWrapper implements IElectricItem {

    /**
     * Capability holder for the FE-capability.
     */
    private final IEnergyStorage energyStorage;

    public GTEnergyItemWrapper(IEnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    @Override
    public boolean canProvideChargeExternally() {
        return energyStorage.canExtract();
    }

    @Override
    public boolean chargeable() {
        return energyStorage.canReceive();
    }

    @Override
    public void addChargeListener(BiConsumer<ItemStack, Long> chargeListener) {

    }

    @Override
    public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
        if (amount <= 0) return 0;
        return FeCompat.nativeToEu(energyStorage.receiveEnergy(FeCompat.nativeToFe(amount), simulate));
    }

    @Override
    public long discharge(long amount, int dischargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
        if (amount <= 0) return 0;
        return FeCompat.nativeToEu(energyStorage.extractEnergy(FeCompat.nativeToFe(amount), simulate));
    }

    @Override
    public long getTransferLimit() {
        return getMaxCharge();
    }

    @Override
    public long getMaxCharge() {
        return FeCompat.nativeToEu(energyStorage.getMaxEnergyStored());
    }

    @Override
    public long getCharge() {
        return FeCompat.nativeToEu(energyStorage.getEnergyStored());
    }

    @Override
    public int getTier() {
        return 0;
    }
}
