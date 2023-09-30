package gregtech.api.capability.impl;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.GTValues.V;

public class LaserContainerHandler extends MTETrait implements ILaserContainer {
    private long buffer;
    private final long capacity;
    private final int tier;
    private final int amperage;
    private final boolean isOutput;
    private final long throughput;

    /**
     * Create a new MTE trait.
     *
     * @param metaTileEntity the MTE to reference, and add the trait to
     */
    public LaserContainerHandler(@NotNull MetaTileEntity metaTileEntity, int tier, int amperage, boolean isOutput) {
        super(metaTileEntity);
        this.buffer = 0;
        this.capacity = V[tier] * amperage * 64L;
        this.throughput = V[tier] * amperage;
        this.tier = tier;
        this.amperage = amperage;
        this.isOutput = isOutput;
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

    @Override
    public boolean isOutput() {
        return isOutput;
    }

    @Override
    public boolean isInput() {
        return !isOutput;
    }

    @Override
    public void update() {
        if (getMetaTileEntity().getWorld().isRemote || isInput()) {
            return;
        }
        EnumFacing oppositeSide = metaTileEntity.getFrontFacing().getOpposite();
        TileEntity te = metaTileEntity.getWorld().getTileEntity(metaTileEntity.getPos().offset(metaTileEntity.getFrontFacing()));
        if (te != null && te.hasCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide)) {
            ILaserContainer laserContainer = te.getCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide);
            if (laserContainer == null || !laserContainer.isInput()) return;

            removeEnergy(laserContainer.changeEnergy(Math.min(throughput, getEnergyStored())));
        }

    }
    @Override
    public String toString() {
        return "LaserContainerHandler{" +
                "buffer=" + buffer +
                ", capacity=" + capacity +
                ", tier=" + tier +
                ", amperage=" + amperage +
                ", isOutput=" + isOutput +
                '}';
    }

}
