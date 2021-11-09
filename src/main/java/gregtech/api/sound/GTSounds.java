package gregtech.api.sound;

import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GTSounds {
    public static SoundEvent FORGE_HAMMER;
    public static SoundEvent MACERATOR;
    public static SoundEvent WRENCH_TOOL;

    public static void registerSounds() {
        FORGE_HAMMER = registerSound("tick.forge_hammer");
        MACERATOR = registerSound("tick.macerator");
        WRENCH_TOOL = registerSound("use.wrench");
    }

    private static SoundEvent registerSound(String soundNameIn) {
        ResourceLocation location = new ResourceLocation(GTValues.MODID, soundNameIn);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
}
