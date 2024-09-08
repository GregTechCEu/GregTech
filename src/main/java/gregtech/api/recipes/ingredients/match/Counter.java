package gregtech.api.recipes.ingredients.match;

import org.jetbrains.annotations.NotNull;

public interface Counter<T> {

    long count(@NotNull T obj);
}
