package gregtech.api.configurator.profile;

import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;

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
    IKey getProfileName();

    @NotNull
    ModularPanel createConfiguratorPanel(@NotNull PanelSyncManager panelSyncManager, @NotNull NBTTagCompound config);
}
