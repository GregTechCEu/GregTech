package gregtech.api.configurator.profile;

import gregtech.api.configurator.playerdata.PlayerConfiguratorData;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface IMachineConfiguratorProfile {

    /**
     * The internal name of this profile.
     */
    @NotNull
    String getName();

    /**
     * Get the network ID of this profile.
     */
    int networkID();

    /**
     * Set the network ID of this profile. Will be called when registering in
     * {@link ConfiguratorProfileRegistry#registerMachineConfiguratorProfile(IMachineConfiguratorProfile)}
     * 
     * @param networkID the network ID to be assigned
     */
    void setNetworkID(int networkID);

    /**
     * The name of this profile as an {@link IKey}
     */
    @NotNull
    IKey getProfileName();

    @NotNull
    ModularPanel createConfiguratorPanel(@NotNull PanelSyncManager panelSyncManager,
                                         @NotNull PlayerConfiguratorData playerData,
                                         @NotNull Supplier<@NotNull String> selectedSlot);
}
