package gregtech.api.configurator.profile;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IGuiAction.MousePressed;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SimpleMachineProfile implements IMachineConfiguratorProfile {

    public static final SimpleMachineProfile INSTANCE = new SimpleMachineProfile();
    private int networkID;

    @Nullable
    private static IDrawable itemAndFluidOverlay;

    private SimpleMachineProfile() {}

    @Override
    public @NotNull String getName() {
        return "SimpleMachineProfile";
    }

    @Override
    public int networkID() {
        return networkID;
    }

    @Override
    public void setNetworkID(int networkID) {
        this.networkID = networkID;
    }

    @Override
    public @NotNull IKey getProfileName() {
        return IKey.lang("gregtech.machine_configurator.simple_machine_profile");
    }

    @NotNull
    @Override
    public ModularPanel createConfiguratorPanel(@NotNull PanelSyncManager panelSyncManager,
                                                @NotNull NBTTagCompound config) {
        var panel = GTGuis.createPanel("simple_machine_configurator", 102, 62)
                .child(IKey.lang("gregtech.machine_configurator.simple_machine_profile")
                        .color(CoverWithUI.UI_TITLE_COLOR)
                        .asWidget()
                        .left(4)
                        .top(4));

        BooleanSyncValue autoOutputItems = new BooleanSyncValue(() -> config.getBoolean("AutoOutputItems"),
                bool -> config.setBoolean("AutoOutputItems", bool));
        BooleanSyncValue autoOutputFluids = new BooleanSyncValue(() -> config.getBoolean("AutoOutputFluids"),
                bool -> config.setBoolean("AutoOutputFluids", bool));

        BooleanSyncValue allowItemInputFromOutput = new BooleanSyncValue(
                () -> config.getBoolean("AllowItemInputFromOutput"),
                bool -> config.setBoolean("AllowItemInputFromOutput", bool));
        BooleanSyncValue allowFluidInputFromOutput = new BooleanSyncValue(
                () -> config.getBoolean("AllowFluidInputFromOutput"),
                bool -> config.setBoolean("AllowFluidInputFromOutput", bool));

        EnumSyncValue<EnumFacing> itemOutputSide = new EnumSyncValue<>(EnumFacing.class,
                () -> EnumFacing.values()[config.getByte("ItemOutputSide")],
                side -> config.setByte("ItemOutputSide", (byte) side.getIndex()));
        EnumSyncValue<EnumFacing> fluidOutputSide = new EnumSyncValue<>(EnumFacing.class,
                () -> EnumFacing.values()[config.getByte("FluidOutputSide")],
                side -> config.setByte("FluidOutputSide", (byte) side.getIndex()));

        panelSyncManager.syncValue("ItemOutputSide", itemOutputSide);
        panelSyncManager.syncValue("FluidOutputSide", fluidOutputSide);

        List<Widget<?>> controls = new ArrayList<>(4);
        controls.add(new ToggleButton()
                .value(autoOutputItems)
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .addTooltip(false, IKey.lang("gregtech.gui.item_auto_output.tooltip.disabled"))
                .addTooltip(true, IKey.lang("gregtech.gui.item_auto_output.tooltip.enabled")));
        controls.add(new ToggleButton()
                .value(allowItemInputFromOutput)
                .overlay(false, GTGuiTextures.INPUT_FROM_OUTPUT_ITEM[0])
                .overlay(true, GTGuiTextures.INPUT_FROM_OUTPUT_ITEM[1])
                .addTooltip(false, IKey.lang(
                        "gregtech.machine_configurator.simple_machine_profile.item_input_from_output_side.disallow"))
                .addTooltip(true, IKey.lang(
                        "gregtech.machine_configurator.simple_machine_profile.item_input_from_output_side.allow")));
        controls.add(new ToggleButton()
                .value(autoOutputFluids)
                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                .addTooltip(false, IKey.lang("gregtech.gui.fluid_auto_output.tooltip.disabled"))
                .addTooltip(true, IKey.lang("gregtech.gui.fluid_auto_output.tooltip.enabled")));
        controls.add(new ToggleButton()
                .value(allowFluidInputFromOutput)
                .overlay(false, GTGuiTextures.INPUT_FROM_OUTPUT_FLUID[0])
                .overlay(true, GTGuiTextures.INPUT_FROM_OUTPUT_FLUID[1])
                .addTooltip(false, IKey.lang(
                        "gregtech.machine_configurator.simple_machine_profile.fluid_input_from_output_side.disallow"))
                .addTooltip(true, IKey.lang(
                        "gregtech.machine_configurator.simple_machine_profile.fluid_input_from_output_side.allow")));

        panel.child(Flow.row()
                .left(4)
                .top(4)
                .coverChildren()
                .child(new Grid()
                        .coverChildren()
                        .top(18)
                        .mapTo(2, controls))
                .child(SlotGroupWidget.builder()
                        .matrix(" B ",
                                "BBB",
                                " BB")
                        .key('B', buttonIndex -> new ButtonWidget<>()
                                .overlay(getSideButtonOverlay(buttonIndex, itemOutputSide, fluidOutputSide))
                                .onMousePressed(getSideButtonAction(buttonIndex, itemOutputSide, fluidOutputSide))
                                .tooltipBuilder(getSideButtonTooltip(buttonIndex)))
                        .build()
                        .marginLeft(4)));

        return panel;
    }

    private static @NotNull IDrawable getSideButtonOverlay(int buttonIndex,
                                                           @NotNull EnumSyncValue<EnumFacing> itemOutputSide,
                                                           @NotNull EnumSyncValue<EnumFacing> fluidOutputSide) {
        return switch (buttonIndex) {
            case (0) -> new DynamicDrawable(() -> {
                boolean isItem = itemOutputSide.getValue().equals(EnumFacing.UP);
                boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.UP);

                return getSideOverlay(isItem, isFluid);
            });
            case (1) -> new DynamicDrawable(() -> {
                boolean isItem = itemOutputSide.getValue().equals(EnumFacing.EAST);
                boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.EAST);

                return getSideOverlay(isItem, isFluid);
            });
            case (2) -> GTGuiTextures.ASSEMBLER_FRONT_OVERLAY;
            case (3) -> new DynamicDrawable(() -> {
                boolean isItem = itemOutputSide.getValue().equals(EnumFacing.WEST);
                boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.WEST);

                return getSideOverlay(isItem, isFluid);
            });
            case (4) -> new DynamicDrawable(() -> {
                boolean isItem = itemOutputSide.getValue().equals(EnumFacing.DOWN);
                boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.DOWN);

                return getSideOverlay(isItem, isFluid);
            });
            case (5) -> new DynamicDrawable(() -> {
                boolean isItem = itemOutputSide.getValue().equals(EnumFacing.SOUTH);
                boolean isFluid = fluidOutputSide.getValue().equals(EnumFacing.SOUTH);

                return getSideOverlay(isItem, isFluid);
            });
            default -> IDrawable.EMPTY;
        };
    }

    private static @NotNull IDrawable getSideOverlay(boolean isItem, boolean isFluid) {
        if (isItem && isFluid) {
            if (itemAndFluidOverlay == null) {
                itemAndFluidOverlay = IDrawable.of(GTGuiTextures.ITEM_OUTPUT_OVERLAY,
                        GTGuiTextures.FLUID_OUTPUT_OVERLAY);
            }

            return itemAndFluidOverlay;
        } else if (isItem) {
            return GTGuiTextures.ITEM_OUTPUT_OVERLAY;
        } else if (isFluid) {
            return GTGuiTextures.FLUID_OUTPUT_OVERLAY;
        }

        return IDrawable.EMPTY;
    }

    private static @NotNull MousePressed getSideButtonAction(int buttonIndex,
                                                             @NotNull EnumSyncValue<EnumFacing> itemOutputSide,
                                                             @NotNull EnumSyncValue<EnumFacing> fluidOutputSide) {
        return switch (buttonIndex) {
            case (0) -> button -> {
                if (button == 0) {
                    itemOutputSide.setValue(EnumFacing.UP);
                    return true;
                } else if (button == 1) {
                    fluidOutputSide.setValue(EnumFacing.UP);
                    return true;
                }

                return false;
            };
            case (1) -> button -> {
                if (button == 0) {
                    itemOutputSide.setValue(EnumFacing.EAST);
                    return true;
                } else if (button == 1) {
                    fluidOutputSide.setValue(EnumFacing.EAST);
                    return true;
                }

                return false;
            };
            case (3) -> button -> {
                if (button == 0) {
                    itemOutputSide.setValue(EnumFacing.WEST);
                    return true;
                } else if (button == 1) {
                    fluidOutputSide.setValue(EnumFacing.WEST);
                    return true;
                }

                return false;
            };
            case (4) -> button -> {
                if (button == 0) {
                    itemOutputSide.setValue(EnumFacing.DOWN);
                    return true;
                } else if (button == 1) {
                    fluidOutputSide.setValue(EnumFacing.DOWN);
                    return true;
                }

                return false;
            };
            case (5) -> button -> {
                if (button == 0) {
                    itemOutputSide.setValue(EnumFacing.SOUTH);
                    return true;
                } else if (button == 1) {
                    fluidOutputSide.setValue(EnumFacing.SOUTH);
                    return true;
                }

                return false;
            };
            default -> $ -> false;
        };
    }

    private static @NotNull Consumer<RichTooltip> getSideButtonTooltip(int buttonIndex) {
        if (buttonIndex == 2) {
            return tooltip -> tooltip
                    .addLine(IKey.lang("gregtech.machine_configurator.simple_machine_profile.front_face"));
        }

        return tooltip -> {
            tooltip.addLine(IKey.lang("gregtech.machine_configurator.simple_machine_profile.item_output"));
            tooltip.addLine(IKey.lang("gregtech.machine_configurator.simple_machine_profile.fluid_output"));
        };
    }
}
