package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.common.pipelike.laser.tile.LaserContainer;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class laserContainerList implements  LaserContainer {
    private List<LaserContainer> laserContainerList;


    public laserContainerList(List<LaserContainer> energyContainerList) {
        this.laserContainerList = energyContainerList;
    }

    @Override
    public long acceptLaserFromNetwork(EnumFacing side, long voltage, long amperage) {
        long amperesUsed = 0L;
        for (LaserContainer energyContainer : laserContainerList) {
            amperesUsed += energyContainer.acceptLaserFromNetwork(null, voltage, amperage);
            if (amperage == amperesUsed) break;
        }
        return amperesUsed;
    }
    @Override
    public long changeLaser(long energyToAdd) {
        long energyAdded = 0L;
        for (LaserContainer energyContainer : laserContainerList) {
            energyAdded += energyContainer.changeLaser(energyToAdd - energyAdded);
            if (energyAdded == energyToAdd) break;
        }
        return energyAdded;
    }

    @Override
    public long getLaserStored() {
        return laserContainerList.stream()
                .mapToLong(LaserContainer::getLaserStored)
                .sum();
    }
    @Override
    public long getLaserCapacity() {
        return laserContainerList.stream()
                .mapToLong(LaserContainer::getLaserCapacity)
                .sum();
    }
    @Override
    public long getInputParallel() {
        return 1L;
    }

    @Override
    public long getOutputParallel() {
        return 1L;
    }

    @Override
    public long getInputLaser() {
        return laserContainerList.stream()
                .mapToLong(v -> v.getInputLaser() * v.getInputParallel())
                .sum();
    }

    @Override
    public long getOutputLaser() {
        return laserContainerList.stream()
                .mapToLong(v -> v.getOutputLaser() * v.getOutputParallel())
                .sum();
    }

    @Override
    public boolean inputsLaser(EnumFacing side) {
        return true;
    }

    @Override
    public boolean outputsLaser(EnumFacing side) {
        return true;
    }
}
