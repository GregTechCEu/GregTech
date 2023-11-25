package gregtech.worldgen.generator;

import gregtech.worldgen.OrePlacementResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public interface ChunkAlignedWorldGenerator extends GeneratorEntry {

    /**
     * Generate worldgen chunk-aligned.
     *
     * @param world the world to generate in
     * @param random the random to use
     * @param biome the biome being generated in
     * @param dimension the dimension being generated in
     * @param originX the origin chunk X in block coordinates
     * @param originZ the origin chunk Z in block coordinates
     * @param chunkX the current chunk X in block coordinates
     * @param chunkZ the current chunk Z in block coordinates
     * @return the result
     */
    @NotNull OrePlacementResult generate(@NotNull World world, @NotNull Random random, @NotNull String biome,
                                         int dimension, int originX, int originZ, int chunkX, int chunkZ);

    /**
     * @return the name of this generator
     */
    @NotNull String getName();
}
