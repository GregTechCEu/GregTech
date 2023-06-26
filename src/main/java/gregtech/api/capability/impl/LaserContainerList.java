package gregtech.api.capability.impl;

import gregtech.api.capability.ILaserContainer;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class LaserContainerList implements ILaserContainer {
    private final List<ILaserContainer> containerList;

    public LaserContainerList(List<ILaserContainer> containerList) {
        this.containerList = containerList;
    }

    @Override
    public long acceptEnergy(EnumFacing side, long amount) {
        long energyUsed = 0;
        for (ILaserContainer container : containerList) {
            energyUsed += container.acceptEnergy(null, amount - energyUsed);
            if (energyUsed == amount) {
                break;
            }
        }
        return energyUsed;
    }

    @Override
    public long changeEnergy(long amount) {
        long energyUsed = 0;
        for (ILaserContainer container : containerList) {
            energyUsed += container.acceptEnergy(null, amount - energyUsed);
            if (energyUsed == amount) {
                break;
            }
        }
        return energyUsed;
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
        long totalStored = 0;
        for (ILaserContainer container : containerList) {
            totalStored += container.getEnergyStored();
        }
        return totalStored;
    }

    @Override
    public long getEnergyCapacity() {
        long totalCapacity = 0;
        for (ILaserContainer container : containerList) {
            totalCapacity += container.getEnergyCapacity();
        }
        return totalCapacity;
    }
}
