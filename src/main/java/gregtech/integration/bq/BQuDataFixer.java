package gregtech.integration.bq;

import gregtech.api.GregTechAPI;
import gregtech.api.util.Mods;
import gregtech.datafix.migration.lib.MTERegistriesMigrator;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BQuDataFixer {

    private static final Logger LOG = LogManager.getLogger(BQuDataFixer.class);

    /**
     * {@code saves/<world>/betterquesting/QuestDatabase.json}
     */
    private static final String QUEST_DB_JSON_FILE = "QuestDatabase.json";
    /**
     * {@code config/betterquesting/resources/}
     */
    private static final String BQ_RESOURCES_DIR = "resources";
    private static final String MC_CONFIG_DIR = "config";

    private static final String ID_8 = "id:8";
    private static final String DAMAGE_2 = "Damage:2";
    private static final String TAG_10 = "tag:10";
    private static final String ORIG_ID_8 = "orig_id:8";
    private static final String ORIG_META_3 = "orig_meta:3";

    private static final String PLACEHOLDER = "betterquesting:placeholder";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private BQuDataFixer() {}

    public static void onServerStarting(@NotNull ICommandSender server) {
        LOG.info("Attempting to apply BQu DataFixers");
        Path worldDir = server.getEntityWorld().getSaveHandler().getWorldDirectory().toPath();
        if (processWorldDir(worldDir)) {
            LOG.info("Successfully applied BQu data fixers");
        } else {
            LOG.error("Failed to apply BQu data fixers");
        }
    }

    /**
     * Processes the current world directory and applies data fixers
     *
     * @param worldDir the current world's directory
     * @return if processing was successful
     */
    public static boolean processWorldDir(@NotNull Path worldDir) {
        LOG.info("Processing world directory");
        Path bqDir = worldDir.resolve(Mods.Names.BETTER_QUESTING);
        Path questDbPath = bqDir.resolve(QUEST_DB_JSON_FILE);
        if (!Files.isRegularFile(questDbPath)) {
            LOG.info("Unable to find BQ Quest Database, assuming this is a new world");
            return true;
        }

        JsonObject json;
        try {
            json = readJson(questDbPath);
        } catch (FileNotFoundException e) {
            LOG.info("Unable to find BQ Quest Database, assuming this is a new world");
            return true;
        } catch (IOException e) {
            LOG.error("Failed to read BQ Quest Database in World Folder", e);
            return false;
        }

        for (var entry : json.entrySet()) {
            recurseJsonApplyFix(entry.getValue());
        }

        try {
            writeJson(questDbPath, json);
        } catch (IOException e) {
            LOG.error("Failed to write BQ Quest DataBase in World Folder", e);
        }

        return true;
    }

    /**
     * Processes the BQu config directory and applies data fixers
     *
     * @param worldDir the current world's directory
     * @return if processing was successful
     */
    public static boolean processConfigDir(@NotNull Path worldDir) {
        LOG.info("Processing config directory");
        Path mcDir = worldDir.getParent().getParent();
        Path configDir = mcDir.resolve(MC_CONFIG_DIR);
        Path bqConfigDir = configDir.resolve(Mods.Names.BETTER_QUESTING);

        List<Path> paths;
        try (var stream = Files.walk(bqConfigDir, 4)) {
            paths = stream.filter(p -> !p.endsWith(BQ_RESOURCES_DIR)) // do not walk down the resources dir
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toFile().getName().endsWith(".json"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Failed to read BQ Quest Database in Config Folder", e);
            return false;
        }

        Map<Path, JsonObject> map = new Object2ObjectOpenHashMap<>(paths.size());
        for (Path path : paths) {
            try {
                map.put(path, readJson(path));
            } catch (IOException e) {
                LOG.error("Failed to read BQ Quest File from Config Folder", e);
            }
        }

        map.values().stream()
                .flatMap(jsonObject -> jsonObject.entrySet().stream())
                .map(Map.Entry::getValue)
                .forEach(BQuDataFixer::recurseJsonApplyFix);

        for (var entry : map.entrySet()) {
            try {
                writeJson(entry.getKey(), entry.getValue());
            } catch (IOException e) {
                LOG.error("Failed to write BQ Quest File in Config Folder", e);
            }
        }

        return true;
    }

    /**
     * @param path the path to read
     * @return the json object stored at the path
     * @throws IOException if failure
     */
    private static @NotNull JsonObject readJson(@NotNull Path path) throws IOException {
        LOG.info("Reading file at path {}", path);
        try (Reader reader = Files.newBufferedReader(path)) {
            return GSON.fromJson(reader, JsonObject.class);
        }
    }

    /**
     * Recursively walks a JSON file and applies datafixers to matching sub-objects
     *
     * @param element the json element to recurse
     */
    private static void recurseJsonApplyFix(@NotNull JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has(ID_8)) {
                applyDataFix(object);
            } else {
                for (var entry : object.entrySet()) {
                    recurseJsonApplyFix(entry.getValue());
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement value : element.getAsJsonArray()) {
                recurseJsonApplyFix(value);
            }
        }
    }

    /**
     * @param path       the path to write the file
     * @param jsonObject the object to write
     * @throws IOException if failure
     */
    private static void writeJson(@NotNull Path path, @NotNull JsonObject jsonObject) throws IOException {
        LOG.info("Writing file to path {}", path);
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(jsonObject, writer);
        }
    }

    /**
     * Applies {@link MTERegistriesMigrator} fixes to a BQu json file
     *
     * @param jsonObject the json to fix
     */
    private static void applyDataFix(@NotNull JsonObject jsonObject) {
        MTERegistriesMigrator migrator = GregTechAPI.MIGRATIONS.registriesMigrator();

        ResourceLocation itemBlockId;
        short meta;
        String id = jsonObject.get(ID_8).getAsString();

        boolean isPlaceHolder = PLACEHOLDER.equals(id);

        if (isPlaceHolder) {
            // fix cases where BQu marks items as missing with placeholders
            JsonObject orig = jsonObject.getAsJsonObject(TAG_10);
            if (orig == null) {
                return;
            }

            if (!orig.has(ORIG_ID_8) || !orig.has(ORIG_META_3)) {
                return;
            }

            itemBlockId = new ResourceLocation(orig.get(ORIG_ID_8).getAsString());
            meta = orig.get(ORIG_META_3).getAsShort();
        } else {
            itemBlockId = new ResourceLocation(id);
            meta = jsonObject.get(DAMAGE_2) == null ? 0 : jsonObject.get(DAMAGE_2).getAsShort();
        }

        ResourceLocation fixedName = migrator.fixItemName(itemBlockId, meta);
        if (fixedName != null) {
            jsonObject.add(ID_8, new JsonPrimitive(fixedName.toString()));
        }

        short fixedMeta = migrator.fixItemMeta(itemBlockId, meta);
        if (fixedMeta != meta) {
            jsonObject.add(DAMAGE_2, new JsonPrimitive(fixedMeta));
        }

        if (isPlaceHolder) {
            jsonObject.remove(TAG_10);
        }
    }
}
