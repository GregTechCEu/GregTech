package gregtech.worldgen;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gregtech.api.util.XSTR;
import gregtech.api.util.math.ChunkPosDimension;
import gregtech.worldgen.generator.ChunkAlignedWorldGenerator;
import gregtech.worldgen.generator.EmptyVein;
import gregtech.worldgen.generator.LayeredVeinGenerator;
import gregtech.worldgen.generator.LayeredVeinSettings;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static gregtech.worldgen.WorldgenModule.DEBUG;
import static gregtech.worldgen.WorldgenModule.isOriginChunk;

public class ChunkAlignedWorldgen implements Runnable {

    private static final int ORIGIN_CHUNK_SEARCH_RADIUS = 2;

    private static final Queue<ChunkPosDimension> chunksToProcess = new ArrayDeque<>();
    private static final Cache<ChunkPosDimension, ChunkAlignedWorldGenerator> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .concurrencyLevel(1)
            .maximumSize(WorldgenConfig.chunkAlignedCacheSize)
            .softValues()
            .build();

    private static final Collection<WorldgenCallback<LayeredVeinSettings>> callbacks = new ArrayList<>();

    private final int chunkX;
    private final int chunkZ;
    private final World world;
    private final int dimension;
    private final String biome;

    public ChunkAlignedWorldgen(int chunkX, int chunkZ, @NotNull World world, int dimension, @NotNull String biome) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.world = world;
        this.dimension = dimension;
        this.biome = biome;
    }

    /**
     * Register a callback to be fired upon successful vein placement
     *
     * @param callback the callback to register
     */
    @SuppressWarnings("unused")
    public static void registerCallback(@NotNull WorldgenCallback<LayeredVeinSettings> callback) {
        callbacks.add(callback);
    }

    /**
     * Clears the worldgen cache
     */
    @ApiStatus.Internal
    public static void clearCache() {
        cache.invalidateAll();
    }

    @Override
    public void run() {
        findOriginChunks();
        processOriginChunks();
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

        for (int x = westBound; x <= eastBound; x++) {
            for (int z = northBound; z <= southBound; z++) {
                if (isOriginChunk(x, z)) {
                    ChunkPosDimension pos = new ChunkPosDimension(x, z, dimension);
                    if (DEBUG) {
                        WorldgenModule.logger.info("Found OriginChunk {}", pos);
                    }
                    chunksToProcess.add(pos);
                }
            }
        }

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("findOriginChunks time {}ns", end - start);
        }
    }

    /**
     * Process the origin chunks to generate
     */
    private void processOriginChunks() {
        while (!chunksToProcess.isEmpty()) {
            ChunkPosDimension pos = chunksToProcess.remove();
            generate(pos);
        }
    }

    /**
     * Generates a vein at the coordinates
     *
     * @param originPos the origin chunk and dimension to generate in
     */
    private void generate(@NotNull ChunkPosDimension originPos) {
        long start = System.nanoTime();
        long seed = originPos.hashCode() * 31L + world.getSeed();

        if (DEBUG) {
            WorldgenModule.logger.info("generating vein at {}", originPos);
        }

        Random random = new XSTR(seed);

        ChunkAlignedWorldGenerator potential = cache.getIfPresent(originPos);
        if (potential == null) {
            findAndGenerateNew(random, originPos, seed);
        } else {
            generateExisting(potential, random, originPos);
        }

        long end = System.nanoTime();
        if (DEBUG) {
            WorldgenModule.logger.info("generateVein time {}ns", end - start);
        }
    }

    /**
     * Generate a new chunk-aligned vein
     *
     * @param random    the random to use
     * @param originPos the origin chunk pos
     * @param seed      the seed for random values
     */
    private void findAndGenerateNew(@NotNull Random random, @NotNull ChunkPosDimension originPos, long seed) {
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
                    for (LayeredVeinSettings settings : collection) {
                        weight -= settings.weight();
                        if (weight > 0) continue;

                        ChunkAlignedWorldGenerator generator = new LayeredVeinGenerator(settings);

                        PlacementResult result = generator.generate(world, new XSTR(seed), biome,
                                dimension, originX * 16, originZ * 16,
                                chunkX * 16, chunkZ * 16);
                        switch (result) {
                            case PLACED, NON_OVERLAPPING -> {
                                if (DEBUG) {
                                    WorldgenModule.logger.info("Placed vein \"{}\", searchAttempts={}, placementAttempts={}, dimension={}",
                                            settings.name(), i, attempts, dimension);
                                }
                                cache.put(originPos, generator);
                                foundVein = true;
                            }
                            case NON_OVERLAPPING_AIR_BLOCK -> {
                                if (DEBUG) {
                                    WorldgenModule.logger.info("No overlap and air in test spot for vein \"{}\", searchAttempts={}, placementAttempts={}, dimension={}",
                                            settings.name(), i, attempts, dimension);
                                }
                                attempts++;
                            }
                            case CANNOT_GEN_IN_BOTTOM -> attempts++;
                        }
                        if (result == PlacementResult.PLACED) {
                            callbacks.forEach(o -> o.receive(originPos, settings));
                        }

                        break;
                    }
                }
                if (!foundVein && this.chunkX == originX && this.chunkZ == originZ) {
                    cache.put(originPos, EmptyVein.INSTANCE);
                }
            }
        } else {
            if (DEBUG) {
                WorldgenModule.logger.info("Skipping vein pos={}, chunkX={}, chunkZ={}, roll={}, abundance={}",
                        originPos, chunkX, chunkZ, roll, WorldgenModule.oreVeinAbundance());
            }
            cache.put(originPos, EmptyVein.INSTANCE);
        }
    }

    private void generateExisting(@NotNull ChunkAlignedWorldGenerator generator, @NotNull Random random,
                                  @NotNull ChunkPosDimension originPos) {
        // generate the existing vein
        PlacementResult result = generator.generate(world, random, biome, originPos.dimension(),
                originPos.x() * 16, originPos.z() * 16,
                chunkX * 16, chunkZ * 16);
        if (result == PlacementResult.NON_OVERLAPPING && DEBUG) {
            WorldgenModule.logger.info("No overlap");
        }
    }
}
