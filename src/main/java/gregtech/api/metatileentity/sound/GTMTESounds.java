package gregtech.api.metatileentity.sound;

import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GTMTESounds {
    public static SoundEvent FORGE_HAMMER;
    public static SoundEvent MACERATOR;

    public static void registerSounds() {
        FORGE_HAMMER = registerSound("tick.forge_hammer");
        MACERATOR = registerSound("tick.macerator");
    }

    private static SoundEvent registerSound(String soundNameIn) {
        ResourceLocation location = new ResourceLocation(GTValues.MODID, soundNameIn);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
}
