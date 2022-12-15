package gregtech.api.capability;

import net.minecraft.util.math.BlockPos;

public interface IQuantumController {

    void rebuildNetwork();

    /** Return whether this storage block can connect. Can be used to implement a maximum distance from controller for example. */
    boolean canConnect(IQuantumStorage<?> storage);

    BlockPos getPos();
}
