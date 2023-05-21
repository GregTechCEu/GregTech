package gregtech.worldgen.terrain.config.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.util.BlockStateHashStrategy;
import gregtech.api.util.FileUtility;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.terrain.IBlockMapper;
import gregtech.worldgen.terrain.config.JsonBlockMapping;
import gregtech.worldgen.terrain.config.TerrainGenConstants;
import gregtech.worldgen.terrain.internal.BlockMapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TerrainGenFileProcessor {

    /**
     * The version of the terrain generation JSONs
     */
    public static final int TERRAIN_GEN_VERSION = 1;

    // general constants
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    /**
     * The path to the terrain folder
     */
    private final Path terrainFolderPath;
    /**
     * Map containing file names without extensions mapped to a Parsed Mapper Entry
     */
    private final Map<String, ParsedBlockMapping> fileToParsedMapping = new Object2ObjectOpenHashMap<>();
    /**
     * Map containing the block mappers for each dimension
     */
    private final Int2ObjectMap<IBlockMapper> dimensionToBlockMapper = new Int2ObjectOpenHashMap<>();
    /**
     * The json of the replacement_entries file
     */
    private JsonObject replacementEntriesJson;
    /**
     * Paths to all json files in the terrain folder
     */
    private List<Path> terrainFolderContent = null;

    public TerrainGenFileProcessor(@Nonnull Path terrainFolderPath) {
        this.terrainFolderPath = terrainFolderPath;
    }

    /**
     * Process files for Terrain Generation
     *
     * @return a map of dimension id to block mappers
     */
    @Nonnull
    public Int2ObjectMap<IBlockMapper> process() {
        gatherJsonInTerrainFolder();
        processTerrainFolder();
        parseBlockMappersByDimension();
        return dimensionToBlockMapper;
    }

    /**
     * Gather the JSONs in the terrain folder
     */
    private void gatherJsonInTerrainFolder() {
        try (Stream<Path> files = Files.walk(Paths.get(terrainFolderPath.toUri()))) {
            terrainFolderContent = files.filter(file -> file.toString().endsWith(TerrainGenConstants.JSON_EXTENSION))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            WorldgenModule.logger.error("Unable to walk files in Terrain Folder", e);
        }
    }

    /**
     * Parses the contents of the terrain folder
     */
    private void processTerrainFolder() {
        if (terrainFolderContent == null) return;
        for (Path path : terrainFolderContent) {
            String fileName = path.toFile().getName();
            // parse the entries file if found
            if (fileName.startsWith(TerrainGenConstants.REPLACEMENT_ENTRIES)) {
                replacementEntriesJson = FileUtility.tryExtractFromFile(path);
                if (replacementEntriesJson == null) {
                    WorldgenModule.logger.error("Unable to parse Block Mapper entries file at Path {}.", path.toString());
                }
                continue;
            }

            JsonBlockMapping jsonBlockMapping = FileUtility.tryExtractFromFile(path, JsonBlockMapping.class);
            if (jsonBlockMapping == null) continue;
            ParsedBlockMapping parsed = ParsedBlockMapping.fromJson(jsonBlockMapping, path.toString());
            if (parsed == null) continue;
            fileToParsedMapping.put(DOT_PATTERN.split(fileName)[0], parsed);
        }

        if (replacementEntriesJson == null) {
            WorldgenModule.logger.error("Unable to find Block Mapper entries file.");
        }
    }

    /**
     * Parses the replacement entries json into a Map of dimension to BlockMapper
     */
    private void parseBlockMappersByDimension() {
        if (replacementEntriesJson == null) return;

        for (Map.Entry<String, JsonElement> entry : replacementEntriesJson.entrySet()) {
            // get the dimension
            int dimension;
            try {
                dimension = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException e) {
                WorldgenModule.logger.error("Unable to parse terrain replacement entry dimension " + entry.getKey(), e);
                continue;
            }

            // build the mapper for each dimension
            Map<IBlockState, List<IBlockState>> map = new Object2ObjectOpenCustomHashMap<>(BlockStateHashStrategy.STRATEGY);
            if (entry.getValue().isJsonArray()) {
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    addMapperEntryToMap(map, element.getAsString());
                }
            } else {
                addMapperEntryToMap(map, entry.getValue().getAsString());
            }

            dimensionToBlockMapper.put(dimension, new BlockMapper(map));
        }
    }

    /**
     * Adds a mapper entry to a map
     *
     * @param map      the map to add to
     * @param fileName the file name containing the mapper
     */
    private void addMapperEntryToMap(@Nonnull Map<IBlockState, List<IBlockState>> map, @Nonnull String fileName) {
        ParsedBlockMapping mapperEntry = fileToParsedMapping.get(fileName);
        if (mapperEntry == null) {
            WorldgenModule.logger.error("Unable to find Block Mapping for file name {} in entries file. Skipping...", fileName);
            return;
        }

        // merge mappers mapping the same block together
        map.merge(mapperEntry.target, mapperEntry.replacements, (key, value) -> {
            value.addAll(mapperEntry.replacements);
            return value;
        });
    }
}
