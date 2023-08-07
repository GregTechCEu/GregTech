package gregtech.worldgen.terrain.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gregtech.api.util.FileUtility;
import gregtech.worldgen.WorldgenModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Handles default entries for terrain generation.
 * Only register entries when handling an {@link gregtech.worldgen.config.WorldgenDefaultsEvent} event.
 */
public final class TerrainGenDefaults {

    private static final Map<String, JsonBlockMapping> mappings = new Object2ObjectOpenHashMap<>();

    private static final Int2ObjectMap<List<String>> replacementEntries = new Int2ObjectOpenHashMap<>();

    private TerrainGenDefaults() {}

    /**
     * Add a default mapping
     *
     * @param fileName the name of the file for the mapping, with no extension
     * @param mapping the mapping to write
     */
    public static void addDefaultMapping(@Nonnull String fileName, @Nonnull JsonBlockMapping mapping) {
        mappings.put(fileName, mapping);
    }

    /**
     * @param fileName the name of the file to remove, with no extension
     * @return if the file was removed
     */
    @SuppressWarnings("unused")
    public static boolean removeDefaultMapping(@Nonnull String fileName) {
        return mappings.remove(fileName) != null;
    }

    /**
     * @param dimensionId the dimension to replace blocks in
     * @param fileNames the file names of the entries to use
     */
    public static void addReplacementEntry(int dimensionId, @Nonnull String... fileNames) {
        List<String> list = replacementEntries.get(dimensionId);
        if (list == null) {
            list = new ArrayList<>();
            replacementEntries.put(dimensionId, list);
        }
        Collections.addAll(list, fileNames);
    }

    /**
     * @param dimensionId the dimension to remove the entry from
     * @param fileName the name of the entry's file to remove, with no extension
     * @return if the entry was removed
     */
    @SuppressWarnings("unused")
    public static boolean removeReplacementEntry(int dimensionId, @Nonnull String fileName) {
        List<String> list = replacementEntries.get(dimensionId);
        if (list == null) return false;
        return list.remove(fileName);
    }

    /**
     * Internal use <strong>ONLY</strong>. Writes the registered terrain generation defaults to the disk
     *
     * @throws IOException if errors happen during file i/o
     */
    public static void write(@Nonnull Path terrainPath) throws IOException {
        for (Map.Entry<String, JsonBlockMapping> entry : mappings.entrySet()) {
            String path = entry.getKey() + TerrainGenConstants.JSON_EXTENSION;
            try {
                FileUtility.tryWriteObjectAsJsonUnchecked(terrainPath.resolve(path), entry.getValue(), JsonBlockMapping.class);
            } catch (IOException e) {
                WorldgenModule.logger.error("Unable to write default terrain file to path " + path, e);
            }
        }

        JsonObject jsonObject = new JsonObject();
        for (Int2ObjectMap.Entry<List<String>> entry : replacementEntries.int2ObjectEntrySet()) {
            JsonArray array = new JsonArray();
            for (String s : entry.getValue()) {
                array.add(new JsonPrimitive(s));
            }
            jsonObject.add(String.valueOf(entry.getIntKey()), array);
        }

        if (!jsonObject.entrySet().isEmpty()) {
            String path = TerrainGenConstants.REPLACEMENT_ENTRIES + TerrainGenConstants.JSON_EXTENSION;
            FileUtility.tryWriteJsonToFile(terrainPath.resolve(path), jsonObject);
        }
    }
}
