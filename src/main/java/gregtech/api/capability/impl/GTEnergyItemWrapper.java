package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class GTEnergyItemWrapper implements IElectricItem {

    public static final double EU_TO_FE = ConfigHolder.U.energyOptions.euToFeRatio;
    public static final double FE_TO_EU = ConfigHolder.U.energyOptions.feToEuRatio;

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
        if(energyStorage == null) {
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
        if(storage != null)
            return storage.canReceive();
        return false;
    }

    @Override
    public void addChargeListener(BiConsumer<ItemStack, Long> chargeListener) {

    }

    @Override
    public long charge(long amount, int chargerTier, boolean ignoreTransferLimit, boolean simulate) {
        IEnergyStorage storage = getEnergyStorage();
        if(storage == null) return 0;
        int max = (int) Math.min(amount * EU_TO_FE, storage.getMaxEnergyStored() - storage.getEnergyStored());
        return (long) (storage.receiveEnergy(max, simulate) * FE_TO_EU);
    }

    @Override
    public long discharge(long amount, int dischargerTier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
        IEnergyStorage storage = getEnergyStorage();
        if(storage == null) return 0;
        int max = (int) Math.min(amount * EU_TO_FE, storage.getEnergyStored());
        return (long) (storage.extractEnergy(max, simulate) * FE_TO_EU);
    }

    @Override
    public long getTransferLimit() {
        return getMaxCharge();
    }

    @Override
    public long getMaxCharge() {
        IEnergyStorage storage = getEnergyStorage();
        if(storage != null)
            return (long) (storage.getMaxEnergyStored() * FE_TO_EU);
        return 0;
    }

    @Override
    public long getCharge() {
        IEnergyStorage storage = getEnergyStorage();
        if(storage != null)
            return (long) (storage.getEnergyStored() * FE_TO_EU);
        return 0;
    }

    @Override
    public int getTier() {
        return 0;
    }
}
