package gregtech.api.capability.impl;

import gregtech.api.capability.ILaserContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class LaserContainerHandler extends MTETrait implements ILaserContainer {

    private final long capacity;
    private long energyStored;
    private boolean isOutput;

    private Predicate<EnumFacing> sideInputCondition;
    private Predicate<EnumFacing> sideOutputCondition;

    /**
     * Create a new MTE trait.
     *
     * @param metaTileEntity the MTE to reference, and add the trait to
     */
    public LaserContainerHandler(@NotNull MetaTileEntity metaTileEntity, long capacity, boolean isOutput) {
        super(metaTileEntity);
        this.capacity = capacity;
    }

    public void setSideInputCondition(Predicate<EnumFacing> sideInputCondition) {
        this.sideInputCondition = sideInputCondition;
    }

    public void setSideOutputCondition(Predicate<EnumFacing> sideOutputCondition) {
        this.sideOutputCondition = sideOutputCondition;
    }

    @Override
    public long acceptEnergy(EnumFacing side, long amount) {
        if (amount > 0 && !isOutput && (side == null || inputsEnergy(side))) {
            return changeEnergy(amount);
        } else if (amount < 0 && isOutput && (side == null || outputsEnergy(side))) {
            return changeEnergy(amount);
        }
        return 0;
    }

    @Override
    public long changeEnergy(long amount) {
        if (amount > 0) {
            long space = capacity - energyStored;
            if (amount - space >= 0) {
                energyStored += amount;
                return amount;
            } else {
                energyStored += space;
                return space;
            }
        } else if (amount < 0) {
            if (energyStored + amount >= 0) {
                energyStored += amount;
                return amount;
            } else {
                long oldEnergyStored = energyStored;
                energyStored = 0;
                return oldEnergyStored;
            }
        }
        return 0;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return !outputsEnergy(side) && energyStored < capacity && (sideInputCondition == null || sideInputCondition.test(side));
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return energyStored > 0 && (sideOutputCondition == null || sideOutputCondition.test(side));
    }

    @Override
    public long getEnergyStored() {
        return energyStored;
    }

    @Override
    public long getEnergyCapacity() {
        return capacity;
    }

    @NotNull
    @Override
    public String getName() {
        return "LaserContainer";
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        return null;
    }

    @Nonnull
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("EnergyStored", energyStored);
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound compound) {
        this.energyStored = compound.getLong("EnergyStored");
    }
}
