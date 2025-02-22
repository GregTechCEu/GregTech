package gregtech.api.configurator.profile;

import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;

public interface IMachineConfiguratorProfile {

    @NotNull
    String getName();

    @NotNull
    ModularPanel createConfiguratorPanel(PanelSyncManager panelSyncManager, NBTTagCompound nbt);
}
