package gregtech.api.mui.serialize;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.CompoundKey;
import com.cleanroommc.modularui.drawable.text.DynamicKey;
import com.cleanroommc.modularui.drawable.text.LangKey;
import com.cleanroommc.modularui.drawable.text.StringKey;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import org.apache.commons.lang3.ArrayUtils;

public class KeySerializer implements JsonHandler<IKey> {

    @Override
    public IKey deserialize(JsonElement json, JsonDeserializationContext context)
                                                                                  throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        if (object.has("string")) {
            return IKey.str(object.get("string").getAsString());
        } else if (object.has("lang")) {
            String lang = context.deserialize(object.get("lang"), String.class);
            TextFormatting[] formatting = deserializeArray(
                    object.getAsJsonArray("format"), context, TextFormatting[]::new);
            Object[] args = deserializeArray(
                    object.getAsJsonArray("args"), context, Object[]::new);
            return IKey.lang(lang, args).format(formatting);
        } else if (object.has("keys")) {
            IKey[] keys = deserializeArray(
                    object.getAsJsonArray("keys"), context, IKey[]::new);
            TextFormatting[] formatting = deserializeArray(
                    object.getAsJsonArray("format"), context, TextFormatting[]::new);
            return IKey.comp(keys).format(formatting);
        }
        return IKey.EMPTY;
    }

    @Override
    public JsonElement serialize(IKey src, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        if (src instanceof StringKey || src instanceof DynamicKey) {
            obj.add("string", context.serialize(src.getFormatted()));
        } else if (src instanceof LangKey langKey) {
            obj.add("lang", context.serialize(langKey.getKeySupplier().get()));
            obj.add("format", serializeArray(langKey.getFormatting(), context));
            Object[] args = langKey.getArgsSupplier().get();
            if (!ArrayUtils.isEmpty(args))
                obj.add("args", serializeArray(args, context));
        } else if (src instanceof CompoundKey compoundKey) {
            obj.add("keys", serializeArray(compoundKey.getKeys(), context));
            obj.add("format", context.serialize(compoundKey.getFormatting()));
        }
        return obj;
    }
}
