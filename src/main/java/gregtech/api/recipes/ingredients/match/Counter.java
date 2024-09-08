package gregtech.api.recipes.ingredients.match;

public interface Counter<T> {

    long count(T obj);
}
