package gregtech.api.items.materialitem;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * Generates files of materialset models from their textures, in short.
 */
public class MaterialSetModelGen {

    private static final String PATTERN_NO_OVERLAY =
            "{\n" +
                    "    \"parent\": \"item/generated\",\n" +
                    "    \"textures\": {\n" +
                    "        \"layer0\": \"$MAIN$\"\n" +
                    "    }\n" +
                    "}";

    private static final String PATTERN_WITH_OVERLAY =
            "{\n" +
                    "    \"parent\": \"item/generated\",\n" +
                    "    \"textures\": {\n" +
                    "        \"layer0\": \"$MAIN$\",\n" +
                    "        \"layer1\": \"$OVERLAY$\"\n" +
                    "    }\n" +
                    "}";

    public static void main(String[] args) throws Exception {
        File gregtechRoot = new File("src/main/resources/assets/gregtech");
        File modelsRoot = new File(gregtechRoot, "models/item/material_sets");
        File texturesRoot = new File(gregtechRoot, "textures/items/material_sets");
        modelsRoot.mkdirs();
        for (File file : texturesRoot.listFiles()) {
            if (file.isDirectory()) {
                File modelsDir = new File(modelsRoot, file.getName());
                modelsDir.mkdirs();
                for (File child : file.listFiles()) {
                    String name = child.getName();
                    if (name.endsWith(".png") && !name.endsWith("_overlay.png")) {
                        name = name.substring(0, name.length() - 4);
                        File childOverlay = new File(file, name + "_overlay.png");
                        File modelFile = new File(modelsDir, name + ".json");
                        String texturePath = "gregtech:items/material_sets/" + file.getName() + "/" + name;
                        if (childOverlay.exists()) {
                            String overlayPath = texturePath + "_overlay";
                            String data = PATTERN_WITH_OVERLAY.replace("$MAIN$", texturePath).replace("$OVERLAY$", overlayPath);
                            Files.write(modelFile.toPath(), Arrays.asList(data.split("\n")));
                        } else {
                            String data = PATTERN_NO_OVERLAY.replace("$MAIN$", texturePath);
                            Files.write(modelFile.toPath(), Arrays.asList(data.split("\n")));
                        }
                    }
                }
            }
        }

    }

}
