package gregtech.api.terminal.hardware;

import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public interface IHardwareCapability {
    default boolean hasCapability(@Nonnull Capability<?> capability) {
        return getCapability(capability) != null;
    }

    <T> T getCapability(@Nonnull Capability<T> capability);
}
