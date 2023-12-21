package gregtech.api.capability;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IQuantumController extends ICapabilityProvider {

    /**
     * Constructs the network upon placement
     */
    void rebuildNetwork();

    /**
     * Return whether this storage block can connect. Can be used to implement a maximum distance from controller for
     * example.
     */
    boolean canConnect(IQuantumStorage<?> storage);

    BlockPos getPos();

    IDualHandler getHandler();

    IEnergyContainer getEnergyContainer();

    boolean isPowered();
}
