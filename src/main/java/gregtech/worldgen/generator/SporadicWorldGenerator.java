package gregtech.worldgen.generator;

import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public interface SporadicWorldGenerator extends WorldGenerator {

    /**
     * Generate worldgen chunk-aligned.
     *
     * @param world the world to generate in
     * @param random the random to use
     * @param biome the biome being generated in
     * @param dimension the dimension being generated in
     * @param chunkX the current chunk X in block coordinates
     * @param chunkZ the current chunk Z in block coordinates
     */
    void generate(@NotNull World world, @NotNull Random random, @NotNull String biome, int dimension, int chunkX, int chunkZ);
}
