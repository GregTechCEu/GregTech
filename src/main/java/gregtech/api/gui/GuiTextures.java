package gregtech.api.gui;

import gregtech.api.gui.resources.AdoptableTextureArea;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;

public class GuiTextures {

    //GREGTECH
    public static final TextureArea GREGTECH_LOGO = TextureArea.fullImage("textures/gui/icon/gregtech_logo.png");

    //BASE TEXTURES
    public static final TextureArea BACKGROUND = AdoptableTextureArea.fullImage("textures/gui/base/background.png", 176, 166, 3, 3);
    public static final TextureArea BORDERED_BACKGROUND = AdoptableTextureArea.fullImage("textures/gui/base/bordered_background.png", 195, 136, 4, 4);
    public static final TextureArea BOXED_BACKGROUND = AdoptableTextureArea.fullImage("textures/gui/base/boxed_background.png", 256, 174, 11, 11);

    public static final TextureArea DISPLAY = TextureArea.fullImage("textures/gui/base/display.png");
    public static final TextureArea FLUID_SLOT = AdoptableTextureArea.fullImage("textures/gui/base/fluid_slot.png", 18, 18, 1, 1);
    public static final TextureArea FLUID_TANK_BACKGROUND = TextureArea.fullImage("textures/gui/base/fluid_tank_background.png");
    public static final TextureArea FLUID_TANK_OVERLAY = TextureArea.fullImage("textures/gui/base/fluid_tank_overlay.png");
    public static final TextureArea SLOT = AdoptableTextureArea.fullImage("textures/gui/base/slot.png", 18, 18, 1, 1);
    public static final TextureArea SLOT_DARKENED = TextureArea.fullImage("textures/gui/base/darkened_slot.png");

    //FLUID & ITEM OUTPUT BUTTONS
    public static final TextureArea BUTTON = TextureArea.fullImage("textures/gui/widget/button.png");
    public static final TextureArea BUTTON_ALLOW_IMPORT_EXPORT = TextureArea.fullImage("textures/gui/widget/button_allow_import_export.png");
    public static final TextureArea BUTTON_BLACKLIST = TextureArea.fullImage("textures/gui/widget/button_blacklist.png");
    public static final TextureArea BUTTON_CLEAR_GRID = TextureArea.fullImage("textures/gui/widget/button_clear_grid.png");
    public static final TextureArea BUTTON_FILTER_DAMAGE = TextureArea.fullImage("textures/gui/widget/button_filter_damage.png");
    public static final TextureArea BUTTON_FILTER_NBT = TextureArea.fullImage("textures/gui/widget/button_filter_nbt.png");
    public static final TextureArea BUTTON_FLUID_OUTPUT = TextureArea.fullImage("textures/gui/widget/button_fluid_output.png");
    public static final TextureArea BUTTON_ITEM_OUTPUT = TextureArea.fullImage("textures/gui/widget/button_item_output.png");
    public static final TextureArea BUTTON_OVERCLOCK = TextureArea.fullImage("textures/gui/widget/button_overclock.png");
    public static final TextureArea BUTTON_SWITCH_VIEW = TextureArea.fullImage("textures/gui/widget/button_switch_view.png");
    public static final TextureArea LOCK = TextureArea.fullImage("textures/gui/widget/lock.png");
    public static final TextureArea SWITCH = TextureArea.fullImage("textures/gui/widget/switch.png");
    public static final TextureArea SWITCH_HORIZONTAL = TextureArea.fullImage("textures/gui/widget/switch_horizontal.png");
    public static final SizedTextureArea VANILLA_BUTTON = SizedTextureArea.fullImage("textures/gui/widget/vanilla_button.png", 200, 40);

    //INDICATORS & ICONS
    public static final TextureArea INDICATOR_NO_ENERGY = TextureArea.fullImage("textures/gui/base/indicator_no_energy.png");
    public static final TextureArea TANK_ICON = TextureArea.fullImage("textures/gui/base/tank_icon.png");

    //WIDGET UI RELATED
    public static final TextureArea SLIDER_BACKGROUND = TextureArea.fullImage("textures/gui/widget/slider_background.png");
    public static final TextureArea SLIDER_ICON = TextureArea.fullImage("textures/gui/widget/slider.png");

    //BRONZE
    public static final TextureArea BRONZE_BACKGROUND = TextureArea.fullImage("textures/gui/steam/bronze/bronze_gui.png");
    public static final TextureArea BRONZE_BLAST_FURNACE_PROGRESS_BAR = TextureArea.fullImage("textures/gui/steam/bronze/progress_bar_bronze_blast_furnace.png");
    public static final TextureArea BRONZE_COAL_OVERLAY = TextureArea.fullImage("textures/gui/steam/bronze/overlay_bronze_coal.png");
    public static final TextureArea BRONZE_DUST_OVERLAY = TextureArea.fullImage("textures/gui/steam/bronze/overlay_bronze_dust.png");
    public static final TextureArea BRONZE_FURNACE_OVERLAY = TextureArea.fullImage("textures/gui/steam/bronze/slot_bronze_furnace_background.png");
    public static final TextureArea BRONZE_INGOT_OVERLAY = TextureArea.fullImage("textures/gui/steam/bronze/overlay_bronze_ingot.png");
    public static final TextureArea BRONZE_SLOT = TextureArea.fullImage("textures/gui/steam/bronze/slot_bronze.png");

    //STEEL
    public static final TextureArea STEEL_BACKGROUND = TextureArea.fullImage("textures/gui/steam/steel/steel_gui.png");
    public static final TextureArea STEEL_COAL_OVERLAY = TextureArea.fullImage("textures/gui/steam/steel/overlay_steel_coal.png");
    public static final TextureArea STEEL_DUST_OVERLAY = TextureArea.fullImage("textures/gui/steam/steel/overlay_steel_dust.png");
    public static final TextureArea STEEL_FURNACE_OVERLAY = TextureArea.fullImage("textures/gui/steam/steel/slot_steel_furnace_background.png");
    public static final TextureArea STEEL_SLOT = TextureArea.fullImage("textures/gui/steam/steel/slot_steel.png");

    //SLOT OVERLAYS
    public static final TextureArea ATOMIC_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/atomic_overlay_1.png");
    public static final TextureArea ATOMIC_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/atomic_overlay_2.png");
    public static final TextureArea ARROW_INPUT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/arrow_input_overlay.png");
    public static final TextureArea ARROW_OUTPUT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/arrow_output_overlay.png");
    public static final TextureArea BATTERY_OVERLAY = TextureArea.fullImage("textures/gui/overlay/battery_overlay.png");
    public static final TextureArea BEAKER_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/beaker_overlay_1.png");
    public static final TextureArea BEAKER_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/beaker_overlay_2.png");
    public static final TextureArea BEAKER_OVERLAY_3 = TextureArea.fullImage("textures/gui/overlay/beaker_overlay_3.png");
    public static final TextureArea BEAKER_OVERLAY_4 = TextureArea.fullImage("textures/gui/overlay/beaker_overlay_4.png");
    public static final TextureArea BENDER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/bender_overlay.png");
    public static final TextureArea BOX_OVERLAY = TextureArea.fullImage("textures/gui/overlay/box_overlay.png");
    public static final TextureArea BOXED_OVERLAY = TextureArea.fullImage("textures/gui/overlay/boxed_overlay.png");
    public static final TextureArea BREWER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/brewer_overlay.png");
    public static final TextureArea CANNER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/canner_overlay.png");
    public static final TextureArea CHARGER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/charger_slot_overlay.png");
    public static final TextureArea CANISTER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/canister_overlay.png");
    public static final TextureArea CENTRIFUGE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/centrifuge_overlay.png");
    public static final TextureArea CIRCUIT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/circuit_overlay.png");
    public static final TextureArea COMPRESSOR_OVERLAY = TextureArea.fullImage("textures/gui/overlay/compressor_overlay.png");
    public static final TextureArea CRACKING_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/cracking_overlay_1.png");
    public static final TextureArea CRACKING_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/cracking_overlay_2.png");
    public static final TextureArea CRUSHED_ORE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/crushed_ore_overlay.png");
    public static final TextureArea CRYSTAL_OVERLAY = TextureArea.fullImage("textures/gui/overlay/crystal_overlay.png");
    public static final TextureArea CUTTER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/cutter_overlay.png");
    public static final TextureArea DARK_CANISTER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/dark_canister_overlay.png");
    public static final TextureArea DUST_OVERLAY = TextureArea.fullImage("textures/gui/overlay/dust_overlay.png");
    public static final TextureArea EXTRACTOR_OVERLAY = TextureArea.fullImage("textures/gui/overlay/extractor_overlay.png");
    public static final TextureArea FILTER_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/filter_slot_overlay.png");
    public static final TextureArea FURNACE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/furnace_overlay.png");
    public static final TextureArea HAMMER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/hammer_overlay.png");
    public static final TextureArea HEATING_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/heating_overlay_1.png");
    public static final TextureArea HEATING_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/heating_overlay_2.png");
    public static final TextureArea IMPLOSION_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/implosion_overlay_1.png");
    public static final TextureArea IMPLOSION_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/implosion_overlay_2.png");
    public static final TextureArea IN_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/in_slot_overlay.png");
    public static final TextureArea INGOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/ingot_overlay.png");
    public static final TextureArea INT_CIRCUIT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/int_circuit_overlay.png");
    public static final TextureArea LENS_OVERLAY = TextureArea.fullImage("textures/gui/overlay/lens_overlay.png");
    public static final TextureArea LIGHTNING_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/lightning_overlay_1.png");
    public static final TextureArea LIGHTNING_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/lightning_overlay_2.png");
    public static final TextureArea MOLD_OVERLAY = TextureArea.fullImage("textures/gui/overlay/mold_overlay.png");
    public static final TextureArea MOLECULAR_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/molecular_overlay_1.png");
    public static final TextureArea MOLECULAR_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/molecular_overlay_2.png");
    public static final TextureArea MOLECULAR_OVERLAY_3 = TextureArea.fullImage("textures/gui/overlay/molecular_overlay_3.png");
    public static final TextureArea MOLECULAR_OVERLAY_4 = TextureArea.fullImage("textures/gui/overlay/molecular_overlay_4.png");
    public static final TextureArea OUT_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/out_slot_overlay.png");
    public static final TextureArea PAPER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/paper_overlay.png");
    public static final TextureArea PRINTED_PAPER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/printed_paper_overlay.png");
    public static final TextureArea PIPE_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/pipe_overlay_2.png");
    public static final TextureArea PIPE_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/pipe_overlay_1.png");
    public static final TextureArea PRESS_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/press_overlay_1.png");
    public static final TextureArea PRESS_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/press_overlay_2.png");
    public static final TextureArea PRESS_OVERLAY_3 = TextureArea.fullImage("textures/gui/overlay/press_overlay_3.png");
    public static final TextureArea PRESS_OVERLAY_4 = TextureArea.fullImage("textures/gui/overlay/press_overlay_4.png");
    public static final TextureArea SAWBLADE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/sawblade_overlay.png");
    public static final TextureArea SOLIDIFIER_OVERLAY = TextureArea.fullImage("textures/gui/overlay/solidifier_overlay.png");
    public static final TextureArea STRING_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/string_slot_overlay.png");
    public static final TextureArea TOOL_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/tool_slot_overlay.png");
    public static final TextureArea TURBINE_OVERLAY = TextureArea.fullImage("textures/gui/overlay/turbine_overlay.png");
    public static final TextureArea UUA_SLOT_OVERLAY = TextureArea.fullImage("textures/gui/overlay/uua_overlay.png");
    public static final TextureArea VIAL_OVERLAY_1 = TextureArea.fullImage("textures/gui/overlay/vial_overlay_1.png");
    public static final TextureArea VIAL_OVERLAY_2 = TextureArea.fullImage("textures/gui/overlay/vial_overlay_2.png");
    public static final TextureArea WIREMILL_OVERLAY = TextureArea.fullImage("textures/gui/overlay/wiremill_overlay.png");

    //PROGRESS BARS
    public static final TextureArea PROGRESS_BAR_ARC_FURNACE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_arc_furnace.png");
    public static final TextureArea PROGRESS_BAR_ARROW = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_arrow.png");
    public static final TextureArea PROGRESS_BAR_ARROW_MULTIPLE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_arrow_multiple.png");
    public static final TextureArea PROGRESS_BAR_BATH = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_bath.png");
    public static final TextureArea PROGRESS_BAR_BENDING = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_bending.png");
    public static final TextureArea PROGRESS_BAR_CANNER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_canner.png");
    public static final TextureArea PROGRESS_BAR_CIRCUIT = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_circuit.png");
    public static final TextureArea PROGRESS_BAR_CIRCUIT_ASSEMBLER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_circuit_assembler.png");
    public static final TextureArea PROGRESS_BAR_COMPRESS = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_compress.png");
    public static final TextureArea PROGRESS_BAR_CRACKING = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_cracking.png");
    public static final TextureArea PROGRESS_BAR_CRYSTALLIZATION = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_crystallization.png");
    public static final TextureArea PROGRESS_BAR_EXTRACT = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_extract.png");
    public static final TextureArea PROGRESS_BAR_EXTRUDER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_extruder.png");
    public static final TextureArea PROGRESS_BAR_FUSION = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_fusion.png");
    public static final TextureArea PROGRESS_BAR_HAMMER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_hammer.png");
    public static final TextureArea PROGRESS_BAR_HAMMER_BASE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_hammer_base.png");
    public static final TextureArea PROGRESS_BAR_LATHE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_lathe.png");
    public static final TextureArea PROGRESS_BAR_LATHE_BASE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_lathe_base.png");
    public static final TextureArea PROGRESS_BAR_MACERATE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_macerate.png");
    public static final TextureArea PROGRESS_BAR_MAGNET = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_magnet.png");
    public static final TextureArea PROGRESS_BAR_MASS_FAB = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_mass_fab.png");
    public static final TextureArea PROGRESS_BAR_MIXER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_mixer.png");
    public static final TextureArea PROGRESS_BAR_PACKER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_packer.png");
    public static final TextureArea PROGRESS_BAR_RECYCLER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_recycler.png");
    public static final TextureArea PROGRESS_BAR_REPLICATOR = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_replicator.png");
    public static final TextureArea PROGRESS_BAR_SIFT = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_sift.png");
    public static final TextureArea PROGRESS_BAR_SLICE = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_slice.png");
    public static final TextureArea PROGRESS_BAR_UNLOCK = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_unlock.png");
    public static final TextureArea PROGRESS_BAR_UNPACKER = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_unpacker.png");
    public static final TextureArea PROGRESS_BAR_WIREMILL = TextureArea.fullImage("textures/gui/progress_bar/progress_bar_wiremill.png");

    //JEI
    public static final TextureArea INFO_ICON = TextureArea.fullImage("textures/gui/widget/information.png");
    public static final TextureArea MULTIBLOCK_CATEGORY = TextureArea.fullImage("textures/gui/icon/coke_oven.png");

}
