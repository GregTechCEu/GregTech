package gregtech.api.capability.impl.fecompat;

import gregtech.api.capability.GregtechCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.ReentrantLock;

public class EUToFEItemProvider implements ICapabilityProvider {

    private final ItemStack itemStack;
    private GTEnergyItemWrapper wrapper;

    /**
     * Lock used for concurrency protection between hasCapability and getCapability.
     */
    ReentrantLock lock = new ReentrantLock();

    public EUToFEItemProvider(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {

        if (!ForgeEnergyCompat.nativeEUtoFE())
            return false;

        if (lock.isLocked() || (capability != CapabilityEnergy.ENERGY && capability != GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM))
            return false;

        // Wrap FE Items with a GTEU EnergyContainer
        if (wrapper == null) wrapper = new GTEnergyItemWrapper(itemStack);

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

        if (!ForgeEnergyCompat.nativeEUtoFE())
            return null;

        if (lock.isLocked() || !hasCapability(capability, facing))
            return null;

        if (wrapper == null) wrapper = new GTEnergyItemWrapper(itemStack);

        lock.lock();
        try {
            return wrapper.isValid(facing) ? (T) wrapper : null;
        } finally {
            lock.unlock();
        }
    }
}
