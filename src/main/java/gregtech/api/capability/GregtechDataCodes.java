package gregtech.api.capability;

public class GregtechDataCodes {

    // MTE implementation update codes
    public static final int INITIALIZE_MTE = -1;
    public static final int UPDATE_FRONT_FACING = -2;
    public static final int UPDATE_PAINTING_COLOR = -3;
    public static final int SYNC_MTE_TRAITS = -4;
    public static final int COVER_ATTACHED_MTE = -5;
    public static final int COVER_REMOVED_MTE = -6;
    public static final int UPDATE_COVER_DATA_MTE = -7;
    public static final int UPDATE_IS_FRAGILE = -8;
    public static final int UPDATE_SOUND_MUFFLED = -9;

    public static final int UPDATE_OUTPUT_FACING = 100;
    public static final int UPDATE_AUTO_OUTPUT_ITEMS = 101;
    public static final int UPDATE_AUTO_OUTPUT_FLUIDS = 102;

    // Drum
    public static final int UPDATE_AUTO_OUTPUT = 560;

    // Safe
    public static final int UPDATE_LOCKED_STATE = 8;
    public static final int UPDATE_CONTENTS_SEED = 9;

    // Steam Machines
    public static final int NEEDS_VENTING = 2;
    public static final int VENTING_SIDE = 3;
    public static final int VENTING_STUCK = 4;
    public static final int BOILER_HEAT = 15;
    public static final int BOILER_LAST_TICK_STEAM = 16;

    // Misc TEs (Transformer, World Accelerator)
    public static final int SYNC_TILE_MODE = 500;

    // Clipboard
    public static final int CREATE_FAKE_UI = 1;
    public static final int MOUSE_POSITION = 2;
    public static final int INIT_CLIPBOARD_NBT = 3;

    public static final int UPDATE_UI = 10; // 10-36
    // Pump
    public static final int PUMP_HEAD_LEVEL = 200;

    // Item Collector, Magic Energy Absorber, Large Boiler, Steam Oven
    public static final int IS_WORKING = 100;

    // Adjustable Transformer, Adjustable Energy Hatch, Diode
    public static final int AMP_INDEX = 101;

    // Pipe implementation update codes
    public static final int UPDATE_INSULATION_COLOR = -1;
    public static final int UPDATE_CONNECTIONS = -2;
    public static final int SYNC_COVER_IMPLEMENTATION = -3;
    public static final int UPDATE_PIPE_TYPE = -4;
    public static final int UPDATE_PIPE_MATERIAL = -5;
    public static final int UPDATE_BLOCKED_CONNECTIONS = -6;
    public static final int UPDATE_FRAME_MATERIAL = -7;
    public static final int UPDATE_COVER_DATA_PIPE = 0;
    public static final int COVER_ATTACHED_PIPE = 1;
    public static final int COVER_REMOVED_PIPE = 2;
    public static final int CABLE_UPDATE_TEMP = 3;

    // Multiblock implementation update codes
    public static final int SYNC_CONTROLLER = 100;
    public static final int IS_ROTOR_LOOPING = 200;
    public static final int FRONT_FACE_FREE = 201;
    public static final int UPDATE_ROTOR_COLOR = 201;
    public static final int UPDATE_STRUCTURE_SIZE = 202;
    public static final int STRUCTURE_FORMED = 400;
    public static final int VARIANT_RENDER_UPDATE = 410;
    public static final int IS_TAPED = 550;
    public static final int STORE_MAINTENANCE = 551;
    public static final int STORE_TAPED = 552;
    public static final int RECIPE_MAP_INDEX = 553;
    public static final int IS_FRONT_FACE_FREE = 554;
    public static final int MAINTENANCE_MULTIPLIER = 555;

    // Fusion Reactor
    public static final int UPDATE_COLOR = 371;

    // Central Monitor
    public static final int UPDATE_ALL = 1;
    public static final int UPDATE_COVERS = 2;
    public static final int UPDATE_HEIGHT = 3;
    public static final int UPDATE_ACTIVE = 4;
    public static final int UPDATE_PLUGIN_ITEM = 3;

    // Central Monitor Plugin
    public static final int UPDATE_PLUGIN_DATA = 2;
    public static final int UPDATE_PLUGIN_CONFIG = 0;
    public static final int ACTION_PLUGIN_CONFIG = 0;
    public static final int UPDATE_PLUGIN_CLICK = -2;
    public static final int UPDATE_ADVANCED_VALID_POS = 1;
    public static final int UPDATE_FAKE_GUI = 1;
    public static final int ACTION_FAKE_GUI = 1;
    public static final int UPDATE_FAKE_GUI_DETECT = -1;

    // Digital Interface
    public static final int UPDATE_MODE = 1;
    public static final int UPDATE_FLUID = 2;
    public static final int UPDATE_ITEM = 3;
    public static final int UPDATE_ENERGY = 4;
    public static final int UPDATE_ENERGY_PER = 5;
    public static final int UPDATE_MACHINE = 6;

    // Phantom Tanks
    public static final int REMOVE_PHANTOM_FLUID_TYPE = 10;
    public static final int CHANGE_PHANTOM_FLUID = 11;
    public static final int VOID_PHANTOM_FLUID = 12;
    public static final int LOAD_PHANTOM_FLUID_STACK_FROM_NBT = 13;

    // Recipe Logic
    public static final int WORKABLE_ACTIVE = 1;
    public static final int WORKING_ENABLED = 5;

}
