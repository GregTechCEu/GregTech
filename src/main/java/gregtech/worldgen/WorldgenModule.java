package gregtech.worldgen;

import com.google.common.base.Predicate;
import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.modules.BaseGregTechModule;
import gregtech.modules.GregTechModules;
import gregtech.worldgen.generator.ChunkAlignedSettings;
import gregtech.worldgen.generator.GeneratorRegistry;
import gregtech.worldgen.generator.LayeredVeinSettings;
import gregtech.worldgen.generator.StoneBlob;
import gregtech.worldgen.placeable.BlockStatePlaceable;
import gregtech.worldgen.placeable.MaterialPlaceable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@GregTechModule(
        moduleID = GregTechModules.MODULE_WORLDGEN,
        containerID = GTValues.MODID,
        name = "GregTech Worldgen",
        description = "GregTech Worldgen Module"
)
public class WorldgenModule extends BaseGregTechModule {

    public static final Logger logger = LogManager.getLogger("GregTech Worldgen");

    /**
     * If worldgen debugging should be enabled.
     * Generates <strong>very large</strong> log files which can hurt performance.
     */
    public static final boolean DEBUG = false;

    public static final GeneratorRegistry<ChunkAlignedSettings<?>> CHUNK_ALIGNED_REGISTRY = new GeneratorRegistry<>();
    public static final GeneratorRegistry<StoneBlob> STONE_BLOB_REGISTRY = new GeneratorRegistry<>();
//    public static final GeneratorRegistry<SporadicWorldGenerator> SPORADIC_REGISTRY = new GeneratorRegistry<>();

    @Override
    public @NotNull Logger getLogger() {
        return logger;
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        GameRegistry.registerWorldGenerator(GTWorldGenerator.INSTANCE, 1);

        STONE_BLOB_REGISTRY.register(new StoneBlob("blob_tiny", 0, 180, 5, 75,
                new int[]{0}, new String[0],
                new BlockStatePlaceable(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
                        .getState(StoneVariantBlock.StoneType.BASALT))));

        STONE_BLOB_REGISTRY.register(new StoneBlob("blob_small", 0, 180, 10, 100,
                new int[]{0}, new String[0],
                new BlockStatePlaceable(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
                        .getState(StoneVariantBlock.StoneType.BASALT))));

        STONE_BLOB_REGISTRY.register(new StoneBlob("blob_medium", 0, 180, 10, 200,
                new int[]{0}, new String[0],
                new BlockStatePlaceable(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
                        .getState(StoneVariantBlock.StoneType.BASALT))));

        STONE_BLOB_REGISTRY.register(new StoneBlob("blob_large", 0, 120, 70, 300,
                new int[]{0}, new String[0],
                new BlockStatePlaceable(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
                        .getState(StoneVariantBlock.StoneType.BASALT))));

        STONE_BLOB_REGISTRY.register(new StoneBlob("blob_huge", 0, 120, 150, 400,
                new int[]{0}, new String[0],
                new BlockStatePlaceable(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
                        .getState(StoneVariantBlock.StoneType.BASALT))));

        CHUNK_ALIGNED_REGISTRY.register(new LayeredVeinSettings("test1", 20, 50, 80, 5, 32,
                new int[]{0}, new String[0],
                new MaterialPlaceable(Materials.Diamond),
                new MaterialPlaceable(Materials.Gold),
                new MaterialPlaceable(Materials.Copper),
                new MaterialPlaceable(Materials.TricalciumPhosphate)
        ));

        CHUNK_ALIGNED_REGISTRY.register(new LayeredVeinSettings("test2", 40, 70, 20, 2, 24,
                new int[]{0}, new String[0],
                new MaterialPlaceable(Materials.Silver),
                new MaterialPlaceable(Materials.Redstone),
                new MaterialPlaceable(Materials.Opal),
                new MaterialPlaceable(Materials.YellowLimonite)
        ));
    }

    @Override
    public void serverStopped(FMLServerStoppedEvent event) {
        super.serverStopped(event);
        ChunkAlignedWorldgen.clearCache();
    }

    /**
     * Predicate for checking if a state can be replaced with oregen.
     * Use with {@link Block#isReplaceableOreGen(IBlockState, IBlockAccess, BlockPos, Predicate)}.
     *
     * @param state the state to check
     * @return if the state can be replaced for ore generation
     */
    public static boolean isOregenReplaceable(@NotNull IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.STONE) {
            int meta = block.getMetaFromState(state);
            return meta == 0 || meta == 1 || meta == 3 || meta == 5;
        }
        if (block == Blocks.NETHERRACK) return true;
        if (block == Blocks.END_STONE) return true;
        if (block == MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)) return true;

        //TODO also check a gt replaceable worldgen blocks registry
        return false;
    }

    public static boolean isSmallOresEnabled() {
        return true;
    }

    /**
     * @return the multiplier for the amount of small ores to generate
     */
    public static int smallOresMultiplier() {
        return 2;
    }

    /**
     * @return a percentage from 0 to 100 for the abundance of ore veins
     */
    public static int oreVeinAbundance() {
        return 100;
    }

    public static int maxOregenSearchAttempts() {
        return 64;
    }

    public static int maxOregenPlacementAttempts() {
        return 8;
    }

    private static final int ORIGIN_CHUNK_DIAMETER = 3;
    public static boolean isOriginChunk(int chunkX, int chunkZ) {
        return Math.floorMod(chunkX, ORIGIN_CHUNK_DIAMETER) == 1 && Math.floorMod(chunkZ, ORIGIN_CHUNK_DIAMETER) == 1;
    }
}
