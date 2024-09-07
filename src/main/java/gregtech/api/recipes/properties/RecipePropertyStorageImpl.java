package gregtech.api.recipes.properties;

import gregtech.api.GregTechAPI;
import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.Set;

public final class RecipePropertyStorageImpl implements RecipePropertyStorage {

    private final Map<RecipeProperty<?>, Object> map;

    public RecipePropertyStorageImpl() {
        this(new Object2ObjectArrayMap<>(1));
    }

    private RecipePropertyStorageImpl(@NotNull Map<RecipeProperty<?>, Object> map) {
        this.map = map;
    }

    @Override
    public <T> boolean store(@NotNull RecipeProperty<T> recipeProperty, @NotNull T value) {
        String key = recipeProperty.getKey();
        if (map.containsKey(recipeProperty)) {
            GTLog.logger.warn("Duplicate recipe property added: {} -> {}", key, value, new Throwable());
            return false;
        }

        map.put(recipeProperty, value);
        return true;
    }

    @Override
    public <T> T remove(@NotNull RecipeProperty<T> recipeProperty) {
        return recipeProperty.castValue(map.remove(recipeProperty));
    }

    @Override
    public @NotNull RecipePropertyStorage copy() {
        return new RecipePropertyStorageImpl(new Object2ObjectArrayMap<>(this.map));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public @NotNull Set<Map.Entry<RecipeProperty<?>, Object>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    @Contract("_, !null -> !null")
    public <T> @Nullable T get(@NotNull RecipeProperty<T> recipeProperty, @Nullable T defaultValue) {
        var value = map.get(recipeProperty);
        if (value == null) {
            return defaultValue;
        }

        return recipeProperty.castValue(value);
    }

    @Override
    public boolean contains(@NotNull RecipeProperty<?> recipeProperty) {
        return map.containsKey(recipeProperty);
    }

    @Override
    @UnmodifiableView
    public @NotNull Set<@NotNull RecipeProperty<?>> values() {
        return map.keySet();
    }

    @Override
    public @NotNull String toString() {
        return "RecipePropertyStorage{" + map + '}';
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (var entry : map.entrySet()) {
            var property = entry.getKey();
            tag.setTag(property.getKey(), property.serialize(entry.getValue()));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound nbt) {
        for (var entry : nbt.tagMap.entrySet()) {
            var property = GregTechAPI.RECIPE_PROPERTIES.get(entry.getKey());
            if (property == null) {
                GTLog.logger.warn("Failed to read property with key {}", entry.getKey());
                continue;
            }
            map.put(property, property.deserialize(entry.getValue()));
        }
    }
}
