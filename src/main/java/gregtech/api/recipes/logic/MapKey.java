package gregtech.api.recipes.logic;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

// As there are a known (and small) number of these that will be defined,
// the map should be replaced with a custom implementation that has a pseudo-fixed-size array and each
// key is associated with a specific position in the array for optimized read/write speed.
// primitive type keys should be associated with their own primitive arrays as well, to avoid wrapping.
public class MapKey<T> {

    public MapKey() {}

    public T put(Map<MapKey<?>, Object> map, T value) {
        return (T) map.put(this, value);
    }

    public T get(Map<MapKey<?>, Object> map) {
        return (T) map.get(this);
    }

    public T getOrDefault(Map<MapKey<?>, Object> map, T defVal) {
        Object t = map.get(this);
        if (t != null || map.containsKey(this)) {
            return (T) t;
        }
        return defVal;
    }

    public @NotNull T getNonnull(Map<MapKey<?>, Object> map, @NotNull T defVal) {
        Object t = map.get(this);
        if (t != null) {
            return (T) t;
        }
        return defVal;
    }

    public static class LongKey extends MapKey<Long> {

        public long putLong(Map<MapKey<?>, Object> map, long value) {
            return putLong(map, value, 0);
        }

        public long putLong(Map<MapKey<?>, Object> map, long value, long defOldVal) {
            Object t = map.put(this, value);
            if (t == null) return defOldVal;
            return (Long) t;
        }

        public long getLong(Map<MapKey<?>, Object> map) {
            return getLong(map, 0);
        }

        public long getLong(Map<MapKey<?>, Object> map, long defVal) {
            Object t = map.get(this);
            if (t == null) return defVal;
            return (Long) t;
        }
    }

    public static class IntKey extends MapKey<Integer> {

        public int putInt(Map<MapKey<?>, Object> map, int value) {
            return putInt(map, value, 0);
        }

        public int putInt(Map<MapKey<?>, Object> map, int value, int defOldVal) {
            Object t = map.put(this, value);
            if (t == null) return defOldVal;
            return (Integer) t;
        }

        public int getInt(Map<MapKey<?>, Object> map) {
            return getInt(map, 0);
        }

        public int getInt(Map<MapKey<?>, Object> map, int defVal) {
            Object t = map.get(this);
            if (t == null) return defVal;
            return (Integer) t;
        }
    }

    public static class DoubleKey extends MapKey<Double> {

        public double putDouble(Map<MapKey<?>, Object> map, double value) {
            return putDouble(map, value, 0);
        }

        public double putDouble(Map<MapKey<?>, Object> map, double value, double defOldVal) {
            Object t = map.put(this, value);
            if (t == null) return defOldVal;
            return (Double) t;
        }

        public double getDouble(Map<MapKey<?>, Object> map) {
            return getDouble(map, 0);
        }

        public double getDouble(Map<MapKey<?>, Object> map, double defVal) {
            Object t = map.get(this);
            if (t == null) return defVal;
            return (Double) t;
        }
    }
}
