package gregtech.api.sound;

import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GTSounds {
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
    public static SoundEvent SCIENCE;
    public static SoundEvent WRENCH_TOOL;
    public static SoundEvent MORTAR_TOOL;
    public static SoundEvent SOFT_HAMMER_TOOL;
    public static SoundEvent ASSEMBLER;
    public static SoundEvent COMPRESSOR;
    public static SoundEvent REPLICATOR;
    public static SoundEvent CUT;
    public static SoundEvent COOLING;

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
        SCIENCE = registerSound("tick.science");
        WRENCH_TOOL = registerSound("use.wrench");
        MORTAR_TOOL = registerSound("use.mortar");
        SOFT_HAMMER_TOOL = registerSound("use.soft_hammer");
    }

    private static SoundEvent registerSound(String soundNameIn) {
        ResourceLocation location = new ResourceLocation(GTValues.MODID, soundNameIn);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
}
