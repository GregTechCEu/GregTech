package gregtech.api.capability.machineconfigurator;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.function.NBTTagCompoundSupplier;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ToggleButton;
import org.jetbrains.annotations.NotNull;

public class SimpleMachineProfile implements IMachineConfiguratorProfile {

    public static final SimpleMachineProfile INSTANCE = new SimpleMachineProfile();

    private SimpleMachineProfile() {}

    @Override
    public @NotNull String getName() {
        return "SimpleMachineProfile";
    }

    @NotNull
    @Override
    public ModularPanel createConfiguratorPanel(PanelSyncManager panelSyncManager, NBTTagCompoundSupplier getConfig) {
        var panel = GTGuis.createPopupPanel("simple_machine_configurator", 75, 150);

        BooleanSyncValue autoOutputItems = new BooleanSyncValue(() -> getConfig.getBoolean("AutoOutputItems"),
                bool -> getConfig.setBoolean("AutoOutputItems", bool));
        BooleanSyncValue autoOutputFluids = new BooleanSyncValue(() -> getConfig.getBoolean("AutoOutputFluids"),
                bool -> getConfig.setBoolean("AutoOutputFluids", bool));

        BooleanSyncValue allowItemInputFromOutput = new BooleanSyncValue(
                () -> getConfig.getBoolean("AllowItemInputFromOutput"),
                bool -> getConfig.setBoolean("AllowFluidInputFromOutput", bool));
        BooleanSyncValue allowFluidInputFromOutput = new BooleanSyncValue(
                () -> getConfig.getBoolean("AllowItemInputFromOutput"),
                bool -> getConfig.setBoolean("AllowFluidInputFromOutput", bool));

        panel.child(new ToggleButton()
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .value(autoOutputItems));
        panel.child(new ToggleButton()
                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                .value(autoOutputFluids)
                .pos(20, 0));

        return panel;
    }
}
