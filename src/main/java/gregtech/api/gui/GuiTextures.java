package gregtech.api.gui;

import gregtech.api.gui.resources.AdoptableTextureArea;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;

public class GuiTextures {

    //BASE TEXTURES
    public static final TextureArea BACKGROUND = AdoptableTextureArea.fullImage("textures/gui/base/background.png", 176, 166, 3, 3);
    public static final TextureArea BORDERED_BACKGROUND = AdoptableTextureArea.fullImage("textures/gui/base/bordered_background.png", 195, 136, 4, 4);
    public static final TextureArea BOXED_BACKGROUND = AdoptableTextureArea.fullImage("textures/gui/base/boxed_background.png", 256, 174, 11, 11);

    //deprecated texture areas retained for binary & source compatibility
    @Deprecated public static final TextureArea BACKGROUND_SMALL = BACKGROUND; //replaced by normal background
    @Deprecated public static final TextureArea BACKGROUND_EXTENDED = BACKGROUND; //replaced by normal background
    @Deprecated public static final TextureArea BORDERED_BACKGROUND_EXTENDED = BORDERED_BACKGROUND; //replaced by normal background

    public static final TextureArea SLOT = AdoptableTextureArea.fullImage("textures/gui/base/slot.png", 18, 18, 1, 1);
    public static final TextureArea FLUID_SLOT = AdoptableTextureArea.fullImage("textures/gui/base/fluid_slot.png", 18, 18, 1, 1);
    public static final TextureArea SLOT_DARKENED = TextureArea.fullImage("textures/gui/base/darkened_slot.png");
    public static final TextureArea DISPLAY = TextureArea.fullImage("textures/gui/base/display.png");
    public static final TextureArea FLUID_TANK_BACKGROUND = TextureArea.fullImage("textures/gui/base/fluid_tank_background.png");
    public static final TextureArea FLUID_TANK_OVERLAY = TextureArea.fullImage("textures/gui/base/fluid_tank_overlay.png");

    //FLUID & ITEM OUTPUT BUTTONS
    public static final TextureArea BUTTON_FLUID_OUTPUT = TextureArea.fullImage("textures/gui/widget/button_fluid_output.png");
    public static final TextureArea BUTTON_ITEM_OUTPUT = TextureArea.fullImage("textures/gui/widget/button_item_output.png");
    public static final SizedTextureArea VANILLA_BUTTON = SizedTextureArea.fullImage("textures/gui/widget/vanilla_button.png", 200, 40);
    public static final TextureArea BUTTON = TextureArea.fullImage("textures/gui/widget/button.png");
    public static final TextureArea SWITCH = TextureArea.fullImage("textures/gui/widget/switch.png");
    public static final TextureArea SWITCH_HORIZONTAL = TextureArea.fullImage("textures/gui/widget/switch_horizontal.png");
    public static final TextureArea BUTTON_FILTER_DAMAGE = TextureArea.fullImage("textures/gui/widget/button_filter_damage.png");
    public static final TextureArea BUTTON_OVERCLOCK = TextureArea.fullImage("textures/gui/widget/button_overclock.png");
    public static final TextureArea BUTTON_FILTER_NBT = TextureArea.fullImage("textures/gui/widget/button_filter_nbt.png");
    public static final TextureArea BUTTON_BLACKLIST = TextureArea.fullImage("textures/gui/widget/button_blacklist.png");
    public static final TextureArea BUTTON_SWITCH_VIEW = TextureArea.fullImage("textures/gui/widget/button_switch_view.png");
    public static final TextureArea BUTTON_ALLOW_IMPORT_EXPORT = TextureArea.fullImage("textures/gui/widget/button_allow_import_export.png");
    public static final TextureArea BUTTON_CLEAR_GRID = TextureArea.fullImage("textures/gui/widget/button_clear_grid.png");
    public static final TextureArea LOCK = TextureArea.fullImage("textures/gui/widget/lock.png");

    //INDICATORS & ICONS
    public static final TextureArea INDICATOR_NO_ENERGY = TextureArea.fullImage("textures/gui/base/indicator_no_energy.png");
    public static final TextureArea TANK_ICON = TextureArea.fullImage("textures/gui/base/tank_icon.png");

    //WIDGET UI RELATED
    public static final TextureArea SLIDER_BACKGROUND = TextureArea.fullImage("textures/gui/widget/slider_background.png");
    public static final TextureArea SLIDER_ICON = TextureArea.fullImage("textures/gui/widget/slider.png");

    //BRONZE
    public static final TextureArea BRONZE_BACKGROUND = TextureArea.fullImage("textures/gui/steam/bronze/bronze_gui.png");
    public static final TextureArea BRONZE_SLOT = TextureArea.fullImage("textures/gui/steam/bronze/slot_bronze.png");
    public static final TextureArea BRONZE_BLAST_FURNACE_PROGRESS_BAR = TextureArea.fullImage("textures/gui/steam/bronze/progress_bar_bronze_blast_furnace.png");
    public static final TextureArea BRONZE_FURNACE_OVERLAY = TextureArea.fullImage("textures/gui/steam/bronze/slot_bronze_furnace_background.png");
    public static final TextureArea BRONZE_INGOT_OVERLAY = TextureArea.fullImage("textures/gui/steam/bronze/overlay_bronze_ingot.png");
    public static final TextureArea BRONZE_DUST_OVERLAY = TextureArea.fullImage("textures/gui/steam/bronze/overlay_bronze_dust.png");

    //ARMOR
    public static final TextureArea CONNECTION_TYPE_POWER = TextureArea.fullImage("textures/gui/armor/connection/power.png");
    public static final TextureArea COMPONENT_BATTERY = TextureArea.fullImage("textures/gui/armor/component/battery.png");

    //SLOT OVERLAYS
    public static final TextureArea TOOL_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/tool_slot_overlay.png");
    public static final TextureArea ARROW_INPUT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/arrow_input_overlay.png");
    public static final TextureArea ARROW_OUTPUT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/arrow_output_overlay.png");
    public static final TextureArea CHARGER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/charger_slot_overlay.png");
    public static final TextureArea INT_CIRCUIT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/int_circuit_overlay.png");
    public static final TextureArea TURBINE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/turbine_overlay.png");
    public static final TextureArea FURNACE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/furnace_overlay.png");
    public static final TextureArea COMPRESSOR_OVERLAY = TextureArea.fullImage("textures/gui/overlay/compressor_overlay.png");
    public static final TextureArea EXTRACTOR_OVERLAY = TextureArea.fullImage("textures/gui/overlay/extractor_overlay.png");
    public static final TextureArea CRUSHED_ORE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/crushed_ore_overlay.png");
    public static final TextureArea DUST_OVERLAY = TextureArea.fullImage("textures/gui/overlay/dust_overlay.png");
    public static final TextureArea INGOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/ingot_overlay.png");
    public static final TextureArea CIRCUIT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/circuit_overlay.png");
    public static final TextureArea BATTERY_OVERLAY = TextureArea.fullImage("textures/gui/overlay/battery_overlay.png");
    public static final TextureArea PRESS_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/press_overlay_1.png");
    public static final TextureArea PRESS_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/press_overlay_2.png");
    public static final TextureArea PRESS_OVERLAY_3 = TextureArea.fullImage("textures/gui/overlay/press_overlay_3.png");
    public static final TextureArea DARK_CANISTER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/dark_canister_overlay.png");
    public static final TextureArea CANISTER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/canister_overlay.png");
    public static final TextureArea LENS_OVERLAY = TextureArea.fullImage("textures/gui/overlay/lens_overlay.png");
    public static final TextureArea CRYSTAL_OVERLAY = TextureArea.fullImage("textures/gui/overlay/crystal_overlay.png");
    public static final TextureArea BREWER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/brewer_overlay.png");
    public static final TextureArea SOLIDIFIER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/solidifier_overlay.png");
    public static final TextureArea MOLECULAR_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/molecular_overlay_1.png");
    public static final TextureArea MOLECULAR_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/molecular_overlay_2.png");
    public static final TextureArea MOLECULAR_OVERLAY_3 = TextureArea.fullImage("textures/gui/overlay/molecular_overlay_3.png");
    public static final TextureArea VIAL_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/vial_overlay_1.png");
    public static final TextureArea VIAL_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/vial_overlay_2.png");
    public static final TextureArea WIREMILL_OVERLAY = TextureArea.fullImage("textures/gui/overlay/wiremill_overlay.png");
    public static final TextureArea BENDER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/bender_overlay.png");
    public static final TextureArea CANNER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/canner_overlay.png");
    public static final TextureArea PIPE_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/pipe_overlay_1.png");
    public static final TextureArea PIPE_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/pipe_overlay_2.png");
    public static final TextureArea BOX_OVERLAY = TextureArea.fullImage("textures/gui/overlay/box_overlay.png");
    public static final TextureArea BOXED_OVERLAY = TextureArea.fullImage("textures/gui/overlay/boxed_overlay.png");
    public static final TextureArea CUTTER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/cutter_overlay.png");
    public static final TextureArea MOLD_OVERLAY = TextureArea.fullImage("textures/gui/overlay/mold_overlay.png");
    public static final TextureArea HAMMER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/hammer_overlay.png");
    public static final TextureArea PAPER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/paper_overlay.png");
    public static final TextureArea PRINTED_PAPER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/printed_paper_overlay.png");
    public static final TextureArea IN_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/in_slot_overlay.png");
    public static final TextureArea OUT_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/out_slot_overlay.png");
    public static final TextureArea FILTER_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/filter_slot_overlay.png");
    public static final TextureArea STRING_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/string_slot_overlay.png");

    //PROGRESS BARS
    public static final TextureArea PROGRESS_BAR_UNLOCK = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_unlock.png");
    public static final TextureArea PROGRESS_BAR_ARROW = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_arrow.png");
    public static final TextureArea PROGRESS_BAR_ARROW_MULTIPLE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_arrow_multiple.png");
    public static final TextureArea PROGRESS_BAR_BATH = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_bath.png");
    public static final TextureArea PROGRESS_BAR_BENDING = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_bending.png");
    public static final TextureArea PROGRESS_BAR_CANNER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_canner.png");
    public static final TextureArea PROGRESS_BAR_CIRCUIT = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_circuit.png");
    public static final TextureArea PROGRESS_BAR_COMPRESS = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_compress.png");
    public static final TextureArea PROGRESS_BAR_EXTRACT = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_extract.png");
    public static final TextureArea PROGRESS_BAR_EXTRUDER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_extruder.png");
    public static final TextureArea PROGRESS_BAR_HAMMER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_hammer.png");
    public static final TextureArea PROGRESS_BAR_LATHE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_lathe.png");
    public static final TextureArea PROGRESS_BAR_MACERATE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_macerate.png");
    public static final TextureArea PROGRESS_BAR_MAGNET = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_magnet.png");
    public static final TextureArea PROGRESS_BAR_MIXER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_mixer.png");
    public static final TextureArea PROGRESS_BAR_RECYCLER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_recycler.png");
    public static final TextureArea PROGRESS_BAR_SIFT = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_sift.png");
    public static final TextureArea PROGRESS_BAR_SLICE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_slice.png");
    public static final TextureArea PROGRESS_BAR_WIREMILL = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_wiremill.png");

    //JEI
    public static final TextureArea MULTIBLOCK_CATEGORY = TextureArea.fullImage("textures/gui/icon/coke_oven.png");
    public static final TextureArea INFO_ICON = TextureArea.fullImage("textures/gui/widget/information.png");

}
