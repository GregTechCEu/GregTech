package gregtech.api.capability.copytool;

import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface IMachineConfiguratorProfile {

    @NotNull
    String getName();

    @NotNull
    ModularPanel createConfiguratorPanel(PanelSyncManager panelSyncManager, Supplier<NBTTagCompound> getConfig);
}
