package gregtech.api.util;

import gregtech.api.mui.serialize.DrawableSerializer;
import gregtech.api.mui.serialize.FormatSerializer;
import gregtech.api.mui.serialize.KeySerializer;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(IKey.class, new KeySerializer())
            .registerTypeAdapter(IDrawable.class, new DrawableSerializer())
            .registerTypeAdapter(TextFormatting.class, new FormatSerializer())
            .create();

    public static String toJsonString(IDrawable drawable) {
        return getGson().toJson(drawable, IDrawable.class);
    }

    public static Gson getGson() {
        return gson;
    }

    public static IDrawable fromJsonString(String jsonString) {
        return getGson().fromJson(jsonString, IDrawable.class);
    }
}
