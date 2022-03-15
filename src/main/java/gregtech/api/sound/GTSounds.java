package gregtech.api.sound;

import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GTSounds {

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
    public static SoundEvent MALLET_TOOL;

    // Record Sounds
    public static SoundEvent SUS_RECORD;

    //Entity Sounds
    public static SoundEvent PORTAL_OPENING;
    public static SoundEvent PORTAL_CLOSING;

    public static void registerSounds() {
        FORGE_HAMMER = registerSound("tick.forge_hammer");
        MACERATOR = registerSound("tick.macerator");
        CHEMICAL_REACTOR = registerSound("tick.chemical_reactor");
        ASSEMBLER = registerSound("tick.assembler");
        CENTRIFUGE = registerSound("tick.centrifuge");
        COMPRESSOR = registerSound("tick.compressor");
        ELECTROLYZER = registerSound("tick.electrolyzer");
        MIXER = registerSound("tick.mixer");
        REPLICATOR = registerSound("tick.replicator");
        ARC = registerSound("tick.arc");
        BOILER = registerSound("tick.boiler");
        FURNACE = registerSound("tick.furnace");
        COOLING = registerSound("tick.cooling");
        FIRE = registerSound("tick.fire");
        BATH = registerSound("tick.bath");
        MOTOR = registerSound("tick.motor");
        CUT = registerSound("tick.cut");
        TURBINE = registerSound("tick.turbine");
        COMBUSTION = registerSound("tick.combustion");
        MINER = registerSound("tick.miner");
        SCIENCE = registerSound("tick.science");
        WRENCH_TOOL = registerSound("use.wrench");
        MALLET_TOOL = registerSound("use.soft_hammer");
        DRILL_TOOL = registerSound("use.drill");
        PLUNGER_TOOL = registerSound("use.plunger");
        FILE_TOOL = registerSound("use.file");
        SAW_TOOL = registerSound("use.saw");
        SCREWDRIVER_TOOL = registerSound("use.screwdriver");
        CHAINSAW_TOOL = registerSound("use.chainsaw");
        WIRECUTTER_TOOL = registerSound("use.wirecutter");
        SPRAY_CAN_TOOL = registerSound("use.spray_can");
        TRICORDER_TOOL = registerSound("use.tricorder");
        MORTAR_TOOL = registerSound("use.mortar");
        SUS_RECORD = registerSound("record.sus");
        PORTAL_OPENING = registerSound("entity.portal_opening");
        PORTAL_CLOSING = registerSound("entity.portal_closing");
    }

    private static SoundEvent registerSound(String soundNameIn) {
        ResourceLocation location = new ResourceLocation(GTValues.MODID, soundNameIn);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
}
