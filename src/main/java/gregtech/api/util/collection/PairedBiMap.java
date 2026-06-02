package gregtech.api.util.collection;

import com.google.common.collect.BiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * An implementation of BiMap that pairs together a forward and backward version of normal maps to operate.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class PairedBiMap<K, V> implements BiMap<K, V> {

    private static final int DEFAULT_SIZE = 16;

    private final Map<K, V> forwardMap;
    private final Map<V, K> backwardMap;

    private @Nullable BiMap<V, K> reverse;

    protected final boolean byReference;

    public PairedBiMap(MapFunction function) {
        this(function, DEFAULT_SIZE);
    }

    public PairedBiMap(@NotNull MapFunction function, int size) {
        this(function, size, false);
    }

    public PairedBiMap(@NotNull MapFunction function, int size, boolean byReference) {
        forwardMap = function.createMap(size);
        backwardMap = function.createMap(size);
        this.byReference = byReference;
    }

    protected PairedBiMap(@NotNull PairedBiMap<V, K> reverse, @NotNull Map<K, V> forwardMap,
                          @NotNull Map<V, K> backwardMap) {
        this.reverse = reverse;
        this.forwardMap = forwardMap;
        this.backwardMap = backwardMap;
        this.byReference = reverse.byReference;
    }

    @Override
    public int size() {
        return forwardMap.size();
    }

    @Override
    public boolean isEmpty() {
        return forwardMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return forwardMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backwardMap.containsKey(value);
    }

    @Override
    public V get(Object key) {
        return forwardMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        K existing = backwardMap.get(value);
        // throw if this value is already associated with a different key
        if (existing != null && !equivalent(existing, key)) {
            throw new IllegalArgumentException("Value already exists in BiMap!");
        }
        V old = forwardMap.put(key, value);
        backwardMap.put(value, key);
        return old;
    }

    @Override
    public V remove(Object key) {
        V rem = forwardMap.remove(key);
        backwardMap.remove(rem);
        return rem;
    }

    @Override
    public V forcePut(K key, V value) {
        backwardMap.remove(value);
        return put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (var entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        forwardMap.clear();
        backwardMap.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return forwardMap.keySet();
    }

    @Override
    public Set<V> values() {
        return backwardMap.keySet();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return forwardMap.entrySet();
    }

    @Override
    public BiMap<V, K> inverse() {
        if (reverse == null) reverse = new PairedBiMap<>(this, backwardMap, forwardMap);
        return reverse;
    }

    private boolean equivalent(Object a, Object b) {
        if (byReference) return a == b;
        else return a.equals(b);
    }
}
