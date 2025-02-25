package gregtech.api.worldgen.generator;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WorldGeneratorImpl implements IWorldGenerator {

    public static final WorldGeneratorImpl INSTANCE = new WorldGeneratorImpl();

    public static final int GRID_SIZE_X = 3;
    public static final int GRID_SIZE_Z = 3;

    private WorldGeneratorImpl() {}

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                         IChunkProvider chunkProvider) {
        int selfGridX = Math.floorDiv(chunkX, GRID_SIZE_X);
        int selfGridZ = Math.floorDiv(chunkZ, GRID_SIZE_Z);
        generateInternal(world, selfGridX, selfGridZ, chunkX, chunkZ, random);
    }

    private static void generateInternal(World world, int selfGridX, int selfGridZ, int chunkX, int chunkZ,
                                         Random random) {
        int halfSizeX = (GRID_SIZE_X - 1) / 2;
        int halfSizeZ = (GRID_SIZE_Z - 1) / 2;
        for (int gridX = -halfSizeX; gridX <= halfSizeX; gridX++) {
            for (int gridZ = -halfSizeZ; gridZ <= halfSizeZ; gridZ++) {
                CachedGridEntry cachedGridEntry = CachedGridEntry.getOrCreateEntry(world, selfGridX + gridX,
                        selfGridZ + gridZ, chunkX, chunkZ);
                cachedGridEntry.populateChunk(world, chunkX, chunkZ, random);
            }
        }
    }
}
