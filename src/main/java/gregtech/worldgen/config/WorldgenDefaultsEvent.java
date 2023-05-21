package gregtech.worldgen.config;

import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;

/**
 * Event fired when GT worldgen defaults should be registered.
 * <p>
 * Use {@link gregtech.worldgen.terrain.config.TerrainGenDefaults} to register terrain generation entries
 */
public final class WorldgenDefaultsEvent extends Event {

    private final Type type;

    public WorldgenDefaultsEvent(@Nonnull Type type) {
        this.type = type;
    }

    @Nonnull
    public Type getType() {
        return this.type;
    }

    public static enum Type {
        /** <strong>Currently Unused.</strong> */
        ORE_GEN,
        /** <strong>Currently Unused.</strong> */
        BEDROCK_FLUID,
        TERRAIN_GEN
    }
}
