package gregtech.api.capability.machineconfigurator;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public interface IMachineConfiguratorInteractable {

    @NotNull
    IMachineConfiguratorProfile getProfile();

    @NotNull
    NBTTagCompound writeProfileData();

    void readProfileData(@NotNull NBTTagCompound profile);
}
