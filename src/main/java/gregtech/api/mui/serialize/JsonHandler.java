package gregtech.api.mui.serialize;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.IntFunction;

public interface JsonHandler<T> extends JsonSerializer<T>, JsonDeserializer<T> {

    @Override
    default JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return serialize(src, context);
    }

    @Override
    default T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                                                                                              throws JsonParseException {
        return deserialize(json, context);
    }

    JsonElement serialize(T src, JsonSerializationContext context);

    T deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException;

    default <R> JsonArray serializeArray(R[] objects, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        Type arrayType = objects.getClass().getComponentType();
        for (R t : objects) {
            array.add(context.serialize(t, arrayType));
        }
        return array;
    }

    default <R> R[] deserializeArray(JsonArray jsonElements, JsonDeserializationContext context,
                                     IntFunction<R[]> function) {
        if (jsonElements == null) return function.apply(0);
        R[] array2 = function.apply(jsonElements.size());
        Type arrayType = array2.getClass().getComponentType();
        Arrays.setAll(array2, i -> handleArg(jsonElements.get(i), context, arrayType));
        return array2;
    }

    static Object handleArg(JsonElement element, JsonDeserializationContext context, Type arrayType) {
        // args can sometimes be keys
        if (element.isJsonObject()) {
            return context.deserialize(element.getAsJsonObject(), IDrawable.class);
        } else if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
            return primitive.getAsNumber();
        } else {
            return context.deserialize(element, arrayType);
        }
    }
}
