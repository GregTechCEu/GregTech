package gregtech.worldgen;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.common.ConfigHolder;
import gregtech.modules.BaseGregTechModule;
import gregtech.modules.GregTechModules;
import gregtech.worldgen.impl.WorldGenRubberTree;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.*;

@GregTechModule(
                moduleID = GregTechModules.MODULE_WORLDGEN,
                containerID = GTValues.MODID,
                name = "GregTech Worldgen",
                description = "GregTech Worldgen Module.")
public class WorldgenModule extends BaseGregTechModule {

    public static final Logger LOGGER = LogManager.getLogger("GregTech Worldgen");

    private static final Set<OreGenEvent.GenerateMinable.EventType> VANILLA_ORE_GEN_EVENT_TYPES = EnumSet.of(
            COAL, DIAMOND, GOLD, IRON, LAPIS, REDSTONE, QUARTZ, EMERALD);

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    @Override
    public @NotNull List<Class<?>> getTerrainGenBusSubscribers() {
        return Collections.singletonList(WorldgenModule.class);
    }

    @Override
    public @NotNull List<Class<?>> getOreGenBusSubscribers() {
        return Collections.singletonList(WorldgenModule.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGenerateMineable(@NotNull OreGenEvent.GenerateMinable event) {
        if (ConfigHolder.worldgen.disableVanillaOres && VANILLA_ORE_GEN_EVENT_TYPES.contains(event.getType())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onBiomeDecorate(@NotNull DecorateBiomeEvent.Decorate event) {
        if (event.getType() == DecorateBiomeEvent.Decorate.EventType.TREE) {
            if (ConfigHolder.worldgen.disableRubberTreeGeneration) {
                return;
            }

            // replaces regular tree generation with occasional rubber trees
            if (generateRubberTrees(event.getWorld(), event.getChunkPos(), event.getRand())) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    /**
     * Generates a rubber trees
     *
     * @param world    the world in which the tree should be placed
     * @param chunkPos the position of the chunk the tree should be generated at
     * @param random   the random number generator to use
     * @return if trees were generated
     */
    private static boolean generateRubberTrees(@NotNull World world, @NotNull ChunkPos chunkPos,
                                               @NotNull Random random) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        final int chunkX = chunkPos.x * 16;
        final int chunkZ = chunkPos.z * 16;
        pos.setPos(chunkX + 16, 0, chunkZ + 16);
        Biome biome = world.getBiome(pos);

        int amount = 0;
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) {
            amount += random.nextInt(10) + 5;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST) ||
                BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) {
            amount += random.nextInt(5) + 1;
        }

        amount = (int) (amount * ConfigHolder.worldgen.rubberTreeRateIncrease / 2);
        if (amount > 0 && random.nextInt(100) < amount) {
            boolean generated = false;
            for (int i = 0; i < amount; i++) {
                int x = chunkX + random.nextInt(16) + 8;
                int z = chunkZ + random.nextInt(16) + 8;
                int y = world.getHeight(x, z);
                pos.setPos(x, y, z);

                WorldGenRubberTree.INSTANCE.setDecorationDefaults();
                if (WorldGenRubberTree.INSTANCE.generate(world, random, pos)) {
                    WorldGenRubberTree.INSTANCE.generateSaplings(world, random, pos);
                    generated = true;
                }
            }

            return generated;
        }

        return false;
    }
}
