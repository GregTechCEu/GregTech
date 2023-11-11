package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
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

    public EnergyContainerList(@Nonnull List<IEnergyContainer> energyContainerList) {
        this.energyContainerList = energyContainerList;
        long totalInputVoltage = 0;
        long totalOutputVoltage = 0;
        long inputAmperage = 0;
        long outputAmperage = 0;
        long highestInputVoltage = 0;
        int numHighestInputContainers = 0;
        for (IEnergyContainer container : energyContainerList) {
            totalInputVoltage += container.getInputVoltage() * container.getInputAmperage();
            totalOutputVoltage += container.getOutputVoltage() * container.getOutputAmperage();
            inputAmperage += container.getInputAmperage();
            outputAmperage += container.getOutputAmperage();
            if (container.getInputVoltage() > highestInputVoltage) {
                highestInputVoltage = container.getInputVoltage();
            }
        }
        for (IEnergyContainer container : energyContainerList) {
            if (container.getInputVoltage() == highestInputVoltage) {
                numHighestInputContainers++;
            }
        }

        long[] voltageAmperage = calculateVoltageAmperage(totalInputVoltage, inputAmperage);
        this.inputVoltage = voltageAmperage[0];
        this.inputAmperage = voltageAmperage[1];
        voltageAmperage = calculateVoltageAmperage(totalOutputVoltage, outputAmperage);
        this.outputVoltage = voltageAmperage[0];
        this.outputAmperage = voltageAmperage[1];
        this.highestInputVoltage = highestInputVoltage;
        this.numHighestInputContainers = numHighestInputContainers;
    }

    /**
     * Computes the correct max voltage and amperage values
     *
     * @param voltage the sum of voltage * amperage for each hatch
     * @param amperage the total amperage of all hatches
     *
     * @return [newVoltage, newAmperage]
     */
    @Nonnull
    private static long[] calculateVoltageAmperage(long voltage, long amperage) {
        if (voltage > 1 && amperage > 1) {
            // don't operate if there is no voltage or no amperage
            if (hasPrimeFactorGreaterThanTwo(amperage)) {
                // scenarios like 3A, 5A, 6A, etc.
                // treated as 1A of the sum of voltage * amperage for each hatch
                amperage = 1;
            } else if (isPowerOfFour(amperage)) {
                // scenarios like 4A, 16A, etc.
                // treated as 1A of the sum of voltage * amperage for each hatch
                amperage = 1;
            } else if (amperage % 4 == 0) {
                // scenarios like 8A, 32A, etc.
                // reduced to an amperage < 4 and equivalent voltage for the new amperage
                while (amperage > 4) {
                    amperage /= 4;
                }
                voltage /= amperage;
            } else if (amperage == 2) {
                // exactly 2A, all other cases covered by earlier checks
                // reduced to the voltage per amp
                voltage /= amperage;
            } else {
                // fallback case, that should never be hit
                // forced to 1A to prevent excess power draw/output if something falls through
                amperage = 1;
            }
        }
        return new long[]{voltage, amperage};
    }

    private static boolean hasPrimeFactorGreaterThanTwo(long l) {
        int i = 2;
        final long max = l / 2;
        while (i <= max) {
            if (l % i == 0) {
                if (i > 2) return true;
                l /= i;
            } else {
                i++;
            }
        }
        return false;
    }

    /**
     * Checks if a number is a power of 4. Does not include 1, despite it being 4**0.
     */
    private static boolean isPowerOfFour(long l) {
        if (l == 0) return false;
        if ((l & (l - 1)) != 0) return false;
        return (l & 0x55555555) != 0;
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

    /** The number of parts with voltage specified in {@link EnergyContainerList#getHighestInputVoltage()} in this list. */
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
