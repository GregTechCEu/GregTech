package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class EnergyContainerList implements IEnergyContainer {

    private final List<IEnergyContainer> energyContainerList;

    public EnergyContainerList(List<IEnergyContainer> energyContainerList) {
        this.energyContainerList = energyContainerList;
    }

    @Override
    public long getInputPerSec() {
        long sum = 0;
        for (IEnergyContainer energyContainer : energyContainerList) {
            sum += energyContainer.getInputPerSec();
        }
        return sum;
    }

    @Override
    public long getOutputPerSec() {
        long sum = 0;
        for (IEnergyContainer energyContainer : energyContainerList) {
            sum += energyContainer.getOutputPerSec();
        }
        return sum;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        long amperesUsed = 0L;
        for (IEnergyContainer energyContainer : energyContainerList) {
            amperesUsed += energyContainer.acceptEnergyFromNetwork(null, voltage, amperage);
            if (amperage == amperesUsed) break;
        }
        return amperesUsed;
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        long energyAdded = 0L;
        for (IEnergyContainer energyContainer : energyContainerList) {
            energyAdded += energyContainer.changeEnergy(energyToAdd - energyAdded);
            if (energyAdded == energyToAdd) break;
        }
        return energyAdded;
    }

    @Override
    public long getEnergyStored() {
        return energyContainerList.stream()
                .mapToLong(IEnergyContainer::getEnergyStored)
                .sum();
    }

    @Override
    public long getEnergyCapacity() {
        return energyContainerList.stream()
                .mapToLong(IEnergyContainer::getEnergyCapacity)
                .sum();
    }

    @Override
    public long getInputAmperage() {
        return 1L;
    }

    @Override
    public long getOutputAmperage() {
        return 1L;
    }

    @Override
    public long getInputVoltage() {
        return energyContainerList.stream()
                .mapToLong(v -> v.getInputVoltage() * v.getInputAmperage())
                .sum();
    }

    @Override
    public long getOutputVoltage() {
        return energyContainerList.stream()
                .mapToLong(v -> v.getOutputVoltage() * v.getOutputAmperage())
                .sum();
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return true;
    }
}
