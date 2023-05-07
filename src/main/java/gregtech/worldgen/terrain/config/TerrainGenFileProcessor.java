package gregtech.worldgen.terrain.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.util.BlockStateHashStrategy;
import gregtech.api.util.ConfigUtil;
import gregtech.api.util.FileUtility;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.terrain.BlockMapper;
import gregtech.worldgen.terrain.IBlockMapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
    private static final String JSON_EXTENSION = ".json";
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    // replacement_entries.json content constants
    private static final String REPLACEMENT_ENTRIES = "replacement_entries";
    private static final String TARGET = "target";
    private static final String REPLACEMENTS = "replacements";

    private final Path terrainFolderPath;
    /**
     * Map containing file names without extensions mapped to a list of entries for a BlockMapper
     */
    private final Map<String, Map.Entry<IBlockState, List<IBlockState>>> fileToMapperEntry = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectMap<IBlockMapper> dimensionToBlockMapper = new Int2ObjectOpenHashMap<>();
    /**
     * The json in the replacement_entries file
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
     * Parses an entry for a BlockMapper from json
     *
     * @param json     the json to parse from
     * @param filePath the path to the json file, for logging
     * @return the entry
     */
    @Nullable
    private static Map.Entry<IBlockState, List<IBlockState>> parseMapperEntryFromJson(@Nonnull JsonObject json, @Nonnull String filePath) {
        JsonElement element = json.get(TARGET);
        String blockName = element.getAsString();
        IBlockState target = ConfigUtil.getBlockStateFromName(blockName);
        if (target == null) {
            WorldgenModule.logger.error("Unable to parse target BlockState from name {}. Skipping file {}", blockName, filePath);
            return null;
        }

        element = json.get(REPLACEMENTS);
        if (element.isJsonArray()) { // array of replacements
            List<IBlockState> list = new ArrayList<>();
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                IBlockState blockState = parseReplacementBlockStateFromName(jsonElement.getAsString(), target, filePath);
                if (blockState != null) list.add(blockState);
            }

            return new AbstractObject2ObjectMap.BasicEntry<>(target, list);
        } else { // single replacement entry
            IBlockState blockState = parseReplacementBlockStateFromName(element.getAsString(), target, filePath);
            if (blockState != null) {
                return new AbstractObject2ObjectMap.BasicEntry<>(target, Collections.singletonList(blockState));
            }
        }
        return null;
    }

    /**
     * Handles logging for a parsed BlockState
     *
     * @param name     the name of the BlockState to parse
     * @param target   the target BlockState to replace, used for logging
     * @param filePath the path to the file, used for logging
     * @return the parsed BlockState
     */
    @Nullable
    private static IBlockState parseReplacementBlockStateFromName(@Nonnull String name, @Nonnull IBlockState target,
                                                                  @Nonnull String filePath) {
        IBlockState blockState = ConfigUtil.getBlockStateFromName(name);
        if (blockState == null) {
            WorldgenModule.logger.error("Unable to parse replacement BlockState from name {} in file {}. Skipping entry...", name, filePath);
        } else if (blockState == target) {
            WorldgenModule.logger.warn("Replacement BlockState with name {} is identical to the target in file {}. Skipping entry...", name, filePath);
        }
        return blockState;
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
            terrainFolderContent = files.filter(file -> file.toString().endsWith(JSON_EXTENSION))
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
            if (fileName.startsWith(REPLACEMENT_ENTRIES)) {
                replacementEntriesJson = FileUtility.tryExtractFromFile(path);
                if (replacementEntriesJson == null) {
                    WorldgenModule.logger.error("Unable to parse Block Mapper entries file at Path {}.", path.toString());
                }
                continue;
            }

            // store each mapper entry by its file name without the extension
            JsonObject json = FileUtility.tryExtractFromFile(path);
            if (json == null) continue;
            Map.Entry<IBlockState, List<IBlockState>> parsed = parseMapperEntryFromJson(json, path.toString());
            if (parsed == null) continue;
            fileToMapperEntry.put(DOT_PATTERN.split(fileName)[0], parsed);
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
        Map.Entry<IBlockState, List<IBlockState>> mapperEntry = fileToMapperEntry.get(fileName);
        if (mapperEntry == null) {
            WorldgenModule.logger.error("Unable to find Block Mapper for file name {} in entries file. Skipping...", fileName);
            return;
        }
        map.put(mapperEntry.getKey(), mapperEntry.getValue());
    }
}
