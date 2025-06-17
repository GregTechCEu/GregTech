package gregtech.api.configurator;

import gregtech.api.configurator.profile.IMachineConfiguratorProfile;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IMachineConfiguratorInteractable {

    @NotNull
    Set<@NotNull IMachineConfiguratorProfile> getProfiles();

    @NotNull
    NBTTagCompound writeProfileData(@NotNull IMachineConfiguratorProfile configuratorProfile);

    void readProfileData(@NotNull IMachineConfiguratorProfile configuratorProfile, @NotNull NBTTagCompound profile);
}
