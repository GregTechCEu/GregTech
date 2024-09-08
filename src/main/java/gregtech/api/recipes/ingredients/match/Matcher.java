package gregtech.api.recipes.ingredients.match;

import java.util.function.Predicate;

public interface Matcher<T> extends Predicate<T> {

    long getRequiredCount();
}
