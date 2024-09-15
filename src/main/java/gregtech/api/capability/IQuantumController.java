package gregtech.api.capability;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

// ICapabilityProvider is needed because getCapability is called in the quantum proxy against this interface
public interface IQuantumController extends ICapabilityProvider {

    /**
     * Constructs the network upon placement
     * <br />
     * Ideally only called once on world load, use {@link #addStorage(IQuantumStorage)} or
     * {@link #removeStorage(BlockPos)} to handle adding/removing quantum storages
     */
    void rebuildNetwork();

    /**
     * @param storage storage to add to the Quantum Controller
     */
    void addStorage(IQuantumStorage<?> storage);

    /**
     * @param pos storage's BlockPos to remove from the Quantum Controller
     */
    void removeStorage(BlockPos pos);

    /**
     * @param storage storage to remove from the Quantum Controller
     */
    default void removeStorage(IQuantumStorage<?> storage) {
        removeStorage(storage.getPos());
    }

    /**
     * Return whether this storage block can connect. Can be used to implement a maximum distance from controller for
     * example.
     */
    boolean canConnect(IQuantumStorage<?> storage);

    BlockPos getPos();

    IDualHandler getHandler();

    IEnergyContainer getEnergyContainer();

    boolean isPowered();

    long getEnergyUsage();
}
