package gregtech.core.sound;

import net.minecraft.util.SoundEvent;

import static gregtech.api.GregTechAPI.soundManager;

public class GTSoundEvents {

    // Machine Sounds
    public static SoundEvent MOTOR;
    public static SoundEvent BATH;
    public static SoundEvent MIXER;
    public static SoundEvent ELECTROLYZER;
    public static SoundEvent CENTRIFUGE;
    public static SoundEvent FORGE_HAMMER;
    public static SoundEvent MACERATOR;
    public static SoundEvent CHEMICAL_REACTOR;
    public static SoundEvent ARC;
    public static SoundEvent BOILER;
    public static SoundEvent FURNACE;
    public static SoundEvent FIRE;
    public static SoundEvent TURBINE;
    public static SoundEvent COMBUSTION;
    public static SoundEvent SCIENCE;
    public static SoundEvent ASSEMBLER;
    public static SoundEvent COMPRESSOR;
    public static SoundEvent REPLICATOR;
    public static SoundEvent CUT;
    public static SoundEvent COOLING;
    public static SoundEvent MINER;
    public static SoundEvent COMPUTATION;

    // Tool Sounds
    public static SoundEvent DRILL_TOOL;
    public static SoundEvent PLUNGER_TOOL;
    public static SoundEvent FILE_TOOL;
    public static SoundEvent SAW_TOOL;
    public static SoundEvent SCREWDRIVER_TOOL;
    public static SoundEvent CHAINSAW_TOOL;
    public static SoundEvent WIRECUTTER_TOOL;
    public static SoundEvent SPRAY_CAN_TOOL;
    public static SoundEvent TRICORDER_TOOL;
    public static SoundEvent WRENCH_TOOL;
    public static SoundEvent MORTAR_TOOL;
    public static SoundEvent SOFT_MALLET_TOOL;

    // Record Sounds
    public static SoundEvent SUS_RECORD;

    // Entity Sounds
    public static SoundEvent PORTAL_OPENING;
    public static SoundEvent PORTAL_CLOSING;

    // Maintenance Sounds
    public static SoundEvent BREAKDOWN_ELECTRICAL;
    public static SoundEvent BREAKDOWN_MECHANICAL;

    // Misc
    public static SoundEvent DEFAULT_ALARM;

    public static void register() {
        FORGE_HAMMER = soundManager.registerSound("tick.forge_hammer");
        MACERATOR = soundManager.registerSound("tick.macerator");
        CHEMICAL_REACTOR = soundManager.registerSound("tick.chemical_reactor");
        ASSEMBLER = soundManager.registerSound("tick.assembler");
        CENTRIFUGE = soundManager.registerSound("tick.centrifuge");
        COMPRESSOR = soundManager.registerSound("tick.compressor");
        ELECTROLYZER = soundManager.registerSound("tick.electrolyzer");
        MIXER = soundManager.registerSound("tick.mixer");
        REPLICATOR = soundManager.registerSound("tick.replicator");
        ARC = soundManager.registerSound("tick.arc");
        BOILER = soundManager.registerSound("tick.boiler");
        FURNACE = soundManager.registerSound("tick.furnace");
        COOLING = soundManager.registerSound("tick.cooling");
        FIRE = soundManager.registerSound("tick.fire");
        BATH = soundManager.registerSound("tick.bath");
        MOTOR = soundManager.registerSound("tick.motor");
        CUT = soundManager.registerSound("tick.cut");
        TURBINE = soundManager.registerSound("tick.turbine");
        COMBUSTION = soundManager.registerSound("tick.combustion");
        MINER = soundManager.registerSound("tick.miner");
        SCIENCE = soundManager.registerSound("tick.science");
        COMPUTATION = soundManager.registerSound("tick.computation");

        WRENCH_TOOL = soundManager.registerSound("use.wrench");
        SOFT_MALLET_TOOL = soundManager.registerSound("use.soft_hammer");
        DRILL_TOOL = soundManager.registerSound("use.drill");
        PLUNGER_TOOL = soundManager.registerSound("use.plunger");
        FILE_TOOL = soundManager.registerSound("use.file");
        SAW_TOOL = soundManager.registerSound("use.saw");
        SCREWDRIVER_TOOL = soundManager.registerSound("use.screwdriver");
        CHAINSAW_TOOL = soundManager.registerSound("use.chainsaw");
        WIRECUTTER_TOOL = soundManager.registerSound("use.wirecutter");
        SPRAY_CAN_TOOL = soundManager.registerSound("use.spray_can");
        TRICORDER_TOOL = soundManager.registerSound("use.tricorder");
        MORTAR_TOOL = soundManager.registerSound("use.mortar");
        SUS_RECORD = soundManager.registerSound("record.sus");
        PORTAL_OPENING = soundManager.registerSound("entity.portal_opening");
        PORTAL_CLOSING = soundManager.registerSound("entity.portal_closing");

        BREAKDOWN_ELECTRICAL = soundManager.registerSound("maintenance.electrical");
        BREAKDOWN_MECHANICAL = soundManager.registerSound("maintenance.mechanical");

        DEFAULT_ALARM = soundManager.registerSound("alarm");
    }
}
