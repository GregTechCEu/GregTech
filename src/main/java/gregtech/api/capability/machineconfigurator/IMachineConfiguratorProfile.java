package gregtech.api.capability.machineconfigurator;

import gregtech.api.util.function.NBTTagCompoundSupplier;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;

public interface IMachineConfiguratorProfile {

    @NotNull
    String getName();

    @NotNull
    ModularPanel createConfiguratorPanel(PanelSyncManager panelSyncManager, NBTTagCompoundSupplier nbt);
}
