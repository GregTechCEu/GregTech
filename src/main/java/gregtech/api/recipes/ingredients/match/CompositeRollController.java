package gregtech.api.recipes.ingredients.match;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.List;

public class CompositeRollController<T extends Matcher<?>> extends AbstractList<T> implements MatchRollController<T> {

    protected final @NotNull List<T> unrolled;
    protected final @NotNull MatchRollController<T> rolled;

    public CompositeRollController(@NotNull List<T> unrolled, @NotNull MatchRollController<T> rolled) {
        this.unrolled = unrolled;
        this.rolled = rolled;
    }

    public @NotNull List<T> getUnrolled() {
        return unrolled;
    }

    public @NotNull MatchRollController<T> getRolled() {
        return rolled;
    }

    @Override
    public long @NotNull [] getConsumptionRollResults(int scale) {
        long[] result = new long[size()];
        int offset = unrolled.size();
        for (int i = 0; i < offset; i++) {
            result[i] = unrolled.get(i).getRequiredCount() * scale;
        }
        long[] roll = rolled.getConsumptionRollResults(scale);
        System.arraycopy(roll, 0, result, offset, roll.length);
        return result;
    }

    @Override
    public MatchRollController<T> copy() {
        return new CompositeRollController<>(new ObjectArrayList<>(unrolled), rolled.copy());
    }

    @Override
    public T get(int index) {
        return index < unrolled.size() ? unrolled.get(index) : rolled.get(index - unrolled.size());
    }

    @Override
    public int size() {
        return unrolled.size() + rolled.size();
    }
}
