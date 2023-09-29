package gregtech.api.capability.impl;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.GTValues.V;

public class LaserContainerHandler extends MTETrait implements ILaserContainer {
    private long buffer;
    private final long capacity;
    private final int tier;
    private final int amperage;

    /**
     * Create a new MTE trait.
     *
     * @param metaTileEntity the MTE to reference, and add the trait to
     */
    public LaserContainerHandler(@NotNull MetaTileEntity metaTileEntity, int tier, int amperage) {
        super(metaTileEntity);
        this.buffer = 0;
        this.capacity = V[tier] * amperage * 64L;
        this.tier = tier;
        this.amperage = amperage;
    }

    @NotNull
    @Override
    public String getName() {
        return "LaserContainer";
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_LASER) {
            return GregtechTileCapabilities.CAPABILITY_LASER.cast(this);
        }
        return null;
    }
    @Override
    public long changeEnergy(long amount) {
        if (amount > getMaxThroughput()) {
            amount = getMaxThroughput();
        }

        long oldStored = buffer;
        long newStored =  (capacity - oldStored < amount) ? capacity : (oldStored + amount);
        if (newStored < 0) {
            newStored = 0;
        }
        buffer = newStored;
        return newStored - oldStored;
    }

    @Override
    public long getEnergyStored() {
        return buffer;
    }

    @Override
    public long getEnergyCapacity() {
        return capacity;
    }

    @Override
    public long getMaxThroughput() {
        return V[tier] * amperage;
    }
}
