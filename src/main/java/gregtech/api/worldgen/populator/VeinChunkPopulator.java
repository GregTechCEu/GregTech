package gregtech.api.worldgen.populator;

import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.generator.GridEntryInfo;

import net.minecraft.world.World;

import java.util.Random;

public interface VeinChunkPopulator extends IVeinPopulator {

    void populateChunk(World world, int chunkX, int chunkZ, Random random, OreDepositDefinition definition,
                       GridEntryInfo gridEntryInfo);
}
