package gregtech.api.recipes.recipeproperties;

import java.util.Map;
import java.util.Set;

public interface IRecipePropertyStorage {

    String STACKTRACE = "Stacktrace:";

    /**
     * Stores new {@link RecipeProperty} with value
     *
     * @param recipeProperty {@link RecipeProperty}
     * @param value          value
     * @return {@code true} if store succeeds; otherwise {@code false}
     */
    boolean store(RecipeProperty<?> recipeProperty, Object value);

    boolean remove(RecipeProperty<?> recipeProperty);

    void freeze(boolean frozen);

    IRecipePropertyStorage copy();

    /**
     * Provides information how many {@link RecipeProperty} are stored
     *
     * @return number of stored {@link RecipeProperty}
     */
    int getSize();

    /**
     * Provides all stored {@link RecipeProperty}
     *
     * @return all stored {@link RecipeProperty} and values
     */
    Set<Map.Entry<RecipeProperty<?>, Object>> getRecipeProperties();

    /**
     * Provides casted value for one specific {@link RecipeProperty} if is stored or defaultValue
     *
     * @param recipeProperty {@link RecipeProperty}
     * @param defaultValue   Default value if recipeProperty is not found
     * @param <T>            Type of returned value
     * @return value tied with provided recipeProperty on success; otherwise defaultValue
     */
    <T> T getRecipePropertyValue(RecipeProperty<T> recipeProperty, T defaultValue);

    boolean hasRecipeProperty(RecipeProperty<?> recipeProperty);

    Set<String> getRecipePropertyKeys();

    Set<RecipeProperty<?>> getPropertyTypes();

    /**
     * Provides un-casted value for one specific {@link RecipeProperty} searched by key
     *
     * @param key Key of stored {@link RecipeProperty}
     * @return {@link Object} value on success; otherwise {@code null}
     */
    Object getRawRecipePropertyValue(String key);
}
