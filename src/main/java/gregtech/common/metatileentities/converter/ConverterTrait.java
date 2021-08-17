package gregtech.common.metatileentities.converter;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.BitSet;

public class ConverterTrait extends MTETrait {

    public static final double EU_TO_FE = ConfigHolder.U.energyOptions.euToFeRatio;
    public static final double FE_TO_EU = ConfigHolder.U.energyOptions.feToEuRatio;

    private final BitSet batterySlotsUsedThisTick = new BitSet();

    private final int baseAmps;
    private final int tier;
    private final long voltage;
    private boolean feToEu;

    private final IEnergyStorage energyFE;
    private final IEnergyContainer energyEU;
    private long storedEU;

    private final long baseCapacity;

    private long usedAmps = 0;
    private int feAdded = 0;

    public ConverterTrait(MetaTileEntityConverter mte, int tier, int baseAmps, boolean feToEu) {
        super(mte);
        this.baseAmps = baseAmps;
        this.feToEu = feToEu;
        this.tier = tier;
        this.voltage = GTValues.V[tier];
        this.baseCapacity = this.voltage * 8 * baseAmps;
        energyFE = new FEContainer();
        energyEU = new EUContainer();
        storedEU = 0;
    }

    protected void setMode(boolean feToEu) {
        this.feToEu = feToEu;
    }

    protected ConverterTrait copyNew(MetaTileEntityConverter mte) {
        return new ConverterTrait(mte, tier, baseAmps, feToEu);
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

    public World getWorld() {
        return metaTileEntity.getWorld();
    }

    public BlockPos getPos() {
        return metaTileEntity.getPos();
    }

    private long addEnergyInternal(long amount, boolean isEu, boolean simulate) {
        if (amount <= 0)
            return 0;
        if (!isEu)
            amount *= FE_TO_EU;
        long original = amount;

        // add energy first to internal buffer
        long change = Math.min(baseCapacity - storedEU, amount);
        if (!simulate)
            storedEU += change;
        amount -= change;

        // then to batteries
        amount -= addBatteryEnergy(amount, simulate);

        return isEu ? original - amount : (long) ((original - amount) * EU_TO_FE);
    }

    private long removeEnergyInternal(long amount, boolean isEu, boolean simulate) {
        if (amount <= 0)
            return 0;
        if (!isEu)
            amount *= FE_TO_EU;
        long original = amount;

        // remove energy first from batteries
        amount -= removeBatteryEnergy(amount, simulate);

        // then from internal buffer
        long change = Math.min(storedEU, amount);
        if (!simulate)
            storedEU -= change;
        amount -= change;

        return isEu ? original - amount : (long) ((original - amount) * EU_TO_FE);
    }

    private long addBatteryEnergy(long amount, boolean simulate) {
        long original = amount;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (amount == 0) break;
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            long ins = electricItem.charge(amount, tier, false, simulate);
            amount -= ins;
        }
        return original - amount;
    }

    private long removeBatteryEnergy(long amount, boolean simulate) {
        long original = amount;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (amount == 0) break;
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            long ins = electricItem.discharge(amount, tier, false, false, simulate);
            amount -= ins;
        }
        return original - amount;
    }

    private EnumFacing getFront() {
        return metaTileEntity.getFrontFacing();
    }

    private long getEnergyStoredInternal() {
        long energyStored = 0L;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            energyStored += electricItem.getCharge();
        }
        return energyStored + storedEU;
    }

    protected IElectricItem getBatteryContainer(ItemStack itemStack) {
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
        nbt.setBoolean("feToEu", feToEu);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        storedEU = nbt.getLong("StoredEU");
        feToEu = nbt.getBoolean("feToEu");
    }

    @Override
    public void update() {
        super.update();
        usedAmps = 0;
        feAdded = 0;
        this.batterySlotsUsedThisTick.clear();
        if (getWorld().isRemote) return;
        TileEntity tile = getWorld().getTileEntity(getPos().offset(getFront()));
        if (tile == null) return;
        EnumFacing opposite = getFront().getOpposite();
        // check how much can be extracted
        long extractable = removeEnergyInternal(Long.MAX_VALUE, true, true);
        if (extractable == 0)
            return;
        long ampsToInsert = Math.min(energyEU.getInputAmperage(), extractable / voltage);
        if (ampsToInsert * voltage > extractable) {
            ampsToInsert = extractable / voltage;
        }
        if (feToEu) {
            IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, opposite);
            if (container != null) {
                // if the energy is not enough for a full package, make it smaller (for batteries below the converters tier)
                long volt = voltage;
                if (ampsToInsert == 0) {
                    ampsToInsert = 1;
                    volt = extractable;
                }

                long ampsUsed = container.acceptEnergyFromNetwork(opposite, volt, ampsToInsert);
                if (removeEnergyInternal(volt * ampsUsed, true, false) != ampsUsed * volt) {
                    throw new IllegalStateException("The energy extracted is not equal to the energy inserted ");
                }
            }
        } else {
            IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, opposite);
            if (storage != null) {
                // if the energy is not enough for a full package, make it smaller (for batteries below the converters tier)
                long volt = voltage;
                if (ampsToInsert == 0) {
                    ampsToInsert = 1;
                    volt = extractable;
                }

                int inserted = storage.receiveEnergy((int) (Math.min(extractable, volt * ampsToInsert) * EU_TO_FE), false);
                if (removeEnergyInternal(inserted, false, false) != inserted) {
                    throw new IllegalStateException("The energy extracted is not equal to the energy inserted ");
                }
            }
        }
    }
    // -- GTCEu Energy--------------------------------------------

    public class EUContainer implements IEnergyContainer {

        @Override
        public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
            long inputAmps = getInputAmperage();
            if (feToEu || usedAmps >= inputAmps) return 0;
            long ampsUsed = 0;
            if (amperage <= 0 || voltage <= 0) {
                return 0;
            }
            if (side == null || inputsEnergy(side)) {
                if (voltage > getInputVoltage()) {
                    GTUtility.doOvervoltageExplosion(metaTileEntity, voltage);
                    return Math.min(amperage, inputAmps - usedAmps);
                }

                // first add to internal buffers
                long maxAmps = Math.min(inputAmps - usedAmps, amperage);
                long space = baseCapacity - storedEU;
                if (space > voltage) {
                    maxAmps = Math.min(maxAmps, space / voltage);
                    long energyToAdd = voltage * maxAmps;
                    storedEU += energyToAdd;
                    ampsUsed += maxAmps;
                    if (usedAmps + ampsUsed >= inputAmps) {
                        usedAmps += ampsUsed;
                        return ampsUsed;
                    }
                }

                // then to batteries
                IItemHandlerModifiable inventory = getInventory();
                for (int i = 0; i < inventory.getSlots(); i++) {
                    if (batterySlotsUsedThisTick.get(i)) continue;
                    ItemStack batteryStack = inventory.getStackInSlot(i);
                    IElectricItem electricItem = getBatteryContainer(batteryStack);
                    if (electricItem == null) continue;
                    if (electricItem.charge(voltage, tier, false, true) == voltage) {
                        electricItem.charge(voltage, tier, false, false);
                        inventory.setStackInSlot(i, batteryStack);
                        batterySlotsUsedThisTick.set(i);
                        if (++ampsUsed == maxAmps) break;
                    }
                }
            }
            usedAmps += ampsUsed;
            return ampsUsed;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return !feToEu && side != getFront();
        }

        @Override
        public long changeEnergy(long amount) {
            if (amount == 0)
                return 0;
            return amount > 0 ? addEnergyInternal(amount, true, false) : removeEnergyInternal(-amount, true, false);
        }

        @Override
        public long addEnergy(long energyToAdd) {
            return addEnergyInternal(energyToAdd, true, false);
        }

        @Override
        public long removeEnergy(long energyToRemove) {
            return removeEnergyInternal(energyToRemove, true, false);
        }

        @Override
        public long getEnergyStored() {
            return getEnergyStoredInternal();
        }

        @Override
        public long getEnergyCapacity() {
            long energyCapacity = 0L;
            IItemHandlerModifiable inventory = getInventory();
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack batteryStack = inventory.getStackInSlot(i);
                IElectricItem electricItem = getBatteryContainer(batteryStack);
                if (electricItem == null) continue;
                energyCapacity += electricItem.getMaxCharge();
            }
            return energyCapacity + baseCapacity;
        }

        @Override
        public long getInputAmperage() {
            long inputAmperage = 0L;
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
            if (!feToEu) return 0;
            int amount = (int) Math.min(voltage * energyEU.getInputAmperage() * EU_TO_FE - feAdded, maxReceive);
            int inserted = (int) addEnergyInternal(amount, false, simulate);
            feAdded += inserted;
            return inserted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int amount = (int) Math.min(voltage * energyEU.getInputAmperage() * EU_TO_FE, maxExtract);
            return (int) removeEnergyInternal(amount, false, simulate);
        }

        @Override
        public int getEnergyStored() {
            return (int) (getEnergyStoredInternal() * EU_TO_FE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) (energyEU.getEnergyCapacity() * EU_TO_FE);
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
