package gregtech.common.metatileentities.converter;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.util.GTUtility;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import org.jetbrains.annotations.NotNull;

public class ConverterTrait extends MTETrait {

    private final int amps;
    private final long voltage;

    /**
     * If TRUE, the front facing of the machine will OUTPUT EU, other sides INPUT FE.
     * <p>
     * If FALSE, the front facing of the machine will OUTPUT FE, other sides INPUT EU.
     */
    private boolean feToEu;

    private final IEnergyStorage energyFE = new FEContainer();
    private final IEnergyContainer energyEU = new EUContainer();
    protected long storedEU;

    private final long baseCapacity;

    private long usedAmps;

    public ConverterTrait(MetaTileEntityConverter mte, int amps, boolean feToEu) {
        super(mte);
        this.amps = amps;
        this.feToEu = feToEu;
        this.voltage = GTValues.V[mte.getTier()];
        this.baseCapacity = this.voltage * 16 * amps;
    }

    protected IEnergyContainer getEnergyEUContainer() {
        return energyEU;
    }

    protected IEnergyStorage getEnergyFEContainer() {
        return energyFE;
    }

    public boolean isFeToEu() {
        return feToEu;
    }

    protected void setFeToEu(boolean feToEu) {
        this.feToEu = feToEu;
    }

    public int getBaseAmps() {
        return amps;
    }

    public long getVoltage() {
        return voltage;
    }

    @NotNull
    @Override
    public String getName() {
        return GregtechDataCodes.ENERGY_CONVERTER_TRAIT;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        return null;
    }

    private long extractInternal(long amount) {
        if (amount <= 0) return 0;
        long change = Math.min(storedEU, amount);
        storedEU -= change;
        return change;
    }

    @NotNull
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("StoredEU", storedEU);
        nbt.setBoolean("feToEu", feToEu);
        return nbt;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound nbt) {
        this.storedEU = nbt.getLong("StoredEU");
        this.feToEu = nbt.getBoolean("feToEu");
    }

    @Override
    public void update() {
        super.update();
        this.usedAmps = 0;
        if (!metaTileEntity.getWorld().isRemote) {
            pushEnergy();
        }
    }

    protected void pushEnergy() {
        long energyInserted;
        if (feToEu) { // push out EU
            // Get the EU capability in front of us
            IEnergyContainer container = getCapabilityAtFront(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
            if (container == null) return;

            // make sure we can output at least 1 amp
            long ampsToInsert = Math.min(amps, storedEU / voltage);
            if (ampsToInsert == 0) return;

            // send out energy
            energyInserted = container.acceptEnergyFromNetwork(metaTileEntity.getFrontFacing().getOpposite(), voltage,
                    ampsToInsert) * voltage;
        } else { // push out FE
            // Get the FE capability in front of us
            IEnergyStorage storage = getCapabilityAtFront(CapabilityEnergy.ENERGY);
            if (storage == null) return;

            // send out energy
            energyInserted = FeCompat.insertEu(storage, storedEU);
        }
        extractInternal(energyInserted);
    }

    protected <T> T getCapabilityAtFront(Capability<T> capability) {
        TileEntity tile = metaTileEntity.getNeighbor(metaTileEntity.getFrontFacing());
        if (tile == null) return null;
        EnumFacing opposite = metaTileEntity.getFrontFacing().getOpposite();
        return tile.getCapability(capability, opposite);
    }

    // -- GTCEu Energy--------------------------------------------

    public class EUContainer implements IEnergyContainer {

        @Override
        public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
            if (amperage <= 0 || voltage <= 0 || feToEu || side == metaTileEntity.getFrontFacing())
                return 0;
            if (usedAmps >= amps) return 0;
            if (voltage > getInputVoltage()) {
                metaTileEntity.doExplosion(GTUtility.getExplosionPower(voltage));
                return Math.min(amperage, amps - usedAmps);
            }

            long space = baseCapacity - storedEU;
            if (space < voltage) return 0;
            long maxAmps = Math.min(Math.min(amperage, amps - usedAmps), space / voltage);
            storedEU += voltage * maxAmps;
            usedAmps += maxAmps;
            return maxAmps;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return !feToEu && side != metaTileEntity.getFrontFacing();
        }

        @Override
        public long changeEnergy(long amount) {
            if (amount == 0) return 0;
            return amount > 0 ? addEnergy(amount) : removeEnergy(-amount);
        }

        @Override
        public long addEnergy(long energyToAdd) {
            if (energyToAdd <= 0) return 0;
            long original = energyToAdd;

            // add energy to internal buffer
            long change = Math.min(baseCapacity - storedEU, energyToAdd);
            storedEU += change;
            energyToAdd -= change;

            return original - energyToAdd;
        }

        @Override
        public long removeEnergy(long energyToRemove) {
            return extractInternal(energyToRemove);
        }

        @Override
        public long getEnergyStored() {
            return storedEU;
        }

        @Override
        public long getEnergyCapacity() {
            return baseCapacity;
        }

        @Override
        public long getInputAmperage() {
            return feToEu ? 0 : amps;
        }

        @Override
        public long getInputVoltage() {
            return voltage;
        }

        @Override
        public long getOutputAmperage() {
            return feToEu ? amps : 0;
        }

        @Override
        public long getOutputVoltage() {
            return voltage;
        }
    }

    // -- Forge Energy--------------------------------------------

    public class FEContainer implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!feToEu || maxReceive <= 0) return 0;
            int received = Math.min(getMaxEnergyStored() - getEnergyStored(), maxReceive);
            received -= received % FeCompat.ratio(true); // avoid rounding issues
            if (!simulate) storedEU += FeCompat.toEu(received, FeCompat.ratio(true));
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return FeCompat.toFeBounded(storedEU, FeCompat.ratio(feToEu), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return FeCompat.toFeBounded(baseCapacity, FeCompat.ratio(feToEu), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return feToEu;
        }
    }
}
