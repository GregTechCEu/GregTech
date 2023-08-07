package gregtech.api.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import gregtech.api.worldgen.config.WorldGenRegistry;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtility {
    public static final JsonParser jsonParser = new JsonParser();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern UNDERSCORE_REGEX = Pattern.compile("_");

    private FileUtility() {
    }

    public static String readInputStream(InputStream inputStream) throws IOException {
        byte[] streamData = IOUtils.toByteArray(inputStream);
        return new String(streamData, StandardCharsets.UTF_8);
    }

    public static InputStream writeInputStream(String contents) {
        return new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tries to extract {@code JsonObject} from file on given path
     *
     * @param filePath path to file
     * @return {@code JsonObject} if extraction succeeds; otherwise {@code null}
     */
    @Nullable
    public static JsonObject tryExtractFromFile(@Nonnull Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            return jsonParser.parse(reader).getAsJsonObject();
        } catch (IOException exception) {
            GTLog.logger.error("Failed to read file on path {}", filePath, exception);
        } catch (JsonParseException exception) {
            GTLog.logger.error("Failed to extract json from file", exception);
        } catch (Exception exception) {
            GTLog.logger.error("Failed to extract json from file on path {}", filePath, exception);
        }

        return null;
    }

    @Nullable
    public static <T> T tryExtractFromFile(@Nonnull Path filePath, Class<T> clazz) {
        try {
            return gson.fromJson(Files.newBufferedReader(filePath), clazz);
        } catch (IOException exception) {
            GTLog.logger.error("Failed to read file on path " + filePath, exception);
        } catch (JsonParseException exception) {
            GTLog.logger.error("Failed to extract json from file", exception);
        } catch (Exception exception) {
            GTLog.logger.error("Failed to extract json from file on path " + filePath, exception);
        }
        return null;
    }

    /**
     * Write an object as json
     *
     * @param filePath the path to write the file to
     * @param instance the instance to write
     * @param clazz the class of the instance
     * @param <T> the type of the instance
     */
    public static <T> void tryWriteObjectAsJson(@Nonnull Path filePath, T instance, Class<T> clazz) {
        try {
            tryWriteObjectAsJsonUnchecked(filePath, instance, clazz);
        } catch (IOException e) {
            GTLog.logger.error("Failed to write file to path " + filePath, e);
        } catch (JsonParseException e) {
            GTLog.logger.error("Failed to convert object to json", e);
        } catch (Exception e) {
            GTLog.logger.error("Failed to write json file to path " + filePath, e);
        }
    }

    /**
     * Write an object as json
     *
     * @param filePath the path to write the file to
     * @param instance the instance to write
     * @param clazz the class of the instance
     * @param <T> the type of the instance
     * @throws IOException if errors happen during file i/o
     */
    public static <T> void tryWriteObjectAsJsonUnchecked(@Nonnull Path filePath, T instance, Class<T> clazz) throws IOException {
        JsonObject toWrite = gson.toJsonTree(instance, clazz).getAsJsonObject();
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(toWrite, writer);
        }
    }

    /**
     * @param filePath the path to write the file to
     * @param object the json to write
     * @throws IOException if errors happen during file i/o
     */
    public static void tryWriteJsonToFile(@Nonnull Path filePath, JsonObject object) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(object, writer);
        }
    }

    public static JsonElement loadJson(File file) {
        try {
            if (!file.isFile()) return null;
            Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
            JsonElement json = jsonParser.parse(new JsonReader(reader));
            reader.close();
            return json;
        } catch (Exception e) {
            GTLog.logger.error("Failed to read file on path {}", file, e);
        }
        return null;
    }

    public static boolean saveJson(File file, JsonElement element) {
        try {
            if (!file.getParentFile().isDirectory()) {
                if (!file.getParentFile().mkdirs()){
                    GTLog.logger.error("Failed to create file dirs on path {}", file);
                }
            }
            Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
            writer.write(gson.toJson(element));
            writer.close();
            return true;
        } catch (Exception e) {
            GTLog.logger.error("Failed to save file on path {}", file, e);
        }
        return false;
    }

    public static void extractJarFiles(String resource, File targetPath, boolean replace) { //terminal/guide
        FileSystem zipFileSystem = null;
        try {
            URL sampleUrl = WorldGenRegistry.class.getResource("/assets/gregtech/.gtassetsroot");
            if (sampleUrl == null) {
                GTLog.logger.warn("Could not find .gtassetroot resource.");
                return;
            }
            URI sampleUri = sampleUrl.toURI();
            Path resourcePath;
            if (sampleUri.getScheme().equals("jar") || sampleUri.getScheme().equals("zip")) {
                zipFileSystem = FileSystems.newFileSystem(sampleUri, Collections.emptyMap());
                resourcePath = zipFileSystem.getPath(resource);
            } else if (sampleUri.getScheme().equals("file")) {
                URL resourceURL = WorldGenRegistry.class.getResource(resource);
                if (resourceURL == null) {
                    GTLog.logger.warn("Could not find resource file for {}.", resource);
                    return;
                }
                resourcePath = Paths.get(resourceURL.toURI());
            } else {
                throw new IllegalStateException("Unable to locate absolute path to directory: " + sampleUri);
            }

            List<Path> jarFiles;
            try (Stream<Path> stream = Files.walk(resourcePath)) {
                jarFiles = stream.filter(Files::isRegularFile).collect(Collectors.toList());
            }

            for (Path jarFile : jarFiles) {
                Path genPath = targetPath.toPath().resolve(resourcePath.relativize(jarFile).toString());
                Files.createDirectories(genPath.getParent());
                if (replace || !genPath.toFile().isFile()) {
                    Files.copy(jarFile, genPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (URISyntaxException impossible) {
            throw new RuntimeException(impossible);
        } catch (IOException exception) {
            GTLog.logger.error("error while extracting jar files: {} {}", resource, targetPath, exception);
        }
        finally {
            if (zipFileSystem != null) {
                //close zip file system to avoid issues
                IOUtils.closeQuietly(zipFileSystem);
            }
        }

    }


    /**
     * Takes a file path to a json file and trims the path down to the actual file name
     * Replaces all _ in the file name with spaces and capitalizes the file name
     *
     * @param name The File path
     * @return A String of the File name at the end of the file path
     */
    public static String trimFileName(String name) {
        // this method is passed deposit names, which need to be converted first
        name = slashToNativeSep(name);

        //Remove the leading "folderName\"
        String[] tempName = name.split(Matcher.quoteReplacement(File.separator));
        //Take the last entry in case of nested folders
        String newName = tempName[tempName.length - 1];
        //Remove the ".json"
        tempName = newName.split("\\.");
        //Take the first entry
        newName = tempName[0];
        //Replace all "_" with a space
        newName = UNDERSCORE_REGEX.matcher(newName).replaceAll(" ");
        //Capitalize the first letter
        newName = newName.substring(0, 1).toUpperCase() + newName.substring(1);

        return newName;
    }

    /**
     * Converts a path string from using the filesystem's native path separator to /
     * <br>
     * Useful for converting paths to consistent strings across operating systems
     */
    public static String nativeSepToSlash(String path) {
        return path.replace(File.separatorChar, '/');
    }

    /**
     * Converts a path string from using / to the filesystem's native path separator
     * <br>
     * Useful for allowing paths converted with {@link FileUtility#nativeSepToSlash(String)} to be used for file i/o
     */
    public static String slashToNativeSep(String path) {
        return path.replace('/', File.separatorChar);
    }
}
