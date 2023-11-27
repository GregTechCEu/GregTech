package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.*;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class EnergyContainerHandler extends MTETrait implements IEnergyContainer {

    protected final long maxCapacity;
    protected long energyStored;

    private final long maxInputVoltage;
    private final long maxInputAmperage;

    private final long maxOutputVoltage;
    private final long maxOutputAmperage;

    protected long lastEnergyInputPerSec = 0;
    protected long lastEnergyOutputPerSec = 0;
    protected long energyInputPerSec = 0;
    protected long energyOutputPerSec = 0;

    private Predicate<EnumFacing> sideInputCondition;
    private Predicate<EnumFacing> sideOutputCondition;

    protected long amps = 0;

    public EnergyContainerHandler(MetaTileEntity tileEntity, long maxCapacity, long maxInputVoltage,
                                  long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
        super(tileEntity);
        this.maxCapacity = maxCapacity;
        this.maxInputVoltage = maxInputVoltage;
        this.maxInputAmperage = maxInputAmperage;
        this.maxOutputVoltage = maxOutputVoltage;
        this.maxOutputAmperage = maxOutputAmperage;
    }

    public void setSideInputCondition(Predicate<EnumFacing> sideInputCondition) {
        this.sideInputCondition = sideInputCondition;
    }

    public void setSideOutputCondition(Predicate<EnumFacing> sideOutputCondition) {
        this.sideOutputCondition = sideOutputCondition;
    }

    public static EnergyContainerHandler emitterContainer(MetaTileEntity tileEntity, long maxCapacity,
                                                          long maxOutputVoltage, long maxOutputAmperage) {
        return new EnergyContainerHandler(tileEntity, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static EnergyContainerHandler receiverContainer(MetaTileEntity tileEntity, long maxCapacity,
                                                           long maxInputVoltage, long maxInputAmperage) {
        return new EnergyContainerHandler(tileEntity, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    @Override
    public long getInputPerSec() {
        return lastEnergyInputPerSec;
    }

    @Override
    public long getOutputPerSec() {
        return lastEnergyOutputPerSec;
    }

    @NotNull
    @Override
    public String getName() {
        return GregtechDataCodes.ENERGY_CONTAINER_TRAIT;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this);
        }
        return null;
    }

    @NotNull
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("EnergyStored", energyStored);
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        this.energyStored = compound.getLong("EnergyStored");
        notifyEnergyListener(true);
    }

    @Override
    public long getEnergyStored() {
        return this.energyStored;
    }

    public void setEnergyStored(long energyStored) {
        if (energyStored > this.energyStored) {
            energyInputPerSec += energyStored - this.energyStored;
        } else {
            energyOutputPerSec += this.energyStored - energyStored;
        }
        this.energyStored = energyStored;
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            notifyEnergyListener(false);
        }
    }

    protected void notifyEnergyListener(boolean isInitialChange) {
        if (metaTileEntity instanceof IEnergyChangeListener) {
            ((IEnergyChangeListener) metaTileEntity).onEnergyChanged(this, isInitialChange);
        }
    }

    public boolean dischargeOrRechargeEnergyContainers(IItemHandlerModifiable itemHandler, int slotIndex) {
        ItemStack stackInSlot = itemHandler.getStackInSlot(slotIndex);
        if (stackInSlot.isEmpty()) { // no stack to charge/discharge
            return false;
        }

        IElectricItem electricItem = stackInSlot.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            return handleElectricItem(electricItem);
        } else if (ConfigHolder.compat.energy.nativeEUToFE) {
            IEnergyStorage energyStorage = stackInSlot.getCapability(CapabilityEnergy.ENERGY, null);
            if (energyStorage != null) {
                return handleForgeEnergyItem(energyStorage);
            }
        }
        return false;
    }

    private boolean handleElectricItem(IElectricItem electricItem) {
        int machineTier = GTUtility.getTierByVoltage(Math.max(getInputVoltage(), getOutputVoltage()));
        int chargeTier = Math.min(machineTier, electricItem.getTier());
        double chargePercent = getEnergyStored() / (getEnergyCapacity() * 1.0);

        // Check if the item is a battery (or similar), and if we can receive some amount of energy
        if (electricItem.canProvideChargeExternally() && getEnergyCanBeInserted() > 0) {

            // Drain from the battery if we are below half energy capacity, and if the tier matches
            if (chargePercent <= 0.5 && chargeTier == machineTier) {
                long dischargedBy = electricItem.discharge(getEnergyCanBeInserted(), machineTier, false, true, false);
                addEnergy(dischargedBy);
                return dischargedBy > 0L;
            }
        }

        // Else, check if we have above 50% power
        if (chargePercent > 0.5) {
            long chargedBy = electricItem.charge(getEnergyStored(), chargeTier, false, false);
            removeEnergy(chargedBy);
            return chargedBy > 0;
        }
        return false;
    }

    private boolean handleForgeEnergyItem(IEnergyStorage energyStorage) {
        int machineTier = GTUtility.getTierByVoltage(Math.max(getInputVoltage(), getOutputVoltage()));
        double chargePercent = getEnergyStored() / (getEnergyCapacity() * 1.0);

        if (chargePercent > 0.5) {
            long chargedBy = FeCompat.insertEu(energyStorage, GTValues.V[machineTier]);
            removeEnergy(chargedBy);
            return chargedBy > 0;
        }
        return false;
    }

    @Override
    public void update() {
        amps = 0;
        if (getMetaTileEntity().getWorld().isRemote)
            return;
        if (metaTileEntity.getOffsetTimer() % 20 == 0) {
            lastEnergyOutputPerSec = energyOutputPerSec;
            lastEnergyInputPerSec = energyInputPerSec;
            energyOutputPerSec = 0;
            energyInputPerSec = 0;
        }
        if (getEnergyStored() >= getOutputVoltage() && getOutputVoltage() > 0 && getOutputAmperage() > 0) {
            long outputVoltage = getOutputVoltage();
            long outputAmperes = Math.min(getEnergyStored() / outputVoltage, getOutputAmperage());
            if (outputAmperes == 0) return;
            long amperesUsed = 0;
            for (EnumFacing side : EnumFacing.VALUES) {
                if (!outputsEnergy(side)) continue;
                TileEntity tileEntity = metaTileEntity.getNeighbor(side);
                EnumFacing oppositeSide = side.getOpposite();
                if (tileEntity != null &&
                        tileEntity.hasCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide)) {
                    IEnergyContainer energyContainer = tileEntity
                            .getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide);
                    if (energyContainer == null || !energyContainer.inputsEnergy(oppositeSide)) continue;
                    amperesUsed += energyContainer.acceptEnergyFromNetwork(oppositeSide, outputVoltage,
                            outputAmperes - amperesUsed);
                    if (amperesUsed == outputAmperes) break;
                }
            }
            if (amperesUsed > 0) {
                setEnergyStored(getEnergyStored() - amperesUsed * outputVoltage);
            }
        }
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (amps >= getInputAmperage()) return 0;
        long canAccept = getEnergyCapacity() - getEnergyStored();
        if (voltage > 0L && (side == null || inputsEnergy(side))) {
            if (voltage > getInputVoltage()) {
                metaTileEntity.doExplosion(GTUtility.getExplosionPower(voltage));
                return Math.min(amperage, getInputAmperage() - amps);
            }
            if (canAccept >= voltage) {
                long amperesAccepted = Math.min(canAccept / voltage, Math.min(amperage, getInputAmperage() - amps));
                if (amperesAccepted > 0) {
                    setEnergyStored(getEnergyStored() + voltage * amperesAccepted);
                    amps += amperesAccepted;
                    return amperesAccepted;
                }
            }
        }
        return 0;
    }

    @Override
    public long getEnergyCapacity() {
        return this.maxCapacity;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return !outputsEnergy(side) && getInputVoltage() > 0 &&
                (sideInputCondition == null || sideInputCondition.test(side));
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return getOutputVoltage() > 0 && (sideOutputCondition == null || sideOutputCondition.test(side));
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        long oldEnergyStored = getEnergyStored();
        long newEnergyStored = (maxCapacity - oldEnergyStored < energyToAdd) ? maxCapacity :
                (oldEnergyStored + energyToAdd);
        if (newEnergyStored < 0)
            newEnergyStored = 0;
        setEnergyStored(newEnergyStored);
        return newEnergyStored - oldEnergyStored;
    }

    @Override
    public long getOutputVoltage() {
        return this.maxOutputVoltage;
    }

    @Override
    public long getOutputAmperage() {
        return this.maxOutputAmperage;
    }

    @Override
    public long getInputAmperage() {
        return this.maxInputAmperage;
    }

    @Override
    public long getInputVoltage() {
        return this.maxInputVoltage;
    }

    public interface IEnergyChangeListener {

        void onEnergyChanged(IEnergyContainer container, boolean isInitialChange);
    }

    @Override
    public String toString() {
        return "EnergyContainerHandler{" +
                "maxCapacity=" + maxCapacity +
                ", energyStored=" + energyStored +
                ", maxInputVoltage=" + maxInputVoltage +
                ", maxInputAmperage=" + maxInputAmperage +
                ", maxOutputVoltage=" + maxOutputVoltage +
                ", maxOutputAmperage=" + maxOutputAmperage +
                ", amps=" + amps +
                '}';
    }
}
