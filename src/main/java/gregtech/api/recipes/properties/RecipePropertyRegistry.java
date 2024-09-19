package gregtech.api.recipes.properties;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class RecipePropertyRegistry {

    private final Map<String, RecipeProperty<?>> map = new Object2ReferenceOpenHashMap<>();

    /**
     * @param key      the RecipeProperty's key
     * @param property the property's instance
     */
    public void register(@NotNull String key, @NotNull RecipeProperty<?> property) {
        if (map.containsKey(key)) {
            throw new IllegalArgumentException("RecipeProperty is already registered: " + key);
        }
        map.put(key, property);
    }

    @ApiStatus.Internal
    public @Nullable RecipeProperty<?> get(@NotNull String key) {
        return map.get(key);
    }
}
