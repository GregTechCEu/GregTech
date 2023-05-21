package gregtech.worldgen.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.GTValues;
import gregtech.api.util.FileUtility;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.terrain.IBlockMapper;
import gregtech.worldgen.terrain.config.TerrainGenDefaults;
import gregtech.worldgen.terrain.config.internal.TerrainGenFileProcessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WorldgenConfigReader {

    private static final String WORLDGEN_DIR = "worldgen";
    private static final String TERRAIN_DIR = "terrain";
    private static final String TERRAIN_VERSION_KEY = "terrainVersion";
    private static final String WORLDGEN_EXTRACTED = "worldgen_extracted.json";

    @Nullable
    private static Path terrainPath;

    private WorldgenConfigReader() {}

    @Nullable
    public static Int2ObjectMap<IBlockMapper> readTerrainMappersFromConfig() {
        Path configDir = Loader.instance().getConfigDir().toPath();

        Path gtConfigPath = getOrCreateGtConfigFolder(configDir);
        if (gtConfigPath == null) return null;

        Path worldgenPath = getOrCreateWorldgenFolder(gtConfigPath);
        if (worldgenPath == null) return null;

        // create the default paths before attempting to write defaults
        terrainPath = getOrCreateTerrainFolder(worldgenPath);
        if (terrainPath == null) return null;

        handleDefaults(gtConfigPath);

        return new TerrainGenFileProcessor(terrainPath).process();
    }

    /**
     * @param gtConfigPath the path to the gt config folder
     */
    private static void handleDefaults(@Nonnull Path gtConfigPath) {
        Path generationLockPath = gtConfigPath.resolve(WORLDGEN_EXTRACTED);

        if (!Files.exists(generationLockPath)) createLockFile(generationLockPath);

        if (Files.exists(generationLockPath)) {
            JsonObject generationLock = FileUtility.tryExtractFromFile(generationLockPath);
            if (generationLock != null) {
                boolean needsWriting = false;

                if (handleTerrainDefaults(generationLock)) {
                    // bump the version(s) on the lock file if needed
                    generationLock.addProperty(TERRAIN_VERSION_KEY, TerrainGenFileProcessor.TERRAIN_GEN_VERSION);
                    needsWriting = true;
                }

                if (needsWriting) {
                    try {
                        writeJsonToFile(generationLockPath, generationLock);
                    } catch (IOException e) {
                        WorldgenModule.logger.error("Unable to update Worldgen Lock File at Path: " + generationLockPath, e);
                    }
                }
            } // FileUtility#tryExtractFromFile handles logging for the else case already
        } // #createLockFile handles logging for the else case already
    }

    /**
     * Create the generation lock file
     *
     * @param path the path to the file
     */
    private static void createLockFile(@Nonnull Path path) {
        // create generation lock since it doesn't exist
        try {
            Files.createFile(path);
        } catch (IOException e) {
            WorldgenModule.logger.error("Failed to create the Worldgen Lock File at Path: " + path, e);
            return;
        }

        JsonObject lockContent = new JsonObject();
        lockContent.addProperty("_comment0", "This File exists to prevent custom oregen configurations from being overridden.");
        lockContent.addProperty("_comment1", "If this file is deleted, the oregen files will be regenerated from their defaults.");
        lockContent.addProperty("_comment2", "Modpack authors make sure this file is included in your modpack if you have customized ore generation.");
        lockContent.addProperty("_comment3", "The below versions are used for when GT must forcefully override existing vein configurations, and should not be used by packs.");
        lockContent.addProperty(TERRAIN_VERSION_KEY, TerrainGenFileProcessor.TERRAIN_GEN_VERSION);

        try {
            writeJsonToFile(path, lockContent);
        } catch (IOException e) {
            WorldgenModule.logger.error("Failed to write Worldgen Lock File contents to file at Path: " + path, e);
        }
    }

    /**
     * @param generationLock the generation lock file
     * @return if a lock file update is needed
     */
    private static boolean handleTerrainDefaults(@Nonnull JsonObject generationLock) {
        if (terrainPath == null) return false;
        JsonElement terrainVersionElement = generationLock.get(TERRAIN_VERSION_KEY);
        if (terrainVersionElement == null || terrainVersionElement.getAsInt() != TerrainGenFileProcessor.TERRAIN_GEN_VERSION) {
            MinecraftForge.TERRAIN_GEN_BUS.post(new WorldgenDefaultsEvent(WorldgenDefaultsEvent.Type.TERRAIN_GEN));

            try {
                TerrainGenDefaults.write(terrainPath);
            } catch (IOException e) {
                WorldgenModule.logger.error("Unable to write default Terrain Generation files at Path: " + terrainPath, e);
            }
            return true;
        }
        return false;
    }

    /**
     * Writes json to a file
     *
     * @param path the path to the file
     * @param json the json to write
     * @throws IOException if errors happen during file i/o
     */
    private static void writeJsonToFile(@Nonnull Path path, @Nonnull JsonObject json) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(json, writer);
        }
    }

    /**
     * Create the gt config folder
     *
     * @param baseConfigPath the path to the base config folder
     */
    @Nullable
    private static Path getOrCreateGtConfigFolder(@Nonnull Path baseConfigPath) {
        Path gtConfigPath = baseConfigPath.resolve(GTValues.MODID);
        if (!Files.exists(gtConfigPath)) {
            try {
                Files.createDirectories(gtConfigPath);
            } catch (IOException e) {
                WorldgenModule.logger.error("Unable to create GregTech Config Directory at Path: " + gtConfigPath, e);
                return null;
            }
        }
        return gtConfigPath;
    }

    /**
     * Create the worldgen folder
     *
     * @param gtConfigPath the path to the gt config folder
     */
    @Nullable
    private static Path getOrCreateWorldgenFolder(@Nonnull Path gtConfigPath) {
        Path worldgenPath = gtConfigPath.resolve(WORLDGEN_DIR);
        if (!Files.exists(worldgenPath)) {
            try {
                Files.createDirectories(worldgenPath);
            } catch (IOException e) {
                WorldgenModule.logger.error("Unable to create Worldgen Directory at Path: " + worldgenPath, e);
                return null;
            }
        }
        return worldgenPath;
    }

    /**
     * Create the terrain folder
     *
     * @param worldgenPath the path to the worldgen folder
     */
    @Nullable
    private static Path getOrCreateTerrainFolder(@Nonnull Path worldgenPath) {
        Path terrainPath = worldgenPath.resolve(TERRAIN_DIR);
        if (!Files.exists(terrainPath)) {
            try {
                Files.createDirectories(terrainPath);
            } catch (IOException e) {
                WorldgenModule.logger.error("Unable to create GregTech Terrain Directory at Path: " + terrainPath, e);
                return null;
            }
        }
        return terrainPath;
    }
}
