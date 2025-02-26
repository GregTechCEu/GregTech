package gregtech.api.graphnet.pipenet;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;

public interface NodeExposingCapabilities {

    @NotNull
    ICapabilityProvider getProvider();

    default EnumFacing exposedFacing() {
        return null;
    }
}
