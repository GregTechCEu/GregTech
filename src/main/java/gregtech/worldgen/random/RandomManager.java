package gregtech.worldgen.random;

import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public interface RandomManager {

    /**
     * Do not call {@link Random#setSeed(long)} on the returned object.
     *
     * @return a random which persists state across retrievals.
     */
    @NotNull Random persistent(@NotNull World world);

    /**
     * Is safe to call {@link Random#setSeed(long)} on the returned object.
     * <p>
     * Do not store references to the return value with lifetimes beyond function scope, the seed can change.
     * <p>
     * Operates off a shared instance where possible to save memory. Calling this method multiple times on the same
     * thread will not produce different objects.
     *
     * @param seed a seed for the random
     * @return a random with the provided seed
     */
    @NotNull Random seeded(long seed);

    /**
     * Called when a dimension is unloaded
     *
     * @param world the world which is unloading
     */
    default void onDimensionUnload(@NotNull World world) {}
}
