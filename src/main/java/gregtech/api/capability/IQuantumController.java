package gregtech.api.capability;

import net.minecraft.util.math.BlockPos;

public interface IQuantumController {

    /**
     * Constructs the network upon placement
     */
    void rebuildNetwork();

    /** Return whether this storage block can connect. Can be used to implement a maximum distance from controller for example. */
    boolean canConnect(IQuantumStorage<?> storage);

    BlockPos getPos();
}
