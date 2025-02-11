package gregtech.api.util;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.ApiStatus;

/**
 * Utility class caching the current server tick in a slightly more lightweight class, instead of going through
 * FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() every time.
 */
public class TickUtil {

    private static int tick;

    /**
     * Should only be called on {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     * {@link net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START}
     */
    @ApiStatus.Internal
    public static void update() {
        tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() + 1;
    }

    public static int getTick() {
        return tick;
    }
}
