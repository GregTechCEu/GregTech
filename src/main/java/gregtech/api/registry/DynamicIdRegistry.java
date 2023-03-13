package gregtech.api.registry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Registry associating a key and numeric id with a value, and the reverse.
 * <p>
 * Numeric id is not guaranteed to be the same between runs. It is dynamically assigned.
 */
public class DynamicIdRegistry<K, V> extends GTSimpleRegistry<K, V> {

    private final AtomicInteger atomicId = new AtomicInteger();
    private final Object2IntMap<K> keyToId = new Object2IntOpenHashMap<>();
    private final Int2ObjectMap<V> idToValue = new Int2ObjectOpenHashMap<>();

    @Override
    public void putObject(@Nonnull K key, @Nonnull V value) {
        super.putObject(key, value);
        int id = atomicId.getAndIncrement();
        keyToId.put(key, id);
        idToValue.put(id, value);
    }

    /**
     * @param key the key for the value
     * @return the numeric id associated with the key.
     */
    public int getId(@Nonnull K key) {
        return keyToId.get(key);
    }

    /**
     * @param id the numeric id for the key
     * @return the value associated with the key
     */
    @Nullable
    public V getValueForId(int id) {
        return idToValue.get(id);
    }
}
