package gregtech.api.capability.impl;

import gregtech.api.capability.IFuelInfo;

/**
 * Fuel information
 */
public abstract class AbstractFuelInfo implements IFuelInfo {

    private int fuelRemaining;
    private int fuelCapacity;
    private int fuelMinConsumed;
    private long fuelBurnTime;

    public AbstractFuelInfo(final int fuelRemaining, final int fuelCapacity, final int fuelMinConsumed, final int fuelBurnTime) {
        this(fuelRemaining, fuelCapacity, fuelMinConsumed, (long) fuelBurnTime);
    }

    public AbstractFuelInfo(final int fuelRemaining, final int fuelCapacity, final int fuelMinConsumed, final long fuelBurnTime) {
        this.fuelRemaining = fuelRemaining;
        this.fuelCapacity = fuelCapacity;
        this.fuelMinConsumed = fuelMinConsumed;
        this.fuelBurnTime = fuelBurnTime;
    }

    public int getFuelRemaining() {
        return fuelRemaining;
    }

    public int getFuelCapacity() {
        return fuelCapacity;
    }

    public int getFuelMinConsumed() {
        return fuelMinConsumed;
    }

    @Override
    public int getFuelBurnTime() {
        return this.fuelBurnTime > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) this.fuelBurnTime;
    }

    @Override
    public long getFuelBurnTimeLong() {
        return this.fuelBurnTime;
    }

    public void setFuelRemaining(int fuelRemaining) {
        this.fuelRemaining = fuelRemaining;
    }

    public void setFuelCapacity(int fuelCapacity) {
        this.fuelCapacity = fuelCapacity;
    }

    public void setMinConsumed(int fuelMinConsumed) {
        this.fuelMinConsumed = fuelMinConsumed;
    }

    @Deprecated
    public void setFuelBurnTime(int fuelBurnTime) {
        this.fuelBurnTime = fuelBurnTime;
    }

    public void setFuelBurnTime(final long fuelBurnTime) {
        this.fuelBurnTime = fuelBurnTime;
    }

    public void addFuelRemaining(int fuelRemaining) {
        this.fuelRemaining += fuelRemaining;
    }

    public void addFuelCapacity(int fuelCapacity) {
        this.fuelCapacity += fuelCapacity;
    }

    @Deprecated
    public void addFuelBurnTime(int fuelBurnTime) {
        this.fuelBurnTime += fuelBurnTime;
    }

    public void addFuelBurnTime(final long fuelBurnTime) {
        this.fuelBurnTime += fuelBurnTime;
    }
}
