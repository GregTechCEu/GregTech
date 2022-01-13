package gregtech.api.worldgen.generator;

import com.google.common.collect.ImmutableSet;
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

import java.util.Random;
import java.util.Set;

import static net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.*;

public class WorldGeneratorImpl implements IWorldGenerator {

    public static final WorldGeneratorImpl INSTANCE = new WorldGeneratorImpl();

    private static final Set<EventType> ORE_EVENT_TYPES = ImmutableSet.of(COAL, DIAMOND, GOLD, IRON, LAPIS, REDSTONE, QUARTZ, EMERALD);
    public static final int GRID_SIZE_X = 3;
    public static final int GRID_SIZE_Z = 3;

    private WorldGeneratorImpl() { }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onOreGenerate(OreGenEvent.GenerateMinable event) {
        EventType eventType = event.getType();
        if (ConfigHolder.worldgen.disableVanillaOres && ORE_EVENT_TYPES.contains(eventType)) {
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
            generateRubberTree(random, rubberTreeSeed, chunkProvider.provideChunk(chunkX, chunkZ), ConfigHolder.worldgen.rubberTreeRateIncrease);
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

    private static void generateRubberTree(Random random, long seed, Chunk chunk, double baseScale) {
        random.setSeed(seed);
        Biome[] biomes = new Biome[4];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int seaLevel = chunk.getWorld().getSeaLevel();
        for (int i = 0; i < 4; i++) {
            int x = chunk.x * 16 + 8 + (i & 0x1) * 15;
            int z = chunk.z * 16 + 8 + ((i & 0x2) >>> 1) * 15;
            biomes[i] = chunk.getWorld().getBiomeProvider().getBiome(pos.setPos(x, seaLevel, z), Biomes.PLAINS);
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
        rubberTrees = (int) Math.round(rubberTrees * baseScale);
        rubberTrees /= 2;
        if (rubberTrees > 0 && random.nextInt(100) < rubberTrees) {
            for (int j = 0; j < rubberTrees; j++) {
                pos.setPos(chunk.x * 16 + random.nextInt(16), seaLevel, chunk.z * 16 + random.nextInt(16));
                if (!WorldGenRubberTree.WORLD_GEN_INSTANCE.generateImpl(chunk.getWorld(), random, pos)) {
                    rubberTrees -= 3;
                }
            }
        }
    }
}
