package gregtech.api.recipes.ingredients.match;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.Range;

import java.util.function.Predicate;

public interface Matcher<T> {

    /**
     * @return Whether an object matches, independent of count.
     */
    boolean matches(T t);

    /**
     * @return the count required to match
     */
    @Range(from = 1, to = Long.MAX_VALUE)
    long getRequiredCount();

    static <T> Matcher<T> simpleMatcher(Predicate<T> predicate, @Range(from = 1, to = Long.MAX_VALUE) long count) {
        return new SimpleMatcher<>(predicate, count);
    }

    @Desugar
    record SimpleMatcher<T> (Predicate<T> predicate, @Range(from = 1, to = Long.MAX_VALUE) long count)
            implements Matcher<T> {

        @Override
        public @Range(from = 1, to = Long.MAX_VALUE) long getRequiredCount() {
            return count;
        }

        @Override
        public boolean matches(T t) {
            return predicate.test(t);
        }
    }
}
