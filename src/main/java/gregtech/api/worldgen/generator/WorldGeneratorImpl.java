package gregtech.api.worldgen.generator;

import gregtech.common.ConfigHolder;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
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

}
