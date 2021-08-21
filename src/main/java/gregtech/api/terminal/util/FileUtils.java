package gregtech.api.terminal.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileUtils {
    public static JsonElement loadJson(File file) {
        try {
            FileReader reader = new FileReader(file);
            JsonElement json = new JsonParser().parse(new JsonReader(reader));
            reader.close();
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean saveJson(File file, JsonElement element) {
        try {
            if (!file.getParentFile().isDirectory()) {
                file.getParentFile().mkdirs();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(new Gson().toJson(element));
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
