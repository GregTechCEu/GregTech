package gregtech.api.terminal.os;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.ModifyGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.FileUtility;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.awt.*;
import java.io.File;

import static gregtech.api.terminal.TerminalRegistry.TERMINAL_PATH;

public class TerminalTheme {
    private static final String FILE_PATH = "config/theme.json";
    public static final ColorRectTexture COLOR_1 = new ColorRectTexture(new Color(144, 243, 116));
    public static final ColorRectTexture COLOR_2 = new ColorRectTexture(new Color(243, 208, 116));
    public static final ColorRectTexture COLOR_3 = new ColorRectTexture(new Color(231, 95, 95));
    public static final ColorRectTexture COLOR_4 = new ColorRectTexture(new Color(0, 115, 255));
    public static final ColorRectTexture COLOR_5 = new ColorRectTexture(new Color(113, 27, 217));
    public static final ColorRectTexture COLOR_6 = new ColorRectTexture(new Color(30, 30, 30, 255));
    public static final ColorRectTexture COLOR_7 = new ColorRectTexture(new Color(230, 230, 230, 255));

    public static final ColorRectTexture COLOR_F_1 = new ColorRectTexture(new Color(148, 226, 193));
    public static final ColorRectTexture COLOR_F_2 = new ColorRectTexture(new Color(175, 0, 0, 131));

    public static final ColorRectTexture COLOR_B_1 = new ColorRectTexture(new Color(0, 0, 0, 80));
    public static final ColorRectTexture COLOR_B_2 = new ColorRectTexture(new Color(0, 0, 0, 160));
    public static final ColorRectTexture COLOR_B_3 = new ColorRectTexture(new Color(246, 120, 120, 160));

    public static final ModifyGuiTexture WALL_PAPER = new ModifyGuiTexture(TextureArea.fullImage("textures/gui/terminal/terminal_background.png"));

    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            JsonElement element = FileUtility.loadJson(new File(TERMINAL_PATH, FILE_PATH));
            if (element == null || !element.isJsonObject()) {
                saveConfig();
            } else {
                JsonObject config = element.getAsJsonObject();
                if (config.has("COLOR_1")) { COLOR_1.setColor(config.get("COLOR_1").getAsInt()); }
                if (config.has("COLOR_2")) { COLOR_2.setColor(config.get("COLOR_2").getAsInt()); }
                if (config.has("COLOR_3")) { COLOR_3.setColor(config.get("COLOR_3").getAsInt()); }
                if (config.has("COLOR_4")) { COLOR_4.setColor(config.get("COLOR_4").getAsInt()); }
                if (config.has("COLOR_5")) { COLOR_5.setColor(config.get("COLOR_5").getAsInt()); }
                if (config.has("COLOR_6")) { COLOR_6.setColor(config.get("COLOR_6").getAsInt()); }
                if (config.has("COLOR_7")) { COLOR_7.setColor(config.get("COLOR_7").getAsInt()); }
                if (config.has("COLOR_F_1")) { COLOR_F_1.setColor(config.get("COLOR_F_1").getAsInt()); }
                if (config.has("COLOR_F_2")) { COLOR_F_2.setColor(config.get("COLOR_F_2").getAsInt()); }
                if (config.has("COLOR_B_1")) { COLOR_B_1.setColor(config.get("COLOR_B_1").getAsInt()); }
                if (config.has("COLOR_B_2")) { COLOR_B_2.setColor(config.get("COLOR_B_2").getAsInt()); }
                if (config.has("COLOR_B_3")) { COLOR_B_3.setColor(config.get("COLOR_B_3").getAsInt()); }
                if (config.has("WALL_PAPER")) { WALL_PAPER.loadConfig(config.get("WALL_PAPER").getAsJsonObject()); }
            }
        }
    }

    public static boolean saveConfig() {
        JsonObject config = new JsonObject();
        config.addProperty("COLOR_1", COLOR_1.getColor());
        config.addProperty("COLOR_2", COLOR_2.getColor());
        config.addProperty("COLOR_3", COLOR_3.getColor());
        config.addProperty("COLOR_4", COLOR_4.getColor());
        config.addProperty("COLOR_5", COLOR_5.getColor());
        config.addProperty("COLOR_6", COLOR_6.getColor());
        config.addProperty("COLOR_7", COLOR_7.getColor());
        config.addProperty("COLOR_F_1", COLOR_F_1.getColor());
        config.addProperty("COLOR_F_2", COLOR_F_2.getColor());
        config.addProperty("COLOR_B_1", COLOR_B_1.getColor());
        config.addProperty("COLOR_B_2", COLOR_B_2.getColor());
        config.addProperty("COLOR_B_3", COLOR_B_3.getColor());
        config.add("WALL_PAPER", WALL_PAPER.saveConfig());
        return FileUtility.saveJson(new File(TERMINAL_PATH, FILE_PATH), config);
    }
}
