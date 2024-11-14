package gregtech.api.recipes.roll;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.ToLongFunction;

@Unmodifiable
public final class ListWithRollInformation<T> extends AbstractList<T> {

    private final @NotNull ToLongFunction<@NotNull T> yieldCounter;

    private final T @NotNull [] unrolled;

    private final T @NotNull [] rolled;
    private final long @NotNull [] rollValues;
    private final long @NotNull [] rollBoosts;

    private final @NotNull RollInterpreter interpreter;

    @SuppressWarnings("unchecked")
    public ListWithRollInformation(@NotNull ToLongFunction<@NotNull T> yieldCounter,
                                   @NotNull Collection<@NotNull T> unrolled,
                                   @NotNull Collection<@NotNull RollInformation<@NotNull T>> rolled,
                                   @NotNull RollInterpreter interpreter) {
        this.yieldCounter = yieldCounter;
        this.unrolled = (T[]) unrolled.toArray();
        this.rolled = (T[]) rolled.stream().map(RollInformation::value).toArray();
        this.rollValues = rolled.stream().mapToLong(RollInformation::rollValue).toArray();
        this.rollBoosts = rolled.stream().mapToLong(RollInformation::rollBoost).toArray();
        this.interpreter = interpreter;
    }

    private long @NotNull [] maxYields(int trimLimit) {
        trimLimit = Math.min(trimLimit, rolled.length);
        long[] value = new long[trimLimit];
        for (int i = 0; i < trimLimit; i++) {
            value[i] = yieldCounter.applyAsLong(rolled[i]);
        }
        return value;
    }

    private long @NotNull [] rollValues(int trimLimit) {
        trimLimit = Math.min(trimLimit, rollValues.length);
        long[] value = new long[trimLimit];
        System.arraycopy(rollValues, 0, value, 0, trimLimit);
        return value;
    }

    private long @NotNull [] rollBoosts(int trimLimit) {
        trimLimit = Math.min(trimLimit, rollBoosts.length);
        long[] value = new long[trimLimit];
        System.arraycopy(rollBoosts, 0, value, 0, trimLimit);
        return value;
    }

    public T @NotNull [] getUnrolled() {
        return unrolled;
    }

    public T @NotNull [] getRolled() {
        return rolled;
    }

    @Contract("->new")
    public @NotNull ObjectArrayList<RollInformation<T>> recomposeRolled() {
        ObjectArrayList<RollInformation<T>> list = new ObjectArrayList<>(rolled.length);
        for (int i = 0; i < rolled.length; i++) {
            list.add(new RollInformation<>(rolled[i], rollValues[i], rollBoosts[i]));
        }
        return list;
    }

    public long @NotNull [] comprehensiveRoll(int boostStrength, int trimLimit) {
        trimLimit = Math.min(trimLimit, size());
        long[] yield = new long[trimLimit];
        int m = Math.min(trimLimit, unrolled.length);
        for (int i = 0; i < m; i++) {
            yield[i] = yieldCounter.applyAsLong(unrolled[i]);
        }
        trimLimit -= m;
        if (trimLimit == 0) return yield;
        long[] roll = roll(boostStrength, trimLimit);
        System.arraycopy(roll, 0, yield, m, roll.length);
        return yield;
    }

    public long @NotNull [] roll(int boostStrength, int trimLimit) {
        return interpreter.interpretAndRoll(maxYields(trimLimit), rollValues(trimLimit), rollBoosts(trimLimit),
                boostStrength);
    }

    public long @NotNull [] roll(@NotNull RollInterpreter interpreterOverride, int boostStrength, int trimLimit) {
        return interpreterOverride.interpretAndRoll(maxYields(trimLimit), rollValues(trimLimit), rollBoosts(trimLimit),
                boostStrength);
    }

    @Override
    public T get(int index) {
        return index >= unrolled.length ? rolled[index - unrolled.length] : unrolled[index];
    }

    public @NotNull ToLongFunction<@NotNull T> getYieldCounter() {
        return yieldCounter;
    }

    public @NotNull RollInterpreter getInterpreter() {
        return interpreter;
    }

    @Override
    public int size() {
        return unrolled.length + rolled.length;
    }

    public boolean hasRolledEntries() {
        return rolled.length > 0;
    }

    private static final ListWithRollInformation<Object> EMPTY = new ListWithRollInformation<>(o -> 0,
            Collections.emptyList(), Collections.emptyList(), RollInterpreter.DEFAULT);

    public static <T> ListWithRollInformation<T> empty() {
        // noinspection unchecked
        return (ListWithRollInformation<T>) EMPTY;
    }
}
