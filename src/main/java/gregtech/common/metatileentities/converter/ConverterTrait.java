package gregtech.common.metatileentities.converter;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.util.GTUtility;
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

    public static final float EU_TO_FE = 4;
    public static final float FE_TO_EU = 1 / EU_TO_FE;

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

    protected void invertMode() {
        feToEu = !feToEu;
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
        if (amount == 0)
            return 0;
        if (!isEu)
            amount *= FE_TO_EU;
        long inserted = 0;

        long change = Math.min(baseCapacity - storedEU, amount);
        if (!simulate)
            storedEU += change;

        inserted += change;
        amount -= change;

        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (amount == 0) break;
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            long ins = electricItem.charge(amount, tier, false, simulate);
            inserted += ins;
            amount -= ins;
        }

        return inserted;
    }

    private long removeEnergyInternal(long amount, boolean isEu, boolean simulate) {
        if (amount <= 0)
            return 0;
        if (!isEu)
            amount *= FE_TO_EU;
        long inserted = 0;

        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (amount == 0) break;
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            long ins = electricItem.discharge(amount, tier, false, true, simulate);
            inserted += ins;
            amount -= ins;
        }

        long change = Math.min(storedEU, amount);
        if (!simulate)
            storedEU -= change;

        inserted += change;

        return inserted;
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
        this.batterySlotsUsedThisTick.clear();
        if (getWorld().isRemote) return;
        TileEntity tile = getWorld().getTileEntity(getPos().offset(getFront()));
        if (tile == null) return;
        EnumFacing opposite = getFront().getOpposite();
        long extractable = removeEnergyInternal(energyEU.getEnergyCapacity(), true, true);
        long ampsToInsert = Math.min(energyEU.getInputAmperage(), getEnergyStoredInternal() / voltage);
        if(ampsToInsert * voltage > extractable) {
            ampsToInsert = extractable / voltage;
        }
        if (feToEu) {
            IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, opposite);
            if (container != null) {
                long ampsUsed = container.acceptEnergyFromNetwork(opposite, voltage, ampsToInsert);
                removeEnergyInternal(voltage * ampsUsed, true, false);
            }
        } else {
            IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, opposite);
            if (storage != null) {
                int inserted = storage.receiveEnergy((int) (Math.min(getEnergyStoredInternal(), voltage * ampsToInsert) * EU_TO_FE), false);
                removeEnergyInternal((long) (inserted * FE_TO_EU), true, false);
            }
        }
    }
    // -- GTCE Energy--------------------------------------------

    public class EUContainer implements IEnergyContainer {

        @Override
        public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
            long inputAmps = getInputAmperage();
            if (feToEu || usedAmps >= inputAmps) return 0;
            long ampsUsed = 0;
            if (amperage <= 0) {
                return 0;
            }
            if (side == null || inputsEnergy(side)) {
                if (voltage > getInputVoltage()) {
                    GTUtility.doOvervoltageExplosion(metaTileEntity, voltage);
                    return Math.min(amperage, inputAmps - usedAmps);
                }

                final long maxAmps = Math.min(inputAmps - usedAmps, amperage);
                long space = baseCapacity - storedEU;
                if(space > voltage) {
                    for(int i = 0; i < maxAmps; i++) {
                        space -= voltage;
                        storedEU += voltage;
                        if (++ampsUsed == maxAmps || space < voltage) break;
                    }
                    if (ampsUsed == maxAmps) {
                        usedAmps += ampsUsed;
                        return ampsUsed;
                    }
                }

                // first add to batteries
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
            return amount >= 0 ? addEnergyInternal(amount, true, false) : removeEnergyInternal(-amount, true, false);
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
            return feToEu ? 0 : voltage;
        }

        @Override
        public long getOutputAmperage() {
            return feToEu ? getInputAmperage() : 0;
        }

        @Override
        public long getOutputVoltage() {
            return feToEu ? voltage : 0;
        }
    }

    // -- GTCE Energy--------------------------------------------

    public class FEContainer implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!feToEu) return 0;
            return (int) (addEnergyInternal((long) Math.min(maxReceive * FE_TO_EU, energyEU.getEnergyCapacity() - getEnergyStoredInternal()), false, simulate) * EU_TO_FE);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return (int) (removeEnergyInternal((long) Math.min(maxExtract * FE_TO_EU, getEnergyStoredInternal()), true, simulate) * EU_TO_FE);
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
