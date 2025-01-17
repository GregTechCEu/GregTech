package gregtech.api.mui.serialize;

import gregtech.api.mui.drawables.HoverableKey;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.util.ArrayList;
import java.util.List;

public class DrawableSerializer implements JsonHandler<IDrawable> {

    @Override
    public IDrawable deserialize(JsonElement json, JsonDeserializationContext context)
                                                                                       throws JsonParseException {
        if (!json.isJsonObject()) return IDrawable.EMPTY;
        JsonObject parsed = json.getAsJsonObject();
        if (parsed.has("key") && parsed.has("tooltip")) {
            IKey key = context.deserialize(parsed.get("key"), IKey.class);

            List<IDrawable> list = new ArrayList<>();
            for (JsonElement jsonElement : parsed.getAsJsonArray("tooltip")) {
                list.add(context.deserialize(jsonElement, IDrawable.class));
            }
            return HoverableKey.of(key).addLines(list);
        } else {
            return context.deserialize(parsed, IKey.class);
        }
    }

    @Override
    public JsonElement serialize(IDrawable src, JsonSerializationContext context) {
        if (src instanceof IKey) return context.serialize(src, IKey.class);
        JsonObject object = new JsonObject();
        if (src instanceof HoverableKey hoverable) {
            object.add("key", context.serialize(hoverable.getKey(), IKey.class));
            JsonArray array = new JsonArray();
            for (IDrawable tooltipLine : hoverable.getTooltipLines()) {
                array.add(context.serialize(tooltipLine, IDrawable.class));
            }
            object.add("tooltip", array);
            return object;
        }
        return object;
    }
}
