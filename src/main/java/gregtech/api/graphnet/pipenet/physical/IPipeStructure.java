package gregtech.api.graphnet.pipenet.physical;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public interface IPipeStructure extends IStringSerializable {
    float getRenderThickness();

    /**
     * Allows for controlling what sides can be connected to based on current connections,
     * such as in the case of optical and laser pipes.
     */
    default boolean canConnectTo(EnumFacing side, byte connectionMask) {
        return true;
    }
}
