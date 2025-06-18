package gregtech.api.configurator;

import gregtech.api.configurator.profile.IMachineConfiguratorProfile;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public interface IConfiguratorInteractable {

    boolean isProfileValid(@NotNull IMachineConfiguratorProfile profile);

    void writeProfileData(@NotNull IMachineConfiguratorProfile configuratorProfile, @NotNull NBTTagCompound tag);

    void readProfileData(@NotNull IMachineConfiguratorProfile configuratorProfile, @NotNull NBTTagCompound tag);
}
