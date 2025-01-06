package gregtech.api.recipes.properties;

import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface RecipePropertyStorage {

    /**
     * @param recipeProperty the property to store
     * @param value          the value to store
     * @return if the store succeeds
     */
    boolean store(@NotNull RecipeProperty<?> recipeProperty, @NotNull Object value);

    /**
     * @return a copy of this property storage
     */
    @NotNull
    RecipePropertyStorage copy();

    /**
     * @return number of stored properties
     */
    int size();

    /**
     * @return all stored properties and values
     */
    @NotNull
    Set<Map.Entry<RecipeProperty<?>, Object>> entrySet();

    /**
     * @param recipeProperty the property to retrieve
     * @param defaultValue   default value if the property is not found
     * @param <T>            the type of returned value
     * @return value associated with the provided recipeProperty, otherwise the default
     */
    @Contract("_, !null -> !null")
    <T> @Nullable T get(@NotNull RecipeProperty<T> recipeProperty, @Nullable T defaultValue);

    /**
     * @param recipeProperty the property to check
     * @return if the property is in this storage
     */
    boolean contains(@NotNull RecipeProperty<?> recipeProperty);

    /**
     * @return the recipe property values
     */
    @UnmodifiableView
    @NotNull
    Set<@NotNull RecipeProperty<?>> values();

    @NotNull
    NBTTagCompound serializeNBT();

    void deserializeNBT(@NotNull NBTTagCompound nbt);

    RecipePropertyStorage EMPTY = new RecipePropertyStorage() {

        @Override
        public boolean store(@NotNull RecipeProperty<?> recipeProperty, @NotNull Object value) {
            throw new UnsupportedOperationException("empty");
        }

        @Override
        public @NotNull RecipePropertyStorage copy() {
            return this;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public @NotNull Set<Map.Entry<RecipeProperty<?>, Object>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public <T> @Nullable T get(@NotNull RecipeProperty<T> recipeProperty, @Nullable T defaultValue) {
            return defaultValue;
        }

        @Override
        public boolean contains(@NotNull RecipeProperty<?> recipeProperty) {
            return false;
        }

        @Override
        public @UnmodifiableView @NotNull Set<@NotNull RecipeProperty<?>> values() {
            return Collections.emptySet();
        }

        @Override
        public @NotNull NBTTagCompound serializeNBT() {
            return new NBTTagCompound();
        }

        @Override
        public void deserializeNBT(@NotNull NBTTagCompound nbt) {
            if (!nbt.isEmpty()) {
                GTLog.logger.warn("Tried to deserialize non-empty tag in RecipePropertyStorage.EMPTY: {}", nbt,
                        new Throwable());
            }
        }
    };
}
