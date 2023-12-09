package gregtech.worldgen;

import gregtech.worldgen.generator.SporadicSettings;
import gregtech.worldgen.generator.SporadicWorldGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static gregtech.worldgen.WorldgenModule.DEBUG;

public class WorldgenContainer implements Runnable {

    private final int chunkX;
    private final int chunkZ;
    private final World world;
    private final int dimension;
    private final String biome;

    /**
     * A container to handle world generation for a chunk
     *
     * @param chunkX the chunk's x coordinate in chunks
     * @param chunkZ the chunk's z coordinate in chunks
     * @param world  the world the chunk is generating in
     */
    public WorldgenContainer(int chunkX, int chunkZ, @NotNull World world) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.world = world;
        this.dimension = world.provider.getDimension();
        this.biome = world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8)).biomeName;
    }

    @Override
    public void run() {
        long start = System.nanoTime();

        sporadic();

        if (WorldgenModule.STONE_BLOB_REGISTRY.hasGenerators(world.provider.getDimension())) {
            new StoneBlobWorldgen(chunkX, chunkZ, world, dimension, biome).run();
        }

        if (WorldgenModule.CHUNK_ALIGNED_REGISTRY.hasGenerators(world.provider.getDimension())) {
            new ChunkAlignedWorldgen(chunkX, chunkZ, world, dimension, biome).run();
        }

        Chunk chunk = world.getChunk(this.chunkX, this.chunkZ);
        chunk.setModified(true);

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("WorldgenContainer#run time {}ns", end - start);
        }
    }

    /**
     * Handles generation of sporadic generators such as random small ores
     */
    private void sporadic() {
        long start = System.nanoTime();
        var list = WorldgenModule.SPORADIC_REGISTRY.getGenerators(dimension);
        if (list != null) {
            // random is shared between all sporadic generators
            Random random = WorldgenModule.randomManager.persistent(world);

            for (SporadicSettings<?> settings : list) {
                SporadicWorldGenerator generator = settings.createGenerator();
                generator.generate(world, random, biome, dimension, chunkX * 16, chunkZ * 16);
            }
        }

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("sporadic time {}ns", end - start);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorldgenContainer that = (WorldgenContainer) o;

        if (chunkX != that.chunkX) return false;
        return chunkZ == that.chunkZ;
    }

    @Override
    public int hashCode() {
        int result = chunkX;
        result = 31 * result + chunkZ;
        return result;
    }

    @Override
    public String toString() {
        return "WorldgenContainer{" +
                ", chunkX=" + chunkX +
                ", chunkZ=" + chunkZ +
                ", dimension=" + world.provider.getDimension() +
                '}';
    }
}
