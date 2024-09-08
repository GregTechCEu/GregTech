package gregtech.api.recipes.lookup.flag;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Object2ApplicatorMapMap<T> extends Object2ObjectOpenCustomHashMap<T, FlagApplicatorMap<T>> {

    @SuppressWarnings("rawtypes")
    private static final IApplicatorMap EMPTY = (f, c) -> {};

    @SuppressWarnings("unchecked")
    private static <T> IApplicatorMap<T> getEmpty() {
        return (IApplicatorMap<T>) EMPTY;
    }

    public Object2ApplicatorMapMap(int expected, float f, Strategy<T> strategy) {
        super(expected, f, strategy);
    }

    public Object2ApplicatorMapMap(int expected, Strategy<T> strategy) {
        super(expected, strategy);
    }

    public Object2ApplicatorMapMap(Strategy<T> strategy) {
        super(strategy);
    }

    public Object2ApplicatorMapMap(Map<? extends T, ? extends FlagApplicatorMap<T>> m, float f, Strategy<T> strategy) {
        super(m, f, strategy);
    }

    public Object2ApplicatorMapMap(Map<? extends T, ? extends FlagApplicatorMap<T>> m, Strategy<T> strategy) {
        super(m, strategy);
    }

    public Object2ApplicatorMapMap(Object2ObjectMap<T, FlagApplicatorMap<T>> m, float f, Strategy<T> strategy) {
        super(m, f, strategy);
    }

    public Object2ApplicatorMapMap(Object2ObjectMap<T, FlagApplicatorMap<T>> m, Strategy<T> strategy) {
        super(m, strategy);
    }

    public Object2ApplicatorMapMap(T[] k, FlagApplicatorMap<T>[] v, float f, Strategy<T> strategy) {
        super(k, v, f, strategy);
    }

    public Object2ApplicatorMapMap(T[] k, FlagApplicatorMap<T>[] v, Strategy<T> strategy) {
        super(k, v, strategy);
    }

    public @NotNull FlagApplicatorMap<T> getOrCreate(T key) {
        FlagApplicatorMap<T> fetch = get(key);
        if (fetch == null) {
            fetch = new FlagApplicatorMap<>();
            put(key, fetch);
        }
        return fetch;
    }

    public @NotNull IApplicatorMap<T> getApplicator(T key) {
        IApplicatorMap<T> fetch = get(key);
        return fetch == null ? getEmpty() : fetch;
    }
}
