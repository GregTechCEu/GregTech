package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.ILaserContainer;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.Nullable;

public class ActiveTransformerWrapper implements ILaserContainer {

    @Nullable
    private final IEnergyContainer energyInputs;
    @Nullable
    private final ILaserContainer laserUpstream;

    public ActiveTransformerWrapper(@Nullable IEnergyContainer energyInputs, @Nullable ILaserContainer laserUpstream) {
        this.energyInputs = energyInputs;
        this.laserUpstream = laserUpstream;
    }

    @Override
    public long acceptEnergy(EnumFacing side, long amount) {
        return changeEnergy(amount);
    }

    @Override
    public long changeEnergy(long amount) {
        long used = 0;
        if (energyInputs != null) {
            used = energyInputs.changeEnergy(amount);
        }
        if (Math.abs(used) < Math.abs(amount) && laserUpstream != null) {
            used += laserUpstream.changeEnergy(amount - used);
        }
        return used;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return true;
}

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public long getEnergyStored() {
        long stored = 0;
        if (energyInputs != null) {
            stored = energyInputs.getEnergyStored();
        }
        if (laserUpstream != null) {
            stored += laserUpstream.getEnergyStored();
        }
        return stored;
    }

    @Override
    public long getEnergyCapacity() {
        long capacity = 0;
        if (energyInputs != null) {
            capacity = energyInputs.getEnergyCapacity();
        }
        if (laserUpstream != null) {
            capacity += laserUpstream.getEnergyCapacity();
        }
        return capacity;
    }
}
