package gregtech.api.recipes.recipeproperties;

import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecipePropertyStorage implements IRecipePropertyStorage {

    private final Map<RecipeProperty<?>, Object> recipeProperties;

    private boolean frozen = false;

    public RecipePropertyStorage() {
        recipeProperties = new Object2ObjectArrayMap<>(1);
    }

    private RecipePropertyStorage(Map<RecipeProperty<?>, Object> recipeProperties) {
        this();
        this.recipeProperties.putAll(recipeProperties);
    }

    @Override
    public boolean store(RecipeProperty<?> recipeProperty, Object value) {
        boolean success = true;
        String key = recipeProperty.getKey();
        if (frozen) {
            GTLog.logger.warn("Unable to add RecipeProperty with key {} as the storage is frozen", key);
            success = false;
        }
        for (RecipeProperty<?> existingRecipeProperty : recipeProperties.keySet()) {
            if (existingRecipeProperty.getKey().equals(key)) {
                GTLog.logger.warn("Unable to add RecipeProperty with key {} as it already exists", key);
                success = false;
            }
        }

        if (value == null) {
            GTLog.logger.warn("Provided value is null for RecipeProperty with key {}", key);
            success = false;
        }

        try {
            recipeProperty.castValue(value);
        } catch (ClassCastException ex) {
            GTLog.logger.warn("Provided incorrect value for RecipeProperty with key {}", key);
            GTLog.logger.warn("Full exception:", ex);
            success = false;
        }

        if (success) {
            recipeProperties.put(recipeProperty, value);
        } else {
            GTLog.logger.warn(STACKTRACE, new IllegalArgumentException());
        }

        return success;
    }

    @Override
    public boolean remove(RecipeProperty<?> recipeProperty) {
        return this.recipeProperties.remove(recipeProperty) != null;
    }

    @Override
    public void freeze(boolean frozen) {
        this.frozen = frozen;
    }

    @Override
    public IRecipePropertyStorage copy() {
        return new RecipePropertyStorage(this.recipeProperties);
    }

    @Override
    public int getSize() {
        return recipeProperties.size();
    }

    @Override
    public Set<Map.Entry<RecipeProperty<?>, Object>> getRecipeProperties() {
        return this.recipeProperties.entrySet();
    }

    @Override
    public <T> T getRecipePropertyValue(RecipeProperty<T> recipeProperty, T defaultValue) {
        Object value = recipeProperties.get(recipeProperty);

        if (value == null) {
            if (defaultValue == null) {
                return null;
            }
            GTLog.logger.warn("There is no property with key {}", recipeProperty.getKey());
            GTLog.logger.warn(STACKTRACE, new IllegalArgumentException());
            return defaultValue;
        }

        return recipeProperty.castValue(value);
    }

    public boolean hasRecipeProperty(RecipeProperty<?> recipeProperty) {
        return recipeProperties.containsKey(recipeProperty);
    }

    @Override
    public Set<String> getRecipePropertyKeys() {
        HashSet<String> keys = new HashSet<>();

        recipeProperties.keySet().forEach(recipeProperty -> keys.add(recipeProperty.getKey()));

        return keys;
    }

    @Override
    public Object getRawRecipePropertyValue(String key) {
        RecipeProperty<?> recipeProperty = getRecipePropertyValue(key);
        if (recipeProperty != null) {
            return recipeProperties.get(recipeProperty);
        }

        return null;
    }

    private RecipeProperty<?> getRecipePropertyValue(String key) {
        for (RecipeProperty<?> recipeProperty : recipeProperties.keySet()) {
            if (recipeProperty.getKey().equals(key))
                return recipeProperty;
        }

        GTLog.logger.warn("There is no property with key {}", key);
        GTLog.logger.warn(STACKTRACE, new IllegalArgumentException());

        return null;
    }

}
