package gregtech.api.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import gregtech.api.worldgen.config.WorldGenRegistry;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class FileUtility {
    public static final JsonParser jsonParser = new JsonParser();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
     * Tries to extract <code>JsonObject</code> from file on given path
     *
     * @param filePath path to file
     * @return <code>JsonObject</code> if extraction succeeds; otherwise <code>null</code>
     */
    public static JsonObject tryExtractFromFile(Path filePath) {
        try (InputStream fileStream = Files.newInputStream(filePath)) {
            InputStreamReader streamReader = new InputStreamReader(fileStream);
            return jsonParser.parse(streamReader).getAsJsonObject();
        } catch (IOException exception) {
            GTLog.logger.error("Failed to read file on path {}", filePath, exception);
        } catch (JsonParseException exception) {
            GTLog.logger.error("Failed to extract json from file", exception);
        } catch (Exception exception) {
            GTLog.logger.error("Failed to extract json from file on path {}", filePath, exception);
        }

        return null;
    }

    public static JsonElement loadJson(File file) {
        try {
            if (!file.isFile()) return null;
            Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
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
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
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
            URI sampleUri = WorldGenRegistry.class.getResource("/assets/gregtech/.gtassetsroot").toURI();
            Path resourcePath;
            if (sampleUri.getScheme().equals("jar") || sampleUri.getScheme().equals("zip")) {
                zipFileSystem = FileSystems.newFileSystem(sampleUri, Collections.emptyMap());
                resourcePath = zipFileSystem.getPath(resource);
            } else if (sampleUri.getScheme().equals("file")) {
                resourcePath = Paths.get(WorldGenRegistry.class.getResource(resource).toURI());
            } else {
                throw new IllegalStateException("Unable to locate absolute path to directory: " + sampleUri);
            }

            List<Path> jarFiles = Files.walk(resourcePath)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
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
        FileSystem fs = FileSystems.getDefault();
        String separator = fs.getSeparator();

        //Remove the leading "folderName\"
        String[] tempName = name.split(Matcher.quoteReplacement(separator));
        //Take the last entry in case of nested folders
        String newName = tempName[tempName.length - 1];
        //Remove the ".json"
        tempName = newName.split("\\.");
        //Take the first entry
        newName = tempName[0];
        //Replace all "_" with a space
        newName = newName.replaceAll("_", " ");
        //Capitalize the first letter
        newName = newName.substring(0, 1).toUpperCase() + newName.substring(1);

        return newName;
    }
}
