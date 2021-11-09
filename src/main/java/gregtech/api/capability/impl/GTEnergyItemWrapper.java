package gregtech.api.capability.impl;

import gregtech.api.capability.FeCompat;
import gregtech.api.capability.IElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.BiConsumer;

public class GTEnergyItemWrapper implements IElectricItem {

    /**
     * Capability Provider of the FE TileEntity for the EU-capability.
     */
    private final ICapabilityProvider upvalue;

    /**
     * Capability holder for the FE-capability.
     */
    private IEnergyStorage energyStorage;

    protected GTEnergyItemWrapper(ICapabilityProvider upvalue) {
        this.upvalue = upvalue;
    }

    /**
     * Test this EnergyContainer for sided Capabilities.
     *
     * @param side The side of the TileEntity to test for the Capability
     * @return True if side has Capability, false otherwise
     */
    protected boolean isValid(EnumFacing side) {
        return upvalue.hasCapability(CapabilityEnergy.ENERGY, side);
    }

    private IEnergyStorage getEnergyStorage() {
        if (energyStorage == null) {
            energyStorage = upvalue.getCapability(CapabilityEnergy.ENERGY, null);
        }
        return energyStorage;
    }

    /**
     * Safely cast a Long to an Int without overflow.
     *
     * @param v The Long value to cast to an Int.
     * @return v, casted to Int, or Integer.MAX_VALUE if it would overflow.
     */
    public static int safeCastLongToInt(long v) {
        return v > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) v;
    }

    @Override
    public boolean canProvideChargeExternally() {
        return true;
    }

    @Override
    public boolean chargeable() {
        IEnergyStorage storage = getEnergyStorage();
        if (storage != null)
            return storage.canReceive();
        return false;
    }

    @Override
    public void addChargeListener(BiConsumer<ItemStack, Long> chargeListener) {

    }

    @Override
    public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return 0;
        int max = Math.min(FeCompat.convertToFe(amount), storage.getMaxEnergyStored() - storage.getEnergyStored());
        return FeCompat.convertToEu(storage.receiveEnergy(max, simulate));
    }

    @Override
    public long discharge(long amount, int dischargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
        IEnergyStorage storage = getEnergyStorage();
        if (storage == null) return 0;
        int max = Math.min(FeCompat.convertToFe(amount), storage.getEnergyStored());
        return FeCompat.convertToEu(storage.extractEnergy(max, simulate));
    }

    @Override
    public long getTransferLimit() {
        return getMaxCharge();
    }

    @Override
    public long getMaxCharge() {
        IEnergyStorage storage = getEnergyStorage();
        if (storage != null)
            return FeCompat.convertToEu(storage.getMaxEnergyStored());
        return 0;
    }

    @Override
    public long getCharge() {
        IEnergyStorage storage = getEnergyStorage();
        if (storage != null)
            return FeCompat.convertToEu(storage.getEnergyStored());
        return 0;
    }

    @Override
    public int getTier() {
        return 0;
    }
}
