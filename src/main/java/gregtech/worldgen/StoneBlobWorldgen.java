package gregtech.worldgen;

import gregtech.api.util.XSTR;
import gregtech.api.util.math.ChunkPosDimension;
import gregtech.worldgen.generator.impl.StoneBlob;
import gregtech.worldgen.generator.impl.StoneBlobGenerator;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class StoneBlobWorldgen implements Runnable {

    private static final Queue<ChunkPosDimension> blobChunks = new ArrayDeque<>();
    private static final Object2BooleanMap<ChunkPosDimension> blobs = new Object2BooleanOpenHashMap<>();

    private final int chunkX;
    private final int chunkZ;
    private final World world;
    private final int dimension;
    private final String biome;

    public StoneBlobWorldgen(int chunkX, int chunkZ, @NotNull World world, int dimension, @NotNull String biome) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.world = world;
        this.dimension = dimension;
        this.biome = biome;
    }

    @Override
    public void run() {
        List<StoneBlob> list = WorldgenModule.STONE_BLOB_REGISTRY.getGenerators(dimension);
        if (list == null) return;

        Random random = new XSTR();
        for (StoneBlob blob : list) {
            double realSize = findOriginChunks(blob, random);
            processOriginChunks(blob, random, realSize);
        }
    }

    private double findOriginChunks(@NotNull StoneBlob blob, @NotNull Random random) {
        double realSize = blob.size() / 16.0f;
        int windowWidth = (int) realSize / 16 + 1;

        int minX = chunkX - windowWidth;
        int maxX = chunkX + windowWidth + 1;
        int minZ = chunkZ - windowWidth;
        int maxZ = chunkZ + windowWidth + 1;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                ChunkPosDimension pos = new ChunkPosDimension(x, z, dimension);
                if (blobs.containsKey(pos)) {
                    if (blobs.get(pos)) {
                        blobChunks.add(pos);
                    }
                } else {
                    random.setSeed(WorldgenUtil.getRandomSeed(world, pos));
                    if (random.nextInt(blob.weight()) == 0) {
                        blobs.put(pos, true);
                        blobChunks.add(pos);
                    } else {
                        blobs.put(pos, false);
                    }
                }
            }
        }
        return realSize;
    }

    private void processOriginChunks(@NotNull StoneBlob blob, @NotNull Random random, double realSize) {
        while (!blobChunks.isEmpty()) {
            ChunkPosDimension pos = blobChunks.remove();
            generate(blob, pos, random, realSize);
        }
    }

    private void generate(@NotNull StoneBlob blob, @NotNull ChunkPosDimension originPos, @NotNull Random random,
                          double realSize) {
        random.setSeed(WorldgenUtil.getRandomSeed(world, originPos));
        PlacementResult result =  new StoneBlobGenerator(blob).generate(world, random, biome, dimension, chunkX * 16, chunkZ * 16,
                originPos.x() * 16, originPos.z() * 16, realSize);
        if (result == PlacementResult.NON_OVERLAPPING_AIR_BLOCK) {
            blobs.put(originPos, false);
        }
    }
}
