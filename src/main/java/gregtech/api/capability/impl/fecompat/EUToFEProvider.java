package gregtech.api.capability.impl.fecompat;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.common.ConfigHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.ReentrantLock;

public class EUToFEProvider implements ICapabilityProvider {

    private final TileEntity tileEntity;
    private GTEnergyWrapper wrapper;

    /**
     * Lock used for concurrency protection between hasCapability and getCapability.
     */
    ReentrantLock lock = new ReentrantLock();

    public EUToFEProvider(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {

        if (!ConfigHolder.compat.energy.nativeEUToFE)
            return false;

        if (lock.isLocked() || capability != GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER)
            return false;

        // Wrap FE Machines with a GTEU EnergyContainer
        if (wrapper == null) wrapper = new GTEnergyWrapper(tileEntity);

        lock.lock();
        try {
            return wrapper.isValid(facing);
        } finally {
            lock.unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {

        if (!ConfigHolder.compat.energy.nativeEUToFE)
            return null;

        if (lock.isLocked() || capability != GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER)
            return null;

        if (wrapper == null) wrapper = new GTEnergyWrapper(tileEntity);

        lock.lock();
        try {
            return wrapper.isValid(facing) ? (T) wrapper : null;
        } finally {
            lock.unlock();
        }
    }
}
