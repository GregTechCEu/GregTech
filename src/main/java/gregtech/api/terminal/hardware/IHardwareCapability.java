package gregtech.api.terminal.hardware;

import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;

public interface IHardwareCapability {

    default boolean hasCapability(@NotNull Capability<?> capability) {
        return getCapability(capability) != null;
    }

    <T> T getCapability(@NotNull Capability<T> capability);
}
