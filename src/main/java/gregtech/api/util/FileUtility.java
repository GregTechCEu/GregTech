package gregtech.api.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtility {
    public static final JsonParser jsonParser = new JsonParser();
    public static final Gson gson = new Gson();

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
            FileReader reader = new FileReader(file);
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
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(element));
            writer.close();
            return true;
        } catch (Exception e) {
            GTLog.logger.error("Failed to save file on path {}", file, e);
        }
        return false;
    }

    public static void extractJarFiles(Path resourcePath, Path targetPath, boolean replace) throws IOException { //terminal/guide
        List<Path> jarFiles = Files.walk(resourcePath)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
        for (Path jarFile : jarFiles) {
            Path genPath = targetPath.resolve(resourcePath.relativize(jarFile).toString());
            Files.createDirectories(genPath.getParent());
            if (replace || !genPath.toFile().isFile()) {
                Files.copy(jarFile, genPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
