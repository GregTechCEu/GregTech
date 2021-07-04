package gregtech.api.worldgen.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.GTValues;
import gregtech.api.util.FileUtility;
import gregtech.api.util.GTLog;
import gregtech.api.worldgen.filler.BlacklistedBlockFiller;
import gregtech.api.worldgen.filler.BlockFiller;
import gregtech.api.worldgen.filler.SimpleBlockFiller;
import gregtech.api.worldgen.generator.WorldGeneratorImpl;
import gregtech.api.worldgen.populator.FluidSpringPopulator;
import gregtech.api.worldgen.populator.IVeinPopulator;
import gregtech.api.worldgen.populator.SurfaceBlockPopulator;
import gregtech.api.worldgen.populator.SurfaceRockPopulator;
import gregtech.api.worldgen.shape.*;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.io.IOUtils;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ZenClass("mods.gregtech.ore.WorldGenRegistry")
@ZenRegister
public class WorldGenRegistry {

    public static final WorldGenRegistry INSTANCE = new WorldGenRegistry();

    private WorldGenRegistry() {
    }

    private final Map<String, Supplier<ShapeGenerator>> shapeGeneratorRegistry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Supplier<BlockFiller>> blockFillerRegistry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Supplier<IVeinPopulator>> veinPopulatorRegistry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<Integer, String> namedDimensions = new HashMap<>();

    private final List<OreDepositDefinition> registeredDefinitions = new ArrayList<>();
    private final Map<WorldProvider, WorldOreVeinCache> oreVeinCache = new WeakHashMap<>();

    private class WorldOreVeinCache {
        private final List<OreDepositDefinition> worldVeins;
        private final Map<Biome, List<Entry<Integer, OreDepositDefinition>>> biomeVeins = new HashMap<>();

        public WorldOreVeinCache(WorldProvider worldProvider) {
            this.worldVeins = registeredDefinitions.stream()
                .filter(definition -> definition.getDimensionFilter().test(worldProvider))
                .collect(Collectors.toList());
        }

        private List<Entry<Integer, OreDepositDefinition>> getBiomeEntry(Biome biome) {
            if (biomeVeins.containsKey(biome))
                return biomeVeins.get(biome);
            List<Entry<Integer, OreDepositDefinition>> result = worldVeins.stream()
                .map(vein -> new SimpleEntry<>(vein.getWeight() + vein.getBiomeWeightModifier().apply(biome), vein))
                .filter(entry -> entry.getKey() > 0)
                .collect(Collectors.toList());
            biomeVeins.put(biome, result);
            return result;
        }
    }

    public List<Entry<Integer, OreDepositDefinition>> getCachedBiomeVeins(WorldProvider provider, Biome biome) {
        if (oreVeinCache.containsKey(provider))
            return oreVeinCache.get(provider).getBiomeEntry(biome);
        WorldOreVeinCache worldOreVeinCache = new WorldOreVeinCache(provider);
        oreVeinCache.put(provider, worldOreVeinCache);
        return worldOreVeinCache.getBiomeEntry(biome);
    }

    public void initializeRegistry() {
        GTLog.logger.info("Initializing ore generation registry...");
        registerShapeGenerator("ellipsoid", EllipsoidGenerator::new);
        registerShapeGenerator("sphere", SphereGenerator::new);
        registerShapeGenerator("plate", PlateGenerator::new);
        registerShapeGenerator("single", SingleBlockGenerator::new);
        registerBlockFiller("simple", SimpleBlockFiller::new);
        registerBlockFiller("ignore_bedrock", () -> new BlacklistedBlockFiller(Lists.newArrayList(Blocks.BEDROCK.getDefaultState())));
        registerVeinPopulator("surface_rock", SurfaceRockPopulator::new);
        registerVeinPopulator("fluid_spring", FluidSpringPopulator::new);
        registerVeinPopulator("surface_block", SurfaceBlockPopulator::new);

        WorldGeneratorImpl worldGenerator = new WorldGeneratorImpl();
        GameRegistry.registerWorldGenerator(worldGenerator, 1);
        MinecraftForge.ORE_GEN_BUS.register(worldGenerator);
        try {
            reinitializeRegisteredVeins();
        } catch (IOException | RuntimeException exception) {
            GTLog.logger.fatal("Failed to initialize worldgen registry.", exception);
        }
    }

    public void reinitializeRegisteredVeins() throws IOException {
        GTLog.logger.info("Reloading ore generation files from config...");
        registeredDefinitions.clear();
        oreVeinCache.clear();
        Path configPath = Loader.instance().getConfigDir().toPath().resolve(GTValues.MODID);
        Path dimensionsFile = configPath.resolve("dimensions.json");
        Path worldgenRootPath = configPath.resolve("worldgen");
        Path jarFileExtractLockOld = configPath.resolve(".worldgen_extracted");
        Path jarFileExtractLock = configPath.resolve("worldgen_extracted");
        if (!Files.exists(worldgenRootPath)) {
            Files.createDirectories(worldgenRootPath);
        }

        if(!Files.exists(dimensionsFile)) {
            Files.createFile(dimensionsFile);
            extractJarVeinDefinitions(configPath, dimensionsFile);
        }

        //attempt extraction if file extraction lock is absent or worldgen root directory is empty
        if ((!Files.exists(jarFileExtractLock) && !Files.exists(jarFileExtractLockOld)) || !Files.list(worldgenRootPath).findFirst().isPresent()) {
            if (!Files.exists(jarFileExtractLock)) {
                //create extraction lock only if it doesn't exist
                Files.createFile(jarFileExtractLock);
            }
            extractJarVeinDefinitions(configPath, worldgenRootPath);
        }

        gatherNamedDimensions(dimensionsFile);

        List<Path> worldgenFiles = Files.walk(worldgenRootPath)
            .filter(path -> path.toString().endsWith(".json"))
            .filter(path -> Files.isRegularFile(path))
            .collect(Collectors.toList());

        for (Path worldgenDefinition : worldgenFiles) {
            JsonObject element = FileUtility.tryExtractFromFile(worldgenDefinition);
            if(element == null){
                break;
            }

            String depositName = worldgenRootPath.relativize(worldgenDefinition).toString();

            try {
                OreDepositDefinition deposit = new OreDepositDefinition(depositName);
                deposit.initializeFromConfig(element);
                registeredDefinitions.add(deposit);
            } catch (RuntimeException exception) {
                GTLog.logger.error("Failed to parse worldgen definition {} on path {}", depositName, worldgenDefinition, exception);
            }
        }
        GTLog.logger.info("Loaded {} worldgen definitions", registeredDefinitions.size());
    }

    private static void extractJarVeinDefinitions(Path configPath, Path targetPath) throws IOException {
        Path worldgenRootPath = configPath.resolve("worldgen");
        Path dimensionsRootPath = configPath.resolve("dimensions.json");
        FileSystem zipFileSystem = null;
        try {
            URI sampleUri = WorldGenRegistry.class.getResource("/assets/gregtech/.gtassetsroot").toURI();
            Path worldgenJarRootPath;
            if (sampleUri.getScheme().equals("jar") || sampleUri.getScheme().equals("zip")) {
                zipFileSystem = FileSystems.newFileSystem(sampleUri, Collections.emptyMap());
                worldgenJarRootPath = zipFileSystem.getPath("/assets/gregtech/worldgen");
            } else if (sampleUri.getScheme().equals("file")) {
                worldgenJarRootPath = Paths.get(WorldGenRegistry.class.getResource("/assets/gregtech/worldgen").toURI());
            } else {
                throw new IllegalStateException("Unable to locate absolute path to worldgen root directory: " + sampleUri);
            }

            if(targetPath.compareTo(worldgenRootPath) == 0) {
                GTLog.logger.info("Attempting extraction of standard worldgen definitions from {} to {}",
                    worldgenJarRootPath, worldgenRootPath);
                List<Path> jarFiles = Files.walk(worldgenJarRootPath)
                    .filter(jarFile -> Files.isRegularFile(jarFile))
                    .filter(jarPath -> !(jarPath.compareTo(worldgenJarRootPath.resolve("dimensions.json")) == 0))
                    .collect(Collectors.toList());
                for (Path jarFile : jarFiles) {
                    Path worldgenPath = worldgenRootPath.resolve(worldgenJarRootPath.relativize(jarFile).toString());
                    Files.createDirectories(worldgenPath.getParent());
                    Files.copy(jarFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);
                }
                GTLog.logger.info("Extracted {} builtin worldgen definitions into worldgen folder", jarFiles.size());
            }
            else if(targetPath.compareTo(dimensionsRootPath) == 0) {
                GTLog.logger.info("Attempting extraction of standard dimension definitions from {} to {}",
                    worldgenJarRootPath, dimensionsRootPath);

                Path dimensionFile = worldgenJarRootPath.resolve("dimensions.json");

                Path worldgenPath = dimensionsRootPath.resolve(worldgenJarRootPath.relativize(worldgenJarRootPath).toString());
                Files.copy(dimensionFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);

                GTLog.logger.info("Extracted builtin dimension definitions into worldgen folder");
            }

        } catch (URISyntaxException impossible) {
            //this is impossible, since getResource always returns valid URI
            throw new RuntimeException(impossible);
        } finally {
            if (zipFileSystem != null) {
                //close zip file system to avoid issues
                IOUtils.closeQuietly(zipFileSystem);
            }
        }
    }

    private void gatherNamedDimensions(Path dimensionsFile) {
        JsonObject element = FileUtility.tryExtractFromFile(dimensionsFile);
        if(element == null){
            return;
        }

        try {
            JsonArray dims = element.getAsJsonArray("dims");
            for(JsonElement dim : dims) {
                namedDimensions.put(dim.getAsJsonObject().get("dimID").getAsInt(), dim.getAsJsonObject().get("dimName").getAsString());
            }
        } catch (RuntimeException exception){
            GTLog.logger.error("Failed to parse named dimensions", exception);
        }
    }


    public void registerShapeGenerator(String identifier, Supplier<ShapeGenerator> shapeGeneratorSupplier) {
        if (shapeGeneratorRegistry.containsKey(identifier))
            throw new IllegalArgumentException("Identifier already occupied:" + identifier);
        shapeGeneratorRegistry.put(identifier, shapeGeneratorSupplier);
    }

    public void registerBlockFiller(String identifier, Supplier<BlockFiller> blockFillerSupplier) {
        if (blockFillerRegistry.containsKey(identifier))
            throw new IllegalArgumentException("Identifier already occupied:" + identifier);
        blockFillerRegistry.put(identifier, blockFillerSupplier);
    }

    public void registerVeinPopulator(String identifier, Supplier<IVeinPopulator> veinPopulatorSupplier) {
        if (veinPopulatorRegistry.containsKey(identifier))
            throw new IllegalArgumentException("Identifier already occupied:" + identifier);
        veinPopulatorRegistry.put(identifier, veinPopulatorSupplier);
    }

    public ShapeGenerator createShapeGenerator(JsonObject object) {
        String identifier = object.get("type").getAsString();
        if (!shapeGeneratorRegistry.containsKey(identifier))
            throw new IllegalArgumentException("No shape generator found for type " + identifier);
        ShapeGenerator shapeGenerator = shapeGeneratorRegistry.get(identifier).get();
        shapeGenerator.loadFromConfig(object);
        return shapeGenerator;
    }

    public BlockFiller createBlockFiller(JsonObject object) {
        String identifier = object.get("type").getAsString();
        if (!blockFillerRegistry.containsKey(identifier))
            throw new IllegalArgumentException("No block filler found for type " + identifier);
        BlockFiller blockFiller = blockFillerRegistry.get(identifier).get();
        blockFiller.loadFromConfig(object);
        return blockFiller;
    }

    public IVeinPopulator createVeinPopulator(JsonObject object) {
        String identifier = object.get("type").getAsString();
        if (!veinPopulatorRegistry.containsKey(identifier))
            throw new IllegalArgumentException("No vein populator found for type " + identifier);
        IVeinPopulator veinPopulator = veinPopulatorRegistry.get(identifier).get();
        veinPopulator.loadFromConfig(object);
        return veinPopulator;
    }

    @ZenGetter("oreDeposits")
    public static List<OreDepositDefinition> getOreDeposits() {
        return Collections.unmodifiableList(INSTANCE.registeredDefinitions);
    }

    public static Map<Integer, String> getNamedDimensions() {
        return INSTANCE.namedDimensions;
    }

}
