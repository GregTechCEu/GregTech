package gregtech.api.mui;

import gregtech.api.GTValues;

import com.cleanroommc.modularui.drawable.UITexture;

public class GTGuiTextures {

    /** Keys used for GT assets registered for use in Themes */
    public static class IDs {

        public static final String STANDARD_BACKGROUND = "gregtech_standard_bg";
        public static final String BRONZE_BACKGROUND = "gregtech_bronze_bg";
        public static final String STEEL_BACKGROUND = "gregtech_steel_bg";
        public static final String PRIMITIVE_BACKGROUND = "gregtech_primitive_bg";

        public static final String STANDARD_SLOT = "gregtech_standard_slot";
        public static final String BRONZE_SLOT = "gregtech_bronze_slot";
        public static final String STEEL_SLOT = "gregtech_steel_slot";
        public static final String PRIMITIVE_SLOT = "gregtech_primitive_slot";

        public static final String STANDARD_FLUID_SLOT = "gregtech_standard_fluid_slot";

        public static final String STANDARD_BUTTON = "gregtech_standard_button";
    }

    // GT LOGOS
    public static final UITexture GREGTECH_LOGO = fullImage("textures/gui/icon/gregtech_logo.png");
    public static final UITexture GREGTECH_LOGO_XMAS = fullImage("textures/gui/icon/gregtech_logo_xmas.png");
    public static final UITexture GREGTECH_LOGO_DARK = fullImage("textures/gui/icon/gregtech_logo_dark.png");
    // todo blinking GT logos

    // BACKGROUNDS
    public static final UITexture BACKGROUND = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .registerAsBackground(IDs.STANDARD_BACKGROUND, true)
            .build();

    // todo BORDERED/BOXED backgrounds will not be ported, if possible

    public static final UITexture BACKGROUND_BRONZE = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background_bronze.png")
            .imageSize(176, 166)
            .adaptable(3)
            .registerAsBackground(IDs.BRONZE_BACKGROUND)
            .build();

    public static final UITexture BACKGROUND_STEEL = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background_steel.png")
            .imageSize(176, 166)
            .adaptable(3)
            .registerAsBackground(IDs.STEEL_BACKGROUND)
            .build();

    // todo move to textures/gui/base
    public static final UITexture BACKGROUND_PRIMITIVE = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/primitive/primitive_background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .registerAsBackground(IDs.PRIMITIVE_BACKGROUND)
            .build();

    // todo clipboard backgrounds, may deserve some redoing

    // DISPLAYS
    public static final UITexture DISPLAY = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/display.png")
            .imageSize(143, 75)
            .adaptable(2)
            .canApplyTheme()
            .build();

    public static final UITexture DISPLAY_BRONZE = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/display_bronze.png")
            .imageSize(143, 75)
            .adaptable(2)
            .build();

    public static final UITexture DISPLAY_STEEL = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/display_steel.png")
            .imageSize(143, 75)
            .adaptable(2)
            .build();

    // todo primitive display?

    // SLOTS
    public static final UITexture SLOT = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground(IDs.STANDARD_SLOT, true)
            .build();

    public static final UITexture SLOT_BRONZE = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/slot_bronze.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground(IDs.BRONZE_SLOT)
            .build();

    public static final UITexture SLOT_STEEL = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/slot_steel.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground(IDs.STEEL_SLOT)
            .build();

    // todo move to textures/gui/base
    public static final UITexture SLOT_PRIMITIVE = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/primitive/primitive_slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground(IDs.PRIMITIVE_SLOT)
            .build();

    public static final UITexture FLUID_SLOT = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/fluid_slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground(IDs.STANDARD_FLUID_SLOT, true)
            .build();

    // todo bronze/steel/primitive fluid slots?

    // SLOT OVERLAYS
    public static final UITexture ATOMIC_OVERLAY_1 = fullImage("textures/gui/overlay/atomic_overlay_1.png", true);
    public static final UITexture ATOMIC_OVERLAY_2 = fullImage("textures/gui/overlay/atomic_overlay_2.png", true);
    public static final UITexture ARROW_INPUT_OVERLAY = fullImage("textures/gui/overlay/arrow_input_overlay.png", true);
    public static final UITexture ARROW_OUTPUT_OVERLAY =
            fullImage("textures/gui/overlay/arrow_output_overlay.png", true);
    public static final UITexture BATTERY_OVERLAY = fullImage("textures/gui/overlay/battery_overlay.png", true);
    public static final UITexture BEAKER_OVERLAY_1 = fullImage("textures/gui/overlay/beaker_overlay_1.png", true);
    public static final UITexture BEAKER_OVERLAY_2 = fullImage("textures/gui/overlay/beaker_overlay_2.png", true);
    public static final UITexture BEAKER_OVERLAY_3 = fullImage("textures/gui/overlay/beaker_overlay_3.png", true);
    public static final UITexture BEAKER_OVERLAY_4 = fullImage("textures/gui/overlay/beaker_overlay_4.png", true);
    public static final UITexture BENDER_OVERLAY = fullImage("textures/gui/overlay/bender_overlay.png", true);
    public static final UITexture BOX_OVERLAY = fullImage("textures/gui/overlay/box_overlay.png", true);
    public static final UITexture BOXED_OVERLAY = fullImage("textures/gui/overlay/boxed_overlay.png", true);
    public static final UITexture BREWER_OVERLAY = fullImage("textures/gui/overlay/brewer_overlay.png", true);
    public static final UITexture CANNER_OVERLAY = fullImage("textures/gui/overlay/canner_overlay.png", true);
    public static final UITexture CHARGER_OVERLAY = fullImage("textures/gui/overlay/charger_slot_overlay.png", true);
    public static final UITexture CANISTER_OVERLAY = fullImage("textures/gui/overlay/canister_overlay.png", true);
    public static final UITexture CANISTER_OVERLAY_BRONZE =
            fullImage("textures/gui/overlay/canister_overlay_bronze.png");
    public static final UITexture CANISTER_OVERLAY_STEEL = fullImage("textures/gui/overlay/canister_overlay_steel.png");
    public static final UITexture CENTRIFUGE_OVERLAY = fullImage("textures/gui/overlay/centrifuge_overlay.png", true);
    public static final UITexture CIRCUIT_OVERLAY = fullImage("textures/gui/overlay/circuit_overlay.png", true);
    public static final UITexture COAL_OVERLAY_BRONZE = fullImage("textures/gui/overlay/coal_overlay_bronze.png");
    public static final UITexture COAL_OVERLAY_STEEL = fullImage("textures/gui/overlay/coal_overlay_steel.png");
    public static final UITexture COMPRESSOR_OVERLAY = fullImage("textures/gui/overlay/compressor_overlay.png", true);
    public static final UITexture COMPRESSOR_OVERLAY_BRONZE =
            fullImage("textures/gui/overlay/compressor_overlay_bronze.png");
    public static final UITexture COMPRESSOR_OVERLAY_STEEL =
            fullImage("textures/gui/overlay/compressor_overlay_steel.png");
    public static final UITexture CRACKING_OVERLAY_1 = fullImage("textures/gui/overlay/cracking_overlay_1.png", true);
    public static final UITexture CRACKING_OVERLAY_2 = fullImage("textures/gui/overlay/cracking_overlay_2.png", true);
    public static final UITexture CRUSHED_ORE_OVERLAY = fullImage("textures/gui/overlay/crushed_ore_overlay.png", true);
    public static final UITexture CRUSHED_ORE_OVERLAY_BRONZE =
            fullImage("textures/gui/overlay/crushed_ore_overlay_bronze.png");
    public static final UITexture CRUSHED_ORE_OVERLAY_STEEL =
            fullImage("textures/gui/overlay/crushed_ore_overlay_steel.png");
    public static final UITexture CRYSTAL_OVERLAY = fullImage("textures/gui/overlay/crystal_overlay.png", true);
    public static final UITexture CUTTER_OVERLAY = fullImage("textures/gui/overlay/cutter_overlay.png", true);
    public static final UITexture DARK_CANISTER_OVERLAY =
            fullImage("textures/gui/overlay/dark_canister_overlay.png", true);
    public static final UITexture DUST_OVERLAY = fullImage("textures/gui/overlay/dust_overlay.png", true);
    public static final UITexture DUST_OVERLAY_BRONZE = fullImage("textures/gui/overlay/dust_overlay_bronze.png");
    public static final UITexture DUST_OVERLAY_STEEL = fullImage("textures/gui/overlay/dust_overlay_steel.png");
    public static final UITexture EXTRACTOR_OVERLAY = fullImage("textures/gui/overlay/extractor_overlay.png", true);
    public static final UITexture EXTRACTOR_OVERLAY_BRONZE =
            fullImage("textures/gui/overlay/extractor_overlay_bronze.png");
    public static final UITexture EXTRACTOR_OVERLAY_STEEL =
            fullImage("textures/gui/overlay/extractor_overlay_steel.png");
    public static final UITexture FILTER_SLOT_OVERLAY = fullImage("textures/gui/overlay/filter_slot_overlay.png", true);
    public static final UITexture FURNACE_OVERLAY_1 = fullImage("textures/gui/overlay/furnace_overlay_1.png", true);
    public static final UITexture FURNACE_OVERLAY_2 = fullImage("textures/gui/overlay/furnace_overlay_2.png", true);
    public static final UITexture FURNACE_OVERLAY_BRONZE = fullImage("textures/gui/overlay/furnace_overlay_bronze.png");
    public static final UITexture FURNACE_OVERLAY_STEEL = fullImage("textures/gui/overlay/furnace_overlay_steel.png");
    public static final UITexture HAMMER_OVERLAY = fullImage("textures/gui/overlay/hammer_overlay.png", true);
    public static final UITexture HAMMER_OVERLAY_BRONZE = fullImage("textures/gui/overlay/hammer_overlay_bronze.png");
    public static final UITexture HAMMER_OVERLAY_STEEL = fullImage("textures/gui/overlay/hammer_overlay_steel.png");
    public static final UITexture HEATING_OVERLAY_1 = fullImage("textures/gui/overlay/heating_overlay_1.png", true);
    public static final UITexture HEATING_OVERLAY_2 = fullImage("textures/gui/overlay/heating_overlay_2.png", true);
    public static final UITexture IMPLOSION_OVERLAY_1 = fullImage("textures/gui/overlay/implosion_overlay_1.png", true);
    public static final UITexture IMPLOSION_OVERLAY_2 = fullImage("textures/gui/overlay/implosion_overlay_2.png", true);
    public static final UITexture IN_SLOT_OVERLAY = fullImage("textures/gui/overlay/in_slot_overlay.png", true);
    public static final UITexture IN_SLOT_OVERLAY_BRONZE = fullImage("textures/gui/overlay/in_slot_overlay_bronze.png");
    public static final UITexture IN_SLOT_OVERLAY_STEEL = fullImage("textures/gui/overlay/in_slot_overlay_steel.png");
    public static final UITexture INGOT_OVERLAY = fullImage("textures/gui/overlay/ingot_overlay.png", true);
    public static final UITexture INT_CIRCUIT_OVERLAY = fullImage("textures/gui/overlay/int_circuit_overlay.png", true);
    public static final UITexture LENS_OVERLAY = fullImage("textures/gui/overlay/lens_overlay.png", true);
    public static final UITexture LIGHTNING_OVERLAY_1 = fullImage("textures/gui/overlay/lightning_overlay_1.png", true);
    public static final UITexture LIGHTNING_OVERLAY_2 = fullImage("textures/gui/overlay/lightning_overlay_2.png", true);
    public static final UITexture MOLD_OVERLAY = fullImage("textures/gui/overlay/mold_overlay.png", true);
    public static final UITexture MOLECULAR_OVERLAY_1 = fullImage("textures/gui/overlay/molecular_overlay_1.png", true);
    public static final UITexture MOLECULAR_OVERLAY_2 = fullImage("textures/gui/overlay/molecular_overlay_2.png", true);
    public static final UITexture MOLECULAR_OVERLAY_3 = fullImage("textures/gui/overlay/molecular_overlay_3.png", true);
    public static final UITexture MOLECULAR_OVERLAY_4 = fullImage("textures/gui/overlay/molecular_overlay_4.png", true);
    public static final UITexture OUT_SLOT_OVERLAY = fullImage("textures/gui/overlay/out_slot_overlay.png", true);
    public static final UITexture OUT_SLOT_OVERLAY_BRONZE =
            fullImage("textures/gui/overlay/out_slot_overlay_bronze.png");
    public static final UITexture OUT_SLOT_OVERLAY_STEEL = fullImage("textures/gui/overlay/out_slot_overlay_steel.png");
    public static final UITexture PAPER_OVERLAY = fullImage("textures/gui/overlay/paper_overlay.png", true);
    public static final UITexture PRINTED_PAPER_OVERLAY =
            fullImage("textures/gui/overlay/printed_paper_overlay.png", true);
    public static final UITexture PIPE_OVERLAY_2 = fullImage("textures/gui/overlay/pipe_overlay_2.png", true);
    public static final UITexture PIPE_OVERLAY_1 = fullImage("textures/gui/overlay/pipe_overlay_1.png", true);
    public static final UITexture PRESS_OVERLAY_1 = fullImage("textures/gui/overlay/press_overlay_1.png", true);
    public static final UITexture PRESS_OVERLAY_2 = fullImage("textures/gui/overlay/press_overlay_2.png", true);
    public static final UITexture PRESS_OVERLAY_3 = fullImage("textures/gui/overlay/press_overlay_3.png", true);
    public static final UITexture PRESS_OVERLAY_4 = fullImage("textures/gui/overlay/press_overlay_4.png", true);
    public static final UITexture SAWBLADE_OVERLAY = fullImage("textures/gui/overlay/sawblade_overlay.png", true);
    public static final UITexture SOLIDIFIER_OVERLAY = fullImage("textures/gui/overlay/solidifier_overlay.png", true);
    public static final UITexture STRING_SLOT_OVERLAY = fullImage("textures/gui/overlay/string_slot_overlay.png", true);
    public static final UITexture TOOL_SLOT_OVERLAY = fullImage("textures/gui/overlay/tool_slot_overlay.png", true);
    public static final UITexture TURBINE_OVERLAY = fullImage("textures/gui/overlay/turbine_overlay.png", true);
    public static final UITexture VIAL_OVERLAY_1 = fullImage("textures/gui/overlay/vial_overlay_1.png", true);
    public static final UITexture VIAL_OVERLAY_2 = fullImage("textures/gui/overlay/vial_overlay_2.png", true);
    public static final UITexture WIREMILL_OVERLAY = fullImage("textures/gui/overlay/wiremill_overlay.png", true);
    public static final UITexture POSITIVE_MATTER_OVERLAY =
            fullImage("textures/gui/overlay/positive_matter_overlay.png", true);
    public static final UITexture NEUTRAL_MATTER_OVERLAY =
            fullImage("textures/gui/overlay/neutral_matter_overlay.png", true);
    public static final UITexture DATA_ORB_OVERLAY = fullImage("textures/gui/overlay/data_orb_overlay.png", true);
    public static final UITexture SCANNER_OVERLAY = fullImage("textures/gui/overlay/scanner_overlay.png", true);
    public static final UITexture DUCT_TAPE_OVERLAY = fullImage("textures/gui/overlay/duct_tape_overlay.png", true);
    public static final UITexture RESEARCH_STATION_OVERLAY =
            fullImage("textures/gui/overlay/research_station_overlay.png", true);

    // BUTTONS

    public static final UITexture BUTTON = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/widget/button.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsIcon(IDs.STANDARD_BUTTON)
            .canApplyTheme()
            .build();


    // BUTTON OVERLAYS

    public static final UITexture BUTTON_ITEM_OUTPUT = fullImage("textures/gui/widget/button_item_output_overlay.png");
    public static final UITexture BUTTON_AUTO_COLLAPSE =
            fullImage("textures/gui/widget/button_auto_collapse_overlay.png");
    public static final UITexture BUTTON_X = fullImage("textures/gui/widget/button_x_overlay.png", true);

    // PROGRESS BARS

    // MISC

    public static void init() {/**/}

    private static UITexture fullImage(String path) {
        return fullImage(path, false);
    }

    private static UITexture fullImage(String path, boolean canApplyTheme) {
        return UITexture.fullImage(GTValues.MODID, path, canApplyTheme);
    }
}
