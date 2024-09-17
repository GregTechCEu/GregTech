package gregtech.api.recipes.ingredients.match;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Counter<T> {

    long count(@NotNull T obj);

    @Contract("_, _ -> new")
    T withCount(T value, long count);
}
