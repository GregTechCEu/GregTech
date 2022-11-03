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
        List<IEnergyContainer> energyContainerList = this.energyContainerList;
        for (int i = 0; i < energyContainerList.size(); i++) {
            sum += energyContainerList.get(i).getInputPerSec();
        }
        return sum;
    }

    @Override
    public long getOutputPerSec() {
        long sum = 0;
        List<IEnergyContainer> energyContainerList = this.energyContainerList;
        for (int i = 0; i < energyContainerList.size(); i++) {
            sum += energyContainerList.get(i).getOutputPerSec();
        }
        return sum;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        long amperesUsed = 0L;
        List<IEnergyContainer> energyContainerList = this.energyContainerList;
        for (int i = 0; i < energyContainerList.size(); i++) {
            amperesUsed += energyContainerList.get(i).acceptEnergyFromNetwork(null, voltage, amperage);
            if (amperage == amperesUsed) {
                return amperesUsed;
            }
        }
        return amperesUsed;
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        long energyAdded = 0L;
        List<IEnergyContainer> energyContainerList = this.energyContainerList;
        for (int i = 0; i < energyContainerList.size(); i++) {
            energyAdded += energyContainerList.get(i).changeEnergy(energyToAdd - energyAdded);
            if (energyAdded == energyToAdd) {
                return energyAdded;
            }
        }
        return energyAdded;
    }

    @Override
    public long getEnergyStored() {
        long energyStored = 0L;
        for (int i = 0; i < energyContainerList.size(); i++) {
            energyStored += energyContainerList.get(i).getEnergyStored();
        }
        return energyStored;
    }

    @Override
    public long getEnergyCapacity() {
        long energyCapacity = 0L;
        for (int i = 0; i < energyContainerList.size(); i++) {
            energyCapacity += energyContainerList.get(i).getEnergyCapacity();
        }
        return energyCapacity;
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
        long inputVoltage = 0L;
        for (int i = 0; i < energyContainerList.size(); i++) {
            IEnergyContainer container = energyContainerList.get(i);
            inputVoltage += container.getInputVoltage() * container.getInputAmperage();
        }
        return inputVoltage;
    }

    @Override
    public long getOutputVoltage() {
        long outputVoltage = 0L;
        for (int i = 0; i < energyContainerList.size(); i++) {
            IEnergyContainer container = energyContainerList.get(i);
            outputVoltage += container.getOutputVoltage() * container.getOutputAmperage();
        }
        return outputVoltage;
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
