package gregtech.api.capability.machineconfigurator;

import gregtech.api.mui.GTGuis;

import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ToggleButton;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SimpleMachineProfile implements IMachineConfiguratorProfile {

    public static final SimpleMachineProfile INSTANCE = new SimpleMachineProfile();

    private SimpleMachineProfile() {
        throw new RuntimeException("Use the INSTANCE var please!");
    }

    @Override
    public @NotNull String getName() {
        return "SimpleMachineProfile";
    }

    @NotNull
    @Override
    public ModularPanel createConfiguratorPanel(PanelSyncManager panelSyncManager, Supplier<NBTTagCompound> getConfig) {
        var panel = GTGuis.createPopupPanel("simple_machine_configurator", 50, 50);

        BooleanSyncValue autoOutputItems = new BooleanSyncValue(() -> getConfig.get().getBoolean("AutoOutputItems"),
                bool -> getConfig.get().setBoolean("AutoOutputItems", bool));
        BooleanSyncValue autoOutputFluids = new BooleanSyncValue(() -> getConfig.get().getBoolean("AutoOutputFluids"),
                bool -> getConfig.get().setBoolean("AutoOutputFluids", bool));

        panel.child(new ToggleButton()
                .value(autoOutputItems));
        panel.child(new ToggleButton()
                .value(autoOutputFluids)
                .pos(20, 0));

        return panel;
    }
}
