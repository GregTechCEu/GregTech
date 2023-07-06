package gregtech.api.capability.impl;

import gregtech.api.capability.ILaserContainer;
import net.minecraft.util.EnumFacing;

public class ActiveTransformerBuffer implements ILaserContainer {
    private final long capacity;
    private long stored = 0L;

    public ActiveTransformerBuffer(long capacity) {
        this.capacity = capacity;
    }

    @Override
    public long acceptEnergy(EnumFacing side, long amount) {
        return changeEnergy(amount);
    }

    @Override
    public long changeEnergy(long amount) {
        if (amount > capacity - stored) {
            amount = capacity - stored;
        } else if (-amount > stored) {
            amount = -stored;
        }
        stored += amount;
        return amount;
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
        return stored;
    }

    @Override
    public long getEnergyCapacity() {
        return capacity;
    }
}
