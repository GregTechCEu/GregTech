package gregtech.common.metatileentities.converter;

import com.google.common.math.IntMath;
import com.google.common.math.LongMath;
import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ConverterTrait extends MTETrait {

    private final int baseAmps;
    private final int tier;
    private final long voltage;
    private boolean feToEu;

    private final IEnergyStorage energyFE = new FEContainer();
    private final IEnergyContainer energyEU = new EUContainer();
    private long storedEU;
    private long storedFE;

    private final long baseCapacity;

    private long usedAmps;

    public ConverterTrait(MetaTileEntityConverter mte, int tier, int baseAmps, boolean feToEu) {
        super(mte);
        this.baseAmps = baseAmps;
        this.feToEu = feToEu;
        this.tier = tier;
        this.voltage = GTValues.V[tier];
        this.baseCapacity = this.voltage * 8 * baseAmps;
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
        return baseAmps;
    }

    public long getVoltage() {
        return voltage;
    }

    @Override
    public String getName() {
        return "EnergyConvertTrait";
    }

    @Override
    public int getNetworkID() {
        return 1;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        return null;
    }

    private int feCapacity(){
        return FeCompat.toFe(baseCapacity, true);
    }

    private long chargeBattery(long amount, boolean simulate) {
        if (amount <= 0) return 0;
        long original = amount;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots() && amount > 0; i++) {
            IElectricItem electricItem = getBatteryContainer(inventory.getStackInSlot(i));
            if (electricItem == null) continue;
            amount -= electricItem.charge(amount, tier, false, simulate);
        }
        return original - amount;
    }

    private long extractInternal(long amount, boolean simulate) {
        if (amount <= 0) return 0;
        long original = amount;

        // remove energy first from internal buffer
        long change = Math.min(storedEU, amount);
        if (!simulate) storedEU -= change;
        amount -= change;

        // then from batteries
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots() && amount > 0; i++) {
            IElectricItem electricItem = getBatteryContainer(inventory.getStackInSlot(i));
            if (electricItem == null) continue;
            amount -= electricItem.discharge(amount, tier, false, false, simulate);
        }
        return original - amount;
    }

    public IElectricItem getBatteryContainer(ItemStack itemStack) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null && electricItem.canProvideChargeExternally())
            return electricItem;
        return null;
    }

    protected IItemHandlerModifiable getInventory() {
        return metaTileEntity.getImportItems();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("StoredEU", storedEU);
        nbt.setLong("StoredFE", storedFE);
        nbt.setBoolean("feToEu", feToEu);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.storedEU = nbt.getLong("StoredEU");
        this.storedFE = nbt.getLong("StoredFE");
        this.feToEu = nbt.getBoolean("feToEu");
    }

    @Override
    public void update() {
        super.update();
        this.usedAmps = 0;
        if (metaTileEntity.getWorld().isRemote) return;

        pushEnergy();

        if (feToEu && storedEU < baseCapacity) {
            long eu = Math.min(FeCompat.toEu(storedFE, true), baseCapacity - storedEU);
            this.storedEU += eu;
            this.storedFE -= FeCompat.toFe(eu, true);
        }
        this.storedEU -= chargeBattery(storedEU, false);
    }

    private void pushEnergy() {
        long inputAmp = energyEU.getInputAmperage();
        long extractable = extractInternal(inputAmp * voltage, true);
        if (extractable <= 0) return;
        if (feToEu) {
            IEnergyContainer container = getCapabilityAtFront(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
            if (container == null) return;

            // check how much can be extracted
            long ampsToInsert = Math.min(inputAmp, extractable / voltage);

            // if the energy is not enough for a full package, make it smaller (for batteries below the converters tier)
            long volt = voltage;
            if (ampsToInsert == 0) {
                ampsToInsert = 1;
                volt = extractable;
            }

            long inserted = volt * container.acceptEnergyFromNetwork(metaTileEntity.getFrontFacing().getOpposite(), volt, ampsToInsert);
            extractInternal(inserted, false);
        } else {
            IEnergyStorage storage = getCapabilityAtFront(CapabilityEnergy.ENERGY);
            if (storage == null) return;

            long inserted = FeCompat.toEu(storage.receiveEnergy(FeCompat.toFe(extractable, false), false), false);
            extractInternal(inserted, false);
        }
    }

    private <T> T getCapabilityAtFront(Capability<T> capability) {
        TileEntity tile = metaTileEntity.getWorld().getTileEntity(metaTileEntity.getPos().offset(metaTileEntity.getFrontFacing()));
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
            long inputAmps = getInputAmperage();
            if (usedAmps >= inputAmps) return 0;
            if (voltage > getInputVoltage()) {
                GTUtility.doOvervoltageExplosion(metaTileEntity, voltage);
                return Math.min(amperage, inputAmps - usedAmps);
            }

            long space = baseCapacity - storedEU;
            if (space <= voltage) return 0;
            long maxAmps = Math.min(Math.min(amperage, inputAmps - usedAmps), space / voltage);
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
            if (amount == 0)
                return 0;
            return amount > 0 ? addEnergy(amount) : removeEnergy(-amount);
        }

        @Override
        public long addEnergy(long energyToAdd) {
            if (energyToAdd <= 0)
                return 0;
            long original = energyToAdd;

            // add energy first to internal buffer
            long change = Math.min(baseCapacity - storedEU, energyToAdd);
            storedEU += change;
            energyToAdd -= change;

            // then to batteries
            energyToAdd -= chargeBattery(energyToAdd, false);

            return original - energyToAdd;
        }

        @Override
        public long removeEnergy(long energyToRemove) {
            return extractInternal(energyToRemove, false);
        }

        @Override
        public long getEnergyStored() {
            long energyStored = storedEU;
            IItemHandlerModifiable inventory = getInventory();
            for (int i = 0; i < inventory.getSlots(); i++) {
                IElectricItem electricItem = getBatteryContainer(inventory.getStackInSlot(i));
                if (electricItem == null) continue;
                energyStored = LongMath.saturatedAdd(electricItem.getCharge(), energyStored);
                if (energyStored == Long.MAX_VALUE) break;
            }
            return energyStored;
        }

        @Override
        public long getEnergyCapacity() {
            long energyStored = baseCapacity;
            IItemHandlerModifiable inventory = getInventory();
            for (int i = 0; i < inventory.getSlots(); i++) {
                IElectricItem electricItem = getBatteryContainer(inventory.getStackInSlot(i));
                if (electricItem == null) continue;
                energyStored = LongMath.saturatedAdd(electricItem.getMaxCharge(), energyStored);
                if (energyStored == Long.MAX_VALUE) break;
            }
            return energyStored;
        }

        @Override
        public long getInputAmperage() {
            int inputAmperage = 0;
            IItemHandlerModifiable inventory = getInventory();
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack batteryStack = inventory.getStackInSlot(i);
                IElectricItem electricItem = getBatteryContainer(batteryStack);
                if (electricItem == null) continue;
                inputAmperage++;
            }
            return inputAmperage > 0 ? inputAmperage : baseAmps;
        }

        @Override
        public long getInputVoltage() {
            return voltage;
        }

        @Override
        public long getOutputAmperage() {
            return feToEu ? getInputAmperage() : 0;
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
            int received = (int) Math.min(feCapacity() - storedFE,
                    Math.min(FeCompat.toFe(voltage * energyEU.getInputAmperage(), true), maxReceive));
            if (!simulate) storedFE += received;
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return IntMath.saturatedAdd(FeCompat.toFe(energyEU.getEnergyStored(), feToEu),
                    storedFE >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) storedFE);
        }

        @Override
        public int getMaxEnergyStored() {
            return IntMath.saturatedAdd(FeCompat.toFe(energyEU.getEnergyCapacity(), feToEu), feCapacity());
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
