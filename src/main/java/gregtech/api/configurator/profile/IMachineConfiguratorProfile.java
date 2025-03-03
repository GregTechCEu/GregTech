package gregtech.api.configurator.profile;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface IMachineConfiguratorProfile {

    /**
     * The internal name of this profile.
     */
    @NotNull
    String getName();

    /**
     * The name to use when translating this profile's name for the player to see.
     */
    @NotNull
    String getTranslationKey();

    @NotNull
    ModularPanel createConfiguratorPanel(PanelSyncManager panelSyncManager, UUID playerID);
}
