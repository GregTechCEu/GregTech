package gregtech.api.capability.copytool;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public interface IMachineConfiguratorInteractable {

    @NotNull
    IMachineConfiguratorProfile getProfile();

    @NotNull
    NBTTagCompound writeProfileData();

    void readProfileData(@NotNull NBTTagCompound profile);
}
