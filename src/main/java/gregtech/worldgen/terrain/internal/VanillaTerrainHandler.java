package gregtech.worldgen.terrain.internal;

import gregtech.common.ConfigHolder;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

public final class VanillaTerrainHandler {

    private VanillaTerrainHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onOreGenerate(@Nonnull OreGenEvent.GenerateMinable event) {
        // clean up vanilla's patches of things underground, for dimensions GT does terrain gen for
        if (ConfigHolder.worldgen.disableVanillaBlobs && GTTerrainGenManager.isDimensionAllowed(event.getWorld().provider.getDimension())) {
            OreGenEvent.GenerateMinable.EventType eventType = event.getType();
            switch (eventType) {
                case DIRT: case GRAVEL: case DIORITE: case GRANITE: case ANDESITE: case SILVERFISH: {
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }
}
