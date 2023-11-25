package gregtech.worldgen;

import gregtech.api.util.XSTR;
import gregtech.api.util.math.ChunkPosDimension;
import gregtech.worldgen.generator.ChunkAlignedWorldGenerator;
import gregtech.worldgen.generator.EmptyVein;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import static gregtech.worldgen.WorldgenModule.DEBUG;

public class WorldgenContainer implements Runnable {

    private static final int ORIGIN_CHUNK_SEARCH_RADIUS = 2;
    private static final int ORIGIN_CHUNK_DIAMETER = 3;

    private static final Queue<ChunkPosDimension> originChunks = new ArrayDeque<>();
    private static final Map<ChunkPosDimension, ChunkAlignedWorldGenerator> chunkAligned = new Object2ObjectOpenHashMap<>();

    private final Random blobsAndSmallRandom;
    private final int chunkX;
    private final int chunkZ;
    private final World world;
    private final IChunkGenerator chunkGenerator;
    private final IChunkProvider chunkProvider;
    private final String biome;

    public WorldgenContainer(@NotNull Random blobsAndSmallRandom, int chunkX, int chunkZ, @NotNull World world,
                             @NotNull IChunkGenerator chunkGenerator, @NotNull IChunkProvider chunkProvider) {
        this.blobsAndSmallRandom = blobsAndSmallRandom;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.world = world;
        this.chunkGenerator = chunkGenerator;
        this.chunkProvider = chunkProvider;
        this.biome = world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8)).biomeName;
    }

    private static boolean isOriginChunk(int chunkX, int chunkZ) {
        return Math.floorMod(chunkX, ORIGIN_CHUNK_DIAMETER) == 1 && Math.floorMod(chunkZ, ORIGIN_CHUNK_DIAMETER) == 1;
    }

    @Override
    public void run() {
        long start = System.nanoTime();

        stoneBlobs();
        smallOres();

        if (WorldgenModule.CHUNK_ALIGNED_REGISTRY.hasGenerators(world.provider.getDimension())) {
            chunkAligned();
        }

        Chunk chunk = world.getChunk(this.chunkX, this.chunkZ);
        chunk.setModified(true);

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("WorldgenContainer#run time {}", end - start);
        }
    }

    /**
     * Handles generation of stone blobs
     */
    private void stoneBlobs() {
        long start = System.nanoTime();

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("stoneBlobs time {}", end - start);
        }
    }

    /**
     * Handles generation of random small ores
     */
    private void smallOres() {
        long start = System.nanoTime();

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("smallOres time {}", end - start);
        }
    }

    /**
     * Handles chunk-aligned world generation
     */
    private void chunkAligned() {
        long start = System.nanoTime();

        findOriginChunks();
        generateChunkAligned();

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("chunkAligned time {}", end - start);
        }
    }

    /**
     * Find the origin chunks in a box around the container's center chunk
     */
    private void findOriginChunks() {
        long start = System.nanoTime();

        int westBound = chunkX - ORIGIN_CHUNK_SEARCH_RADIUS;
        int eastBound = chunkX + ORIGIN_CHUNK_SEARCH_RADIUS;
        int northBound = chunkZ - ORIGIN_CHUNK_SEARCH_RADIUS;
        int southBound = chunkZ + ORIGIN_CHUNK_SEARCH_RADIUS;

        int dimension = world.provider.getDimension();

        for (int x = westBound; x <= eastBound; x++) {
            for (int z = northBound; z <= southBound; z++) {
                if (isOriginChunk(x, z)) {
                    ChunkPosDimension pos = new ChunkPosDimension(x, z, dimension);
                    if (DEBUG) {
                        WorldgenModule.logger.info("Found OriginChunk {}", pos);
                    }
                    originChunks.add(pos);
                }
            }
        }

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("findOriginChunks time {}", end - start);
        }
    }

    /**
     * Generates things chunk-aligned
     */
    private void generateChunkAligned() {
        while (!originChunks.isEmpty()) {
            ChunkPosDimension pos = originChunks.remove();
            generateChunkAligned(pos);
        }
    }

    /**
     * Generates a vein at the coordinates
     *
     * @param originPos the origin chunk and dimension to generate in
     */
    private void generateChunkAligned(@NotNull ChunkPosDimension originPos) {
        long start = System.nanoTime();
        long seed = originPos.hashCode() * 31 + world.getSeed();

        if (DEBUG) {
            WorldgenModule.logger.info("generating vein at {}", originPos);
        }

        Random random = new XSTR(seed);

        ChunkAlignedWorldGenerator potential = chunkAligned.get(originPos);
        if (potential == null) {
            generateNewChunkAligned(random, originPos, seed);
        } else {
            // generate the existing vein
            OrePlacementResult result = potential.generate(world, random, biome, originPos.dimension(),
                    originPos.x() * 16, originPos.z() * 16,
                    chunkX * 16, chunkZ * 16);
            if (result == OrePlacementResult.NON_OVERLAPPING && DEBUG) {
                WorldgenModule.logger.info("No overlap");
            }
        }

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("generateVein time {}", end - start);
        }
    }

    /**
     * Generate a new chunk-aligned vein
     *
     * @param random the random to use
     * @param originPos the origin chunk pos
     * @param seed the seed for random values
     */
    private void generateNewChunkAligned(@NotNull Random random, @NotNull ChunkPosDimension originPos, long seed) {
        int dimension = originPos.dimension();

        var collection = WorldgenModule.CHUNK_ALIGNED_REGISTRY.getGenerators(dimension);
        if (collection == null) return;

        int originX = originPos.x();
        int originZ = originPos.z();

        int roll = random.nextInt(100);
        if (roll < WorldgenModule.oreVeinAbundance()) {
            int totalWeight = WorldgenModule.CHUNK_ALIGNED_REGISTRY.getTotalWeight(dimension);
            if (totalWeight > 0) {
                int attempts = 0;
                boolean foundVein = false;

                for (int i = 0; i < WorldgenModule.maxOregenSearchAttempts(); i++) {
                    if (foundVein) break;
                    if (attempts >= WorldgenModule.maxOregenPlacementAttempts()) break;

                    int weight = random.nextInt(totalWeight);
                    for (ChunkAlignedWorldGenerator generator : collection) {
                        weight -= generator.getWeight();
                        if (weight > 0) continue;

                        OrePlacementResult result = generator.generate(world, new XSTR(seed), biome,
                                dimension, originX * 16, originZ * 16,
                                chunkX * 16, chunkZ * 16);
                        switch (result) {
                            case ORE_PLACED, NON_OVERLAPPING -> {
                                if (DEBUG) {
                                    WorldgenModule.logger.info("Placed vein \"{}\", searchAttempts={}, placementAttempts={}, dimension={}",
                                            generator.getName(), i, attempts, dimension);
                                }
                                chunkAligned.put(originPos, generator);
                                foundVein = true;
                            }
                            case NON_OVERLAPPING_AIR_BLOCK -> {
                                if (DEBUG) {
                                    WorldgenModule.logger.info("No overlap and air in test spot for vein \"{}\", searchAttempts={}, placementAttempts={}, dimension={}",
                                            generator.getName(), i, attempts, dimension);
                                }
                                attempts++;
                            }
                            case NO_ORE_IN_BOTTOM -> attempts++;
                        }
                        break;
                    }
                }
                if (!foundVein && this.chunkX == originX && this.chunkZ == originZ) {
                    chunkAligned.put(originPos, EmptyVein.INSTANCE);
                }
            }
        } else {
            if (DEBUG) {
                WorldgenModule.logger.info("Skipping vein pos={}, chunkX={}, chunkZ={}, roll={}, abundance={}",
                        originPos, chunkX, chunkZ, roll, WorldgenModule.oreVeinAbundance());
            }
            chunkAligned.put(originPos, EmptyVein.INSTANCE);
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
