package gregtech.api.gui.resources;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ModifyGuiTexture implements IGuiTexture{
    public static List<String> TYPES = Arrays.asList("resource", "url", "text", "color", "file");
    private IGuiTexture texture;

    public ModifyGuiTexture(IGuiTexture texture) {
        this.texture = texture;
        if (texture == null) {
            this.texture = new TextTexture("texture.modify_gui_texture.missing", 0xff000000);
        }
    }

    public IGuiTexture getTexture() {
        return texture;
    }

    public void setTexture(IGuiTexture texture) {
        if (texture != null) {
            this.texture = texture;
        }
    }

    public String getTypeName() {
        if (texture instanceof TextureArea) {
            return "resource";
        } else if (texture instanceof URLTexture) {
            return "url";
        } else if (texture instanceof TextTexture) {
            return "text";
        } else if (texture instanceof ColorRectTexture) {
            return "color";
        } else if (texture instanceof FileTexture) {
            return "file";
        } else {
            return null;
        }
    }

    @Override
    public void draw(double x, double y, int width, int height) {
        texture.draw(x, y, width, height);
    }

    @Override
    public void updateTick() {
        texture.updateTick();
    }

    public JsonObject saveConfig() {
        JsonObject config = new JsonObject();
        if (texture instanceof TextureArea) {
            config.addProperty("type", "resource");
            config.addProperty("resource", ((TextureArea) texture).imageLocation.toString());
        } else if (texture instanceof URLTexture) {
            config.addProperty("type", "url");
            config.addProperty("url", ((URLTexture) texture).url);
        } else if (texture instanceof TextTexture) {
            config.addProperty("type", "text");
            config.addProperty("text", ((TextTexture) texture).text);
            config.addProperty("color", ((TextTexture) texture).color);
        } else if (texture instanceof ColorRectTexture) {
            config.addProperty("type", "color");
            config.addProperty("color", ((ColorRectTexture) texture).color);
        } else if (texture instanceof FileTexture) {
            config.addProperty("type", "file");
            if (((FileTexture) texture).file != null) {
                config.addProperty("file", ((FileTexture) texture).file.getPath());
            } else {
                config.addProperty("file", (String)null);
            }
        } else {
            return null;
        }
        return config;
    }

    public void loadConfig(JsonObject config) {
        try {
            switch (config.get("type").getAsString()) {
                case "resource":
                    setTexture(new TextureArea(new ResourceLocation(config.get("resource").getAsString()), 0.0, 0.0, 1.0, 1.0));
                case "url":
                    setTexture(new URLTexture(config.get("url").getAsString()));
                case "text":
                    setTexture(new TextTexture(config.get("text").getAsString(), config.get("color").getAsInt()));
                case "color":
                    setTexture(new ColorRectTexture(config.get("color").getAsInt()));
                case "file":
                    setTexture(new FileTexture(new File(config.get("file").getAsString())));
            }
        } catch (Exception ignored) {
        }
    }
}
