package gregtech.worldgen.config.internal;

import gregtech.worldgen.config.WorldgenDefaultsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

public final class GTWorldgenDefaults {

    private GTWorldgenDefaults() {}

    public static void init() {
        MinecraftForge.TERRAIN_GEN_BUS.register(GTWorldgenDefaults.class);
    }

    @SubscribeEvent
    public static void registerDefaults(@Nonnull WorldgenDefaultsEvent event) {
        if (event.getType() == WorldgenDefaultsEvent.Type.TERRAIN_GEN) {
            registerDefaultTerrainGen();
        } else {
            throw new UnsupportedOperationException("Worldgen Default Event type " + event.getType() + " is unsupported.");
        }
    }

    private static void registerDefaultTerrainGen() {
        // TODO determine defaults
    }
}
