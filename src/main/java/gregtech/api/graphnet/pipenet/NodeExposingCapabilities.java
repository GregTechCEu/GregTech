package gregtech.api.graphnet.pipenet;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface NodeExposingCapabilities {

    ICapabilityProvider getProvider();

    default EnumFacing exposedFacing() {
        return null;
    }
}
