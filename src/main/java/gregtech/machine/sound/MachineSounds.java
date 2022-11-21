package gregtech.machine.sound;

import net.minecraft.util.SoundEvent;

import static gregtech.api.GregTechAPI.soundManager;

public class MachineSounds {

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

    public static void register() {
        MOTOR = soundManager.registerSound("tick.motor");
        BATH = soundManager.registerSound("tick.bath");
        MIXER = soundManager.registerSound("tick.mixer");
        ELECTROLYZER = soundManager.registerSound("tick.electrolyzer");
        CENTRIFUGE = soundManager.registerSound("tick.centrifuge");
        FORGE_HAMMER = soundManager.registerSound("tick.forge_hammer");
        MACERATOR = soundManager.registerSound("tick.macerator");
        CHEMICAL_REACTOR = soundManager.registerSound("tick.chemical_reactor");
        ARC = soundManager.registerSound("tick.arc");
        BOILER = soundManager.registerSound("tick.boiler");
        FURNACE = soundManager.registerSound("tick.furnace");
        FIRE = soundManager.registerSound("tick.fire");
        TURBINE = soundManager.registerSound("tick.turbine");
        COMBUSTION = soundManager.registerSound("tick.combustion");
        SCIENCE = soundManager.registerSound("tick.science");
        ASSEMBLER = soundManager.registerSound("tick.assembler");
        COMPRESSOR = soundManager.registerSound("tick.compressor");
        REPLICATOR = soundManager.registerSound("tick.replicator");
        CUT = soundManager.registerSound("tick.cut");
        COOLING = soundManager.registerSound("tick.cooling");
        MINER = soundManager.registerSound("tick.miner");
    }
}
