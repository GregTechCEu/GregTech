package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler.IEnergyChangeListener;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class EnergyContainerBatteryBuffer extends MTETrait implements IEnergyContainer {

    private final int tier;
    private long amps = 0;
    private long lastEnergyInputPerSec = 0;
    private long lastEnergyOutputPerSec = 0;
    private long energyInputPerSec = 0;
    private long energyOutputPerSec = 0;

    public EnergyContainerBatteryBuffer(MetaTileEntity metaTileEntity, int tier) {
        super(metaTileEntity);
        this.tier = tier;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (amps >= getInputAmperage())
            return 0;
        long canAccept = getEnergyCapacity() - getEnergyStored();
        if (canAccept < voltage)
            return 0;

        if (voltage > 0L && (side == null || inputsEnergy(side))) {
            if (voltage > getInputVoltage()) {
                GTUtility.doOvervoltageExplosion(metaTileEntity, voltage);
                return Math.min(amperage, getInputAmperage() - amps);
            }
            IItemHandlerModifiable inventory = getInventory();
            long nonFullBatteries = getInputAmperage();
            if (nonFullBatteries == 0)
                return 0;

            // distribute energy evenly
            long distributed = voltage * Math.min(amperage, nonFullBatteries);
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack batteryStack = inventory.getStackInSlot(i);
                IElectricItem electricItem = getBatteryContainer(batteryStack);
                // non batteries and full batteries are ignored
                if (electricItem == null || electricItem.getCharge() == electricItem.getMaxCharge())
                    continue;

                // if the potential distributed voltage cannot all fit,
                // decrease distributed voltage so it will not overfill the battery
                if (!chargeItemWithVoltage(electricItem, distributed, getTier(), true)) {
                    long space = electricItem.getMaxCharge() - electricItem.getCharge();
                    distributed -= space;
                }
            }
            // no energy can be distributed, so do nothing
            if (distributed <= 0)
                return 0;

            // distribute the charge to each battery
            distributed /= nonFullBatteries;

            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack batteryStack = inventory.getStackInSlot(i);
                IElectricItem electricItem = getBatteryContainer(batteryStack);
                // non batteries and full batteries are ignored
                if (electricItem == null || electricItem.getCharge() == electricItem.getMaxCharge())
                    continue;

                // fill every battery with the distributed voltage
                if (chargeItemWithVoltage(electricItem, distributed, getTier(), true)) {
                    chargeItemWithVoltage(electricItem, distributed, getTier(), false);
                    inventory.setStackInSlot(i, batteryStack);
                }
            }
            // not using getInputAmperage() so that the amperage is correctly drawn this tick
            return Math.min(amperage, nonFullBatteries);
        }
        return 0;
    }

    @Override
    public long getInputPerSec() {
        return lastEnergyInputPerSec;
    }

    @Override
    public long getOutputPerSec() {
        return lastEnergyOutputPerSec;
    }

    private static boolean chargeItemWithVoltage(@Nonnull IElectricItem electricItem, long voltage, int tier, boolean simulate) {
        long charged = electricItem.charge(voltage, tier, false, simulate);
        return charged > 0;
    }

    private static long chargeItem(IElectricItem electricItem, long amount, int tier, boolean discharge) {
        if (!discharge) {
            return electricItem.charge(amount, tier, false, false);
        } else {
            return electricItem.discharge(amount, tier, true, true, false);
        }
    }

    @Override
    public void update() {
        amps = 0;
        if (!metaTileEntity.getWorld().isRemote) {
            if (metaTileEntity.getOffsetTimer() % 20 == 0) {
                lastEnergyInputPerSec = energyInputPerSec;
                lastEnergyOutputPerSec = energyOutputPerSec;
                energyInputPerSec = 0;
                energyOutputPerSec = 0;
            }
            EnumFacing outFacing = metaTileEntity.getFrontFacing();
            TileEntity tileEntity = metaTileEntity.getWorld().getTileEntity(metaTileEntity.getPos().offset(outFacing));
            if (tileEntity == null) {
                return;
            }
            IEnergyContainer energyContainer = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, outFacing.getOpposite());
            if (energyContainer == null) {
                return;
            }

            if (getOutputAmperage() == 0)
                return;

            IItemHandlerModifiable inventory = getInventory();
            // distribute energy evenly
            long distributed = getOutputVoltage();
            if (distributed == 0)
                return;

            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack batteryStack = inventory.getStackInSlot(i);
                IElectricItem electricItem = getBatteryContainer(batteryStack);
                // non batteries and empty batteries are ignored
                if (electricItem == null || electricItem.getCharge() == 0)
                    continue;

                // if the potential distributed voltage cannot all drain,
                // decrease distributed voltage so it will not overdrain the battery
                if (electricItem.discharge(distributed, getTier(), true, true, true) != distributed) {
                    long space = distributed - electricItem.getCharge();
                    distributed -= space;
                }
            }
            // no energy can be distributed, so do nothing
            if (distributed <= 0)
                return;

            long amperageUsed = energyContainer.acceptEnergyFromNetwork(outFacing.getOpposite(), distributed / getOutputAmperage(), getOutputAmperage());
            if (amperageUsed <= 0)
                return;

            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack batteryStack = inventory.getStackInSlot(i);
                IElectricItem electricItem = getBatteryContainer(batteryStack);
                // non batteries and empty batteries are ignored
                if (electricItem == null || electricItem.getCharge() == 0)
                    continue;

                // drain every battery with the distributed voltage
                electricItem.discharge(distributed, getTier(), true, true, false);
                inventory.setStackInSlot(i, batteryStack);
            }
            energyOutputPerSec += distributed;
            notifyEnergyListener(false);
        }
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
        return energyCapacity;
    }

    @Override
    public long getEnergyStored() {
        long energyStored = 0L;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            energyStored += electricItem.getCharge();
        }
        return energyStored;
    }

    @Override
    public long getInputAmperage() {
        // input amperage is equal to the amount of non-full batteries
        int count = 0;
        for (int i = 0; i < getInventory().getSlots(); i++) {
            ItemStack batteryStack = getInventory().getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem != null && electricItem.getCharge() != electricItem.getMaxCharge())
                count++;
        }
        return count;
    }

    public IElectricItem getBatteryContainer(ItemStack itemStack) {
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null && getTier() >= electricItem.getTier() &&
                electricItem.canProvideChargeExternally())
            return electricItem;
        return null;
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        boolean isDischarge = energyToAdd < 0L;
        energyToAdd = Math.abs(energyToAdd);
        long initialEnergyToAdd = energyToAdd;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            long charged = chargeItem(electricItem, energyToAdd, getTier(), isDischarge);
            energyToAdd -= charged;
            if (energyToAdd == 0L) break;
        }
        long energyAdded = initialEnergyToAdd - energyToAdd;
        if (energyAdded > 0L) {
            notifyEnergyListener(false);
        }
        energyInputPerSec += energyAdded;
        return energyAdded;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        notifyEnergyListener(true);
    }

    public void notifyEnergyListener(boolean isInitialChange) {
        if (metaTileEntity instanceof IEnergyChangeListener) {
            ((IEnergyChangeListener) metaTileEntity).onEnergyChanged(this, isInitialChange);
        }
    }

    @Override
    public long getInputVoltage() {
        return GTValues.V[getTier()];
    }

    @Override
    public long getOutputVoltage() {
        return getInputVoltage();
    }

    @Override
    public long getOutputAmperage() {
        // output amperage is equal to the amount of non-empty batteries
        int count = 0;
        for (int i = 0; i < getInventory().getSlots(); i++) {
            ItemStack batteryStack = getInventory().getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem != null && electricItem.getCharge() != 0)
                count++;
        }
        return count;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return getMetaTileEntity().getFrontFacing() != side;
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return !inputsEnergy(side);
    }

    @Override
    public String getName() {
        return "BatteryEnergyContainer";
    }

    @Override
    public int getNetworkID() {
        return TraitNetworkIds.TRAIT_ID_ENERGY_CONTAINER;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this);
        }
        return null;
    }

    protected IItemHandlerModifiable getInventory() {
        return metaTileEntity.getImportItems();
    }

    protected int getTier() {
        return tier;
    }
}
