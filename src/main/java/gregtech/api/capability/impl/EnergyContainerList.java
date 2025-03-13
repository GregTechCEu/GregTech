package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnergyContainerList implements IEnergyContainer {

    private final List<IEnergyContainer> energyContainerList;
    private final long inputVoltage;
    private final long outputVoltage;
    private final long inputAmperage;
    private final long outputAmperage;

    /** The highest single energy container's input voltage in the list. */
    private final long highestInputVoltage;
    /** The number of energy containers at the highest input voltage in the list. */
    private final int numHighestInputContainers;

    public EnergyContainerList(@NotNull List<IEnergyContainer> energyContainerList) {
        this.energyContainerList = energyContainerList;
        long highestInputVoltage = 0;
        long highestOutputVoltage = 0;
        long highestInputVoltageCumulativeAmperage = 0;
        long highestOutputVoltageCumulativeAmperage = 0;
        int numHighestInputContainers = 0;
        for (IEnergyContainer container : energyContainerList) {
            if (container.getInputVoltage() > highestInputVoltage) {
                highestInputVoltage = container.getInputVoltage();
                highestInputVoltageCumulativeAmperage = container.getInputAmperage();
                numHighestInputContainers = 0;

            } else if (container.getInputVoltage() == highestInputVoltage) {
                highestInputVoltageCumulativeAmperage += container.getInputAmperage();
                numHighestInputContainers++;
            }
            if (container.getOutputVoltage() > highestOutputVoltage) {
                highestOutputVoltage = container.getOutputVoltage();
                highestOutputVoltageCumulativeAmperage = container.getOutputAmperage();

            } else if (container.getOutputVoltage() == highestOutputVoltage) {
                highestOutputVoltageCumulativeAmperage += container.getOutputAmperage();
            }
        }
        this.inputVoltage = highestInputVoltage;
        this.inputAmperage = highestInputVoltageCumulativeAmperage;
        this.outputVoltage = highestOutputVoltage;
        this.outputAmperage = highestOutputVoltageCumulativeAmperage;
        this.highestInputVoltage = highestInputVoltage;
        this.numHighestInputContainers = numHighestInputContainers;
    }

    @Override
    public long getInputPerSec() {
        long sum = 0;
        List<IEnergyContainer> energyContainerList = this.energyContainerList;
        for (IEnergyContainer iEnergyContainer : energyContainerList) {
            sum += iEnergyContainer.getInputPerSec();
        }
        return sum;
    }

    @Override
    public long getOutputPerSec() {
        long sum = 0;
        List<IEnergyContainer> energyContainerList = this.energyContainerList;
        for (IEnergyContainer iEnergyContainer : energyContainerList) {
            sum += iEnergyContainer.getOutputPerSec();
        }
        return sum;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        long amperesUsed = 0L;
        List<IEnergyContainer> energyContainerList = this.energyContainerList;
        for (IEnergyContainer iEnergyContainer : energyContainerList) {
            amperesUsed += iEnergyContainer.acceptEnergyFromNetwork(null, voltage, amperage);
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

        for (IEnergyContainer iEnergyContainer : energyContainerList) {
            energyAdded += iEnergyContainer.changeEnergy(energyToAdd - energyAdded);
            if (energyAdded == energyToAdd) {
                return energyAdded;
            }
        }
        return energyAdded;
    }

    @Override
    public long getEnergyStored() {
        long energyStored = 0L;
        for (IEnergyContainer iEnergyContainer : energyContainerList) {
            energyStored += iEnergyContainer.getEnergyStored();
        }
        return energyStored;
    }

    @Override
    public long getEnergyCapacity() {
        long energyCapacity = 0L;
        for (IEnergyContainer iEnergyContainer : energyContainerList) {
            energyCapacity += iEnergyContainer.getEnergyCapacity();
        }
        return energyCapacity;
    }

    /** The highest single voltage of an energy container in this list. */
    public long getHighestInputVoltage() {
        return highestInputVoltage;
    }

    /**
     * The number of parts with voltage specified in {@link EnergyContainerList#getHighestInputVoltage()} in this list.
     */
    public int getNumHighestInputContainers() {
        return numHighestInputContainers;
    }

    /**
     * Always < 4. A list with amps > 4 will always be compacted into more voltage at fewer amps.
     *
     * @return maximum amount of receivable energy packets per tick
     */
    @Override
    public long getInputAmperage() {
        return this.inputAmperage;
    }

    /**
     * Always < 4. A list with amps > 4 will always be compacted into more voltage at fewer amps.
     *
     * @return maximum amount of output-able energy packets per tick
     */
    @Override
    public long getOutputAmperage() {
        return this.outputAmperage;
    }

    @Override
    public long getInputVoltage() {
        return this.inputVoltage;
    }

    @Override
    public long getOutputVoltage() {
        return this.outputVoltage;
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
    public String toString() {
        return "EnergyContainerList{" +
                "energyContainerList=" + energyContainerList +
                ", energyStored=" + getEnergyStored() +
                ", energyCapacity=" + getEnergyCapacity() +
                ", inputVoltage=" + inputVoltage +
                ", inputAmperage=" + inputAmperage +
                ", outputVoltage=" + outputVoltage +
                ", outputAmperage=" + outputAmperage +
                '}';
    }
}
