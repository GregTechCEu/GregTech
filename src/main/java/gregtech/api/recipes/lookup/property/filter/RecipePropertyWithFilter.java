package gregtech.api.recipes.lookup.property.filter;

import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.recipes.properties.RecipePropertyStorage;

import org.jetbrains.annotations.NotNull;

public abstract class RecipePropertyWithFilter<T> extends RecipeProperty<T> implements IPropertyFilter<T> {

    protected RecipePropertyWithFilter(String key, Class<T> type) {
        super(key, type);
    }

    public abstract boolean matches(PropertySet properties, T value);

    public static boolean matches(@NotNull RecipePropertyStorage storage, @NotNull PropertySet properties) {
        for (var entry : storage.entrySet()) {
            if (entry.getKey() instanceof RecipePropertyWithFilter<?>filter) {
                if (!matches(storage, filter, properties)) return false;
            }
        }
        return true;
    }

    private static <T> boolean matches(@NotNull RecipePropertyStorage storage,
                                       @NotNull RecipePropertyWithFilter<T> filter, @NotNull PropertySet properties) {
        T value = storage.get(filter, null);
        assert value != null;
        return filter.matches(properties, value);
    }
}
