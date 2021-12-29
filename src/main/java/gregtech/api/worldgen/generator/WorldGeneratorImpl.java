package gregtech.api.worldgen.generator;

import gregtech.common.ConfigHolder;
import gregtech.common.worldgen.WorldGenRubberTree;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.*;

public class WorldGeneratorImpl implements IWorldGenerator {

    private static final List<EventType> ORE_EVENT_TYPES = Arrays.asList(
            COAL, DIAMOND, GOLD, IRON, LAPIS, REDSTONE, QUARTZ, EMERALD);
    public static final int GRID_SIZE_X = 3;
    public static final int GRID_SIZE_Z = 3;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onOreGenerate(OreGenEvent.GenerateMinable event) {
        EventType eventType = event.getType();
        if (ConfigHolder.worldgen.disableVanillaOres &&
                ORE_EVENT_TYPES.contains(eventType)) {
            event.setResult(Result.DENY);
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        int selfGridX = Math.floorDiv(chunkX, GRID_SIZE_X);
        int selfGridZ = Math.floorDiv(chunkZ, GRID_SIZE_Z);
        generateInternal(world, selfGridX, selfGridZ, chunkX, chunkZ, random);

        long rubberTreeSeed = random.nextLong();
        if (!ConfigHolder.worldgen.disableRubberTreeGeneration) {
            generateRubberTree(random, rubberTreeSeed, chunkProvider.provideChunk(chunkX, chunkZ), 1.0f); // Hook in Config here for tree rarity if desired
        }
    }

    private void generateInternal(World world, int selfGridX, int selfGridZ, int chunkX, int chunkZ, Random random) {
        int halfSizeX = (GRID_SIZE_X - 1) / 2;
        int halfSizeZ = (GRID_SIZE_Z - 1) / 2;
        for (int gridX = -halfSizeX; gridX <= halfSizeX; gridX++) {
            for (int gridZ = -halfSizeZ; gridZ <= halfSizeZ; gridZ++) {
                CachedGridEntry cachedGridEntry = CachedGridEntry.getOrCreateEntry(world, selfGridX + gridX, selfGridZ + gridZ, chunkX, chunkZ);
                cachedGridEntry.populateChunk(world, chunkX, chunkZ, random);
            }
        }
    }

    private static void generateRubberTree(Random random, long seed, Chunk chunk, float baseScale) {
        random.setSeed(seed);
        Biome[] biomes = new Biome[4];
        for (int i = 0; i < 4; i++) {
            int x = chunk.x * 16 + 8 + (i & 0x1) * 15;
            int z = chunk.z * 16 + 8 + ((i & 0x2) >>> 1) * 15;
            BlockPos pos = new BlockPos(x, chunk.getWorld().getSeaLevel(), z);
            biomes[i] = chunk.getWorld().getBiomeProvider().getBiome(pos, Biomes.PLAINS);
        }
        int rubberTrees = 0;
        for (Biome biome : biomes) {
            if (biome != null) {
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP))
                    rubberTrees += random.nextInt(10) + 5;
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE))
                    rubberTrees += random.nextInt(5) + 1;
            }
        }
        rubberTrees = Math.round(rubberTrees * baseScale);
        rubberTrees /= 2;
        if (rubberTrees > 0 && random.nextInt(100) < rubberTrees) {
            WorldGenRubberTree gen = new WorldGenRubberTree(false);
            for (int j = 0; j < rubberTrees; j++) {
                if (!gen.generate(chunk.getWorld(), random, new BlockPos(
                        chunk.x * 16 + random.nextInt(16),
                        chunk.getWorld().getSeaLevel(),
                        chunk.z * 16 + random.nextInt(16))))
                    rubberTrees -= 3;
            }
        }
    }
}
