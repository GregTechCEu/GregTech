package gregtech.api.newgui;

import com.cleanroommc.modularui.drawable.UITexture;
import gregtech.api.GTValues;

public class GuiTextures extends com.cleanroommc.modularui.drawable.GuiTextures {

    //GREGTECH
    public static final UITexture GREGTECH_LOGO = UITexture.fullImage(GTValues.MODID, "textures/gui/icon/gregtech_logo.png", false);
    public static final UITexture GREGTECH_LOGO_XMAS = UITexture.fullImage(GTValues.MODID, "textures/gui/icon/gregtech_logo_xmas.png", false);
    public static final UITexture GREGTECH_LOGO_DARK = UITexture.fullImage(GTValues.MODID, "textures/gui/icon/gregtech_logo_dark.png", false);
    public static final UITexture GREGTECH_LOGO_BLINKING_YELLOW = UITexture.fullImage(GTValues.MODID, "textures/gui/icon/gregtech_logo_blinking_yellow.png", false);
    public static final UITexture GREGTECH_LOGO_BLINKING_RED = UITexture.fullImage(GTValues.MODID, "textures/gui/icon/gregtech_logo_blinking_red.png", false);

    //BASE TEXTURES
    public static final UITexture BACKGROUND = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/bordered_background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .canApplyTheme()
            .build();
    public static final UITexture BORDERED_BACKGROUND = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/bordered_background.png")
            .imageSize(195, 136)
            .adaptable(4)
            .canApplyTheme()
            .build();
    public static final UITexture BOXED_BACKGROUND = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/boxed_background.png")
            .imageSize(256, 174)
            .adaptable(11)
            .build();
    /*public static final UITexture BACKGROUND_STEAM = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/bordered_background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .canApplyTheme()
            .build();*/

    //public static final SteamTexture BACKGROUND_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/base/background_%s.png", 176, 166, 3, 3);
    public static final UITexture CLIPBOARD_BACKGROUND = UITexture.fullImage(GTValues.MODID, "textures/gui/base/clipboard_background.png", false);
    public static final UITexture CLIPBOARD_PAPER_BACKGROUND = UITexture.fullImage(GTValues.MODID, "textures/gui/base/clipboard_paper_background.png", false);

    public static final UITexture DISPLAY = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/display.png")
            .imageSize(143, 75)
            .adaptable(2)
            .build();

    public static final UITexture BUTTON_PROXY = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_proxy.png", false);
    public static final UITexture CLIPBOARD_CHECK_BOX = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/clipboard_checkbox.png", false);

    /*public static final UITexture BLANK = UITexture.fullImage(GTValues.MODID, "textures/gui/base/blank.png", 1, 1, 0, 0);
    public static final UITexture DISPLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/base/display.png", 143, 75, 2, 2);
    public static final SteamTexture DISPLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/base/display_%s.png", 143, 75, 2, 2);
    public static final UITexture FLUID_SLOT = UITexture.fullImage(GTValues.MODID, "textures/gui/base/fluid_slot.png", 18, 18, 1, 1);
    public static final UITexture FLUID_TANK_BACKGROUND = UITexture.fullImage(GTValues.MODID, "textures/gui/base/fluid_tank_background.png");
    public static final UITexture FLUID_TANK_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/base/fluid_tank_overlay.png");
    public static final UITexture SLOT = UITexture.fullImage(GTValues.MODID, "textures/gui/base/slot.png", 18, 18, 1, 1);
    public static final UITexture SLOT_DARKENED = UITexture.fullImage(GTValues.MODID, "textures/gui/base/darkened_slot.png");
    public static final SteamTexture SLOT_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/base/slot_%s.png");
    public static final UITexture TOGGLE_BUTTON_BACK = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/toggle_button_background.png");*/

    //FLUID & ITEM OUTPUT BUTTONS
    public static final UITexture BLOCKS_INPUT = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_blocks_input.png");
    public static final UITexture BUTTON = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button.png");
    public static final UITexture BUTTON_ALLOW_IMPORT_EXPORT = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_allow_import_export.png");
    public static final UITexture BUTTON_BLACKLIST = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_blacklist.png");
    public static final UITexture BUTTON_CLEAR_GRID = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_clear_grid.png");
    public static final UITexture BUTTON_FILTER_DAMAGE = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_filter_damage.png");
    public static final UITexture BUTTON_FILTER_NBT = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_filter_nbt.png");
    public static final UITexture BUTTON_FLUID_OUTPUT = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_fluid_output_overlay.png");
    public static final UITexture BUTTON_ITEM_OUTPUT = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_item_output_overlay.png");
    public static final UITexture BUTTON_LOCK = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_lock.png");
    public static final UITexture BUTTON_VOID = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_void.png");
    public static final UITexture BUTTON_VOID_NONE = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_void_none.png");
    public static final UITexture BUTTON_VOID_MULTIBLOCK = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_void_multiblock.png");
    public static final UITexture BUTTON_LEFT = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/left.png");
    public static final UITexture BUTTON_OVERCLOCK = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_overclock.png");
    public static final UITexture BUTTON_PUBLIC_PRIVATE = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_public_private.png");
    public static final UITexture BUTTON_RIGHT = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/right.png");
    public static final UITexture BUTTON_SWITCH_VIEW = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_switch_view.png");
    public static final UITexture BUTTON_WORKING_ENABLE = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_working_enable.png");
    public static final UITexture BUTTON_INT_CIRCUIT_PLUS = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_circuit_plus.png");
    public static final UITexture BUTTON_INT_CIRCUIT_MINUS = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_circuit_minus.png");
    public static final UITexture CLIPBOARD_BUTTON = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/clipboard_button.png");
    //public static final UITexture CLIPBOARD_TEXT_BOX = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/clipboard_text_box.png", 9, 18, 1, 1);
    public static final UITexture DISTRIBUTION_MODE = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_distribution_mode.png");
    public static final UITexture LOCK = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/lock.png");
    public static final UITexture LOCK_WHITE = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/lock_white.png");
    public static final UITexture SWITCH = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/switch.png");
    public static final UITexture SWITCH_HORIZONTAL = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/switch_horizontal.png");
    //public static final UITexture VANILLA_BUTTON = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/vanilla_button.png", 200, 40);
    public static final UITexture BUTTON_POWER = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_power.png");
    public static final UITexture BUTTON_POWER_DETAIL = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_power_detail.png");
    public static final UITexture BUTTON_DISTINCT_BUSES = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_distinct_buses.png");
    public static final UITexture BUTTON_NO_DISTINCT_BUSES = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_no_distinct_buses.png");
    public static final UITexture BUTTON_NO_FLEX = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_no_flex.png");
    public static final UITexture BUTTON_MULTI_MAP = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_multi_map.png");
    public static final UITexture BUTTON_MINER_MODES = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_miner_modes.png");
    public static final UITexture BUTTON_THROTTLE_MINUS = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_throttle_minus.png");
    public static final UITexture BUTTON_THROTTLE_PLUS = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_throttle_plus.png");

    //INDICATORS & ICONS
    public static final UITexture INDICATOR_NO_ENERGY = UITexture.fullImage(GTValues.MODID, "textures/gui/base/indicator_no_energy.png");
    //public static final SteamTexture INDICATOR_NO_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/base/indicator_no_steam_%s.png");
    public static final UITexture TANK_ICON = UITexture.fullImage(GTValues.MODID, "textures/gui/base/tank_icon.png");

    //WIDGET UI RELATED
    public static final UITexture SLIDER_BACKGROUND = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/slider_background.png");
    public static final UITexture SLIDER_BACKGROUND_VERTICAL = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/slider_background_vertical.png");
    public static final UITexture SLIDER_ICON = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/slider.png");
    public static final UITexture MAINTENANCE_ICON = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/button_maintenance.png");

    //PRIMITIVE
    //public static final UITexture PRIMITIVE_BACKGROUND = UITexture.fullImage(GTValues.MODID, "textures/gui/primitive/primitive_background.png", 176, 166, 3, 3);
    //public static final UITexture PRIMITIVE_SLOT = UITexture.fullImage(GTValues.MODID, "textures/gui/primitive/primitive_slot.png", 18, 18, 1, 1);
    public static final UITexture PRIMITIVE_FURNACE_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/primitive/overlay_primitive_furnace.png");
    public static final UITexture PRIMITIVE_DUST_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/primitive/overlay_primitive_dust.png");
    public static final UITexture PRIMITIVE_INGOT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/primitive/overlay_primitive_ingot.png");
    public static final UITexture PRIMITIVE_LARGE_FLUID_TANK = UITexture.fullImage(GTValues.MODID, "textures/gui/primitive/primitive_large_fluid_tank.png");
    public static final UITexture PRIMITIVE_LARGE_FLUID_TANK_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/primitive/primitive_large_fluid_tank_overlay.png");
    public static final UITexture PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR = UITexture.fullImage(GTValues.MODID, "textures/gui/primitive/progress_bar_primitive_blast_furnace.png");

    //SLOT OVERLAYS
    public static final UITexture ATOMIC_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/atomic_overlay_1.png");
    public static final UITexture ATOMIC_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/atomic_overlay_2.png");
    public static final UITexture ARROW_INPUT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/arrow_input_overlay.png");
    public static final UITexture ARROW_OUTPUT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/arrow_output_overlay.png");
    public static final UITexture BATTERY_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/battery_overlay.png");
    public static final UITexture BEAKER_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/beaker_overlay_1.png");
    public static final UITexture BEAKER_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/beaker_overlay_2.png");
    public static final UITexture BEAKER_OVERLAY_3 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/beaker_overlay_3.png");
    public static final UITexture BEAKER_OVERLAY_4 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/beaker_overlay_4.png");
    public static final UITexture BENDER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/bender_overlay.png");
    public static final UITexture BOX_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/box_overlay.png");
    public static final UITexture BOXED_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/boxed_overlay.png");
    public static final UITexture BREWER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/brewer_overlay.png");
    public static final UITexture CANNER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/canner_overlay.png");
    public static final UITexture CHARGER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/charger_slot_overlay.png");
    public static final UITexture CANISTER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/canister_overlay.png");
    //public static final SteamTexture CANISTER_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/canister_overlay_%s.png");
    public static final UITexture CENTRIFUGE_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/centrifuge_overlay.png");
    public static final UITexture CIRCUIT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/circuit_overlay.png");
    //public static final SteamTexture COAL_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/coal_overlay_%s.png");
    public static final UITexture COMPRESSOR_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/compressor_overlay.png");
    //public static final SteamTexture COMPRESSOR_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/compressor_overlay_%s.png");
    public static final UITexture CRACKING_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/cracking_overlay_1.png");
    public static final UITexture CRACKING_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/cracking_overlay_2.png");
    public static final UITexture CRUSHED_ORE_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/crushed_ore_overlay.png");
    //public static final SteamTexture CRUSHED_ORE_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/crushed_ore_overlay_%s.png");
    public static final UITexture CRYSTAL_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/crystal_overlay.png");
    public static final UITexture CUTTER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/cutter_overlay.png");
    public static final UITexture DARK_CANISTER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/dark_canister_overlay.png");
    public static final UITexture DUST_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/dust_overlay.png");
    //public static final SteamTexture DUST_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/dust_overlay_%s.png");
    public static final UITexture EXTRACTOR_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/extractor_overlay.png");
    //public static final SteamTexture EXTRACTOR_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/extractor_overlay_%s.png");
    public static final UITexture FILTER_SLOT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/filter_slot_overlay.png");
    public static final UITexture FURNACE_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/furnace_overlay_1.png");
    public static final UITexture FURNACE_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/furnace_overlay_2.png");
    //public static final SteamTexture FURNACE_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/furnace_overlay_%s.png");
    public static final UITexture HAMMER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/hammer_overlay.png");
    //public static final SteamTexture HAMMER_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/hammer_overlay_%s.png");
    public static final UITexture HEATING_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/heating_overlay_1.png");
    public static final UITexture HEATING_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/heating_overlay_2.png");
    public static final UITexture IMPLOSION_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/implosion_overlay_1.png");
    public static final UITexture IMPLOSION_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/implosion_overlay_2.png");
    public static final UITexture IN_SLOT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/in_slot_overlay.png");
    //public static final SteamTexture IN_SLOT_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/in_slot_overlay_%s.png");
    public static final UITexture INGOT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/ingot_overlay.png");
    public static final UITexture INT_CIRCUIT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/int_circuit_overlay.png");
    public static final UITexture LENS_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/lens_overlay.png");
    public static final UITexture LIGHTNING_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/lightning_overlay_1.png");
    public static final UITexture LIGHTNING_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/lightning_overlay_2.png");
    public static final UITexture MOLD_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/mold_overlay.png");
    public static final UITexture MOLECULAR_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/molecular_overlay_1.png");
    public static final UITexture MOLECULAR_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/molecular_overlay_2.png");
    public static final UITexture MOLECULAR_OVERLAY_3 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/molecular_overlay_3.png");
    public static final UITexture MOLECULAR_OVERLAY_4 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/molecular_overlay_4.png");
    public static final UITexture OUT_SLOT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/out_slot_overlay.png");
    //public static final SteamTexture OUT_SLOT_OVERLAY_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/overlay/out_slot_overlay_%s.png");
    public static final UITexture PAPER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/paper_overlay.png");
    public static final UITexture PRINTED_PAPER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/printed_paper_overlay.png");
    public static final UITexture PIPE_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/pipe_overlay_2.png");
    public static final UITexture PIPE_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/pipe_overlay_1.png");
    public static final UITexture PRESS_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/press_overlay_1.png");
    public static final UITexture PRESS_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/press_overlay_2.png");
    public static final UITexture PRESS_OVERLAY_3 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/press_overlay_3.png");
    public static final UITexture PRESS_OVERLAY_4 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/press_overlay_4.png");
    public static final UITexture SAWBLADE_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/sawblade_overlay.png");
    public static final UITexture SOLIDIFIER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/solidifier_overlay.png");
    public static final UITexture STRING_SLOT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/string_slot_overlay.png");
    public static final UITexture TOOL_SLOT_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/tool_slot_overlay.png");
    public static final UITexture TURBINE_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/turbine_overlay.png");
    public static final UITexture VIAL_OVERLAY_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/vial_overlay_1.png");
    public static final UITexture VIAL_OVERLAY_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/vial_overlay_2.png");
    public static final UITexture WIREMILL_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/wiremill_overlay.png");
    public static final UITexture POSITIVE_MATTER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/positive_matter_overlay.png");
    public static final UITexture NEUTRAL_MATTER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/neutral_matter_overlay.png");
    public static final UITexture DATA_ORB_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/data_orb_overlay.png");
    public static final UITexture SCANNER_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/scanner_overlay.png");
    public static final UITexture DUCT_TAPE_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/duct_tape_overlay.png");
    public static final UITexture RESEARCH_STATION_OVERLAY = UITexture.fullImage(GTValues.MODID, "textures/gui/overlay/research_station_overlay.png");

    //PROGRESS BARS
    public static final UITexture PROGRESS_BAR_ARC_FURNACE = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_arc_furnace.png");
    public static final UITexture PROGRESS_BAR_ARROW = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_arrow.png");
    //public static final SteamTexture PROGRESS_BAR_ARROW_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_arrow_%s.png");
    public static final UITexture PROGRESS_BAR_ARROW_MULTIPLE = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_arrow_multiple.png");
    public static final UITexture PROGRESS_BAR_ASSEMBLY_LINE = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_assembly_line.png");
    public static final UITexture PROGRESS_BAR_ASSEMBLY_LINE_ARROW = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_assembly_line_arrow.png");
    public static final UITexture PROGRESS_BAR_BATH = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_bath.png");
    public static final UITexture PROGRESS_BAR_BENDING = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_bending.png");
    //public static final SteamTexture PROGRESS_BAR_BOILER_EMPTY = SteamTexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_boiler_empty_%s.png");
    //public static final SteamTexture PROGRESS_BAR_BOILER_FUEL = SteamTexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_boiler_fuel_%s.png");
    public static final UITexture PROGRESS_BAR_BOILER_HEAT = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_boiler_heat.png");
    public static final UITexture PROGRESS_BAR_CANNER = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_canner.png");
    public static final UITexture PROGRESS_BAR_CIRCUIT = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_circuit.png");
    public static final UITexture PROGRESS_BAR_CIRCUIT_ASSEMBLER = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_circuit_assembler.png");
    public static final UITexture PROGRESS_BAR_COKE_OVEN = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_coke_oven.png");
    public static final UITexture PROGRESS_BAR_COMPRESS = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_compress.png");
    //public static final SteamTexture PROGRESS_BAR_COMPRESS_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_compress_%s.png");
    public static final UITexture PROGRESS_BAR_CRACKING = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_cracking.png");
    public static final UITexture PROGRESS_BAR_CRACKING_INPUT = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_cracking_2.png");
    public static final UITexture PROGRESS_BAR_CRYSTALLIZATION = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_crystallization.png");
    public static final UITexture PROGRESS_BAR_DISTILLATION_TOWER = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_distillation_tower.png");
    public static final UITexture PROGRESS_BAR_EXTRACT = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_extract.png");
    //public static final SteamTexture PROGRESS_BAR_EXTRACT_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_extract_%s.png");
    public static final UITexture PROGRESS_BAR_EXTRUDER = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_extruder.png");
    public static final UITexture PROGRESS_BAR_FUSION = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_fusion.png");
    public static final UITexture PROGRESS_BAR_GAS_COLLECTOR = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_gas_collector.png");
    public static final UITexture PROGRESS_BAR_HAMMER = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_hammer.png");
    //public static final SteamTexture PROGRESS_BAR_HAMMER_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_hammer_%s.png");
    public static final UITexture PROGRESS_BAR_HAMMER_BASE = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_hammer_base.png");
    //public static final SteamTexture PROGRESS_BAR_HAMMER_BASE_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_hammer_base_%s.png");
    public static final UITexture PROGRESS_BAR_LATHE = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_lathe.png");
    public static final UITexture PROGRESS_BAR_LATHE_BASE = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_lathe_base.png");
    public static final UITexture PROGRESS_BAR_MACERATE = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_macerate.png");
    //public static final SteamTexture PROGRESS_BAR_MACERATE_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_macerate_%s.png");
    public static final UITexture PROGRESS_BAR_MAGNET = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_magnet.png");
    public static final UITexture PROGRESS_BAR_MASS_FAB = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_mass_fab.png");
    public static final UITexture PROGRESS_BAR_MIXER = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_mixer.png");
    public static final UITexture PROGRESS_BAR_PACKER = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_packer.png");
    public static final UITexture PROGRESS_BAR_RECYCLER = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_recycler.png");
    public static final UITexture PROGRESS_BAR_REPLICATOR = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_replicator.png");
    public static final UITexture PROGRESS_BAR_SIFT = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_sift.png");
    public static final UITexture PROGRESS_BAR_SLICE = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_slice.png");
    //public static final SteamTexture PROGRESS_BAR_SOLAR_STEAM = SteamTexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_solar_%s.png");
    public static final UITexture PROGRESS_BAR_UNLOCK = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_unlock.png");
    public static final UITexture PROGRESS_BAR_UNPACKER = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_unpacker.png");
    public static final UITexture PROGRESS_BAR_WIREMILL = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_wiremill.png");
    public static final UITexture PROGRESS_BAR_RESEARCH_STATION_1 = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_research_station_1.png");
    public static final UITexture PROGRESS_BAR_RESEARCH_STATION_2 = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_research_station_2.png");
    public static final UITexture PROGRESS_BAR_RESEARCH_STATION_BASE = UITexture.fullImage(GTValues.MODID, "textures/gui/progress_bar/progress_bar_research_station_base.png");

    //JEI
    public static final UITexture INFO_ICON = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/information.png");
    public static final UITexture MULTIBLOCK_CATEGORY = UITexture.fullImage(GTValues.MODID, "textures/gui/icon/multiblock_category.png");
    public static final UITexture ARC_FURNACE_RECYLCING_CATEGORY = UITexture.fullImage(GTValues.MODID, "textures/gui/icon/arc_furnace_recycling.png");
    public static final UITexture MACERATOR_RECYLCING_CATEGORY = UITexture.fullImage(GTValues.MODID, "textures/gui/icon/macerator_recycling.png");
    public static final UITexture EXTRACTOR_RECYLCING_CATEGORY = UITexture.fullImage(GTValues.MODID, "textures/gui/icon/extractor_recycling.png");

    // Covers
    public static final UITexture COVER_MACHINE_CONTROLLER = UITexture.fullImage(GTValues.MODID, "textures/items/metaitems/cover.controller.png");

    // Ore Filter
    public static final UITexture ORE_FILTER_INFO = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/info.png");
    public static final UITexture ORE_FILTER_SUCCESS = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/success.png");
    public static final UITexture ORE_FILTER_ERROR = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/error.png");
    public static final UITexture ORE_FILTER_WARN = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/warn.png");
    public static final UITexture ORE_FILTER_WAITING = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/waiting.png");

    public static final UITexture ORE_FILTER_MATCH = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/match.png");
    public static final UITexture ORE_FILTER_NO_MATCH = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/ore_filter/no_match.png");

    //Terminal
    public static final UITexture ICON_REMOVE = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/remove_hover.png");
    public static final UITexture ICON_UP = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/up_hover.png");
    public static final UITexture ICON_DOWN = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/down_hover.png");
    public static final UITexture ICON_RIGHT = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/right_hover.png");
    public static final UITexture ICON_LEFT = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/left_hover.png");
    public static final UITexture ICON_ADD = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/add_hover.png");

    public final static UITexture ICON_NEW_PAGE = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/system/memory_card_hover.png");
    public final static UITexture ICON_LOAD = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/folder_hover.png");
    public final static UITexture ICON_SAVE = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/system/save_hover.png");
    public final static UITexture ICON_LOCATION = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/guide_hover.png");
    public final static UITexture ICON_VISIBLE = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/appearance_hover.png");
    public final static UITexture ICON_CALCULATOR = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/icon/calculator_hover.png");
    public final static UITexture UI_FRAME_SIDE_UP = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/frame_side_up.png");
    public final static UITexture UI_FRAME_SIDE_DOWN = UITexture.fullImage(GTValues.MODID, "textures/gui/terminal/frame_side_down.png");

    // Texture Areas
    public static final UITexture BUTTON_FLUID = UITexture.fullImage(GTValues.MODID, "textures/blocks/cover/cover_interface_fluid_button.png");
    public static final UITexture BUTTON_ITEM = UITexture.fullImage(GTValues.MODID, "textures/blocks/cover/cover_interface_item_button.png");
    public static final UITexture BUTTON_ENERGY = UITexture.fullImage(GTValues.MODID, "textures/blocks/cover/cover_interface_energy_button.png");
    public static final UITexture BUTTON_MACHINE = UITexture.fullImage(GTValues.MODID, "textures/blocks/cover/cover_interface_machine_button.png");
    public static final UITexture BUTTON_INTERFACE = UITexture.fullImage(GTValues.MODID, "textures/blocks/cover/cover_interface_computer_button.png");
    public static final UITexture COVER_INTERFACE_MACHINE_ON_PROXY = UITexture.fullImage(GTValues.MODID, "textures/blocks/cover/cover_interface_machine_on_proxy.png");
    public static final UITexture COVER_INTERFACE_MACHINE_OFF_PROXY = UITexture.fullImage(GTValues.MODID, "textures/blocks/cover/cover_interface_machine_off_proxy.png");

    // Lamp item overlay
    public static final UITexture LAMP_NO_BLOOM = UITexture.fullImage(GTValues.MODID, "textures/gui/item_overlay/lamp_no_bloom.png");
    public static final UITexture LAMP_NO_LIGHT = UITexture.fullImage(GTValues.MODID, "textures/gui/item_overlay/lamp_no_light.png");

    // ME hatch/bus
    public static final UITexture NUMBER_BACKGROUND = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/number_background.png");
    public static final UITexture CONFIG_ARROW = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/config_arrow.png");
    public static final UITexture CONFIG_ARROW_DARK = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/config_arrow_dark.png");
    public static final UITexture SELECT_BOX = UITexture.fullImage(GTValues.MODID, "textures/gui/widget/select_box.png");
}
