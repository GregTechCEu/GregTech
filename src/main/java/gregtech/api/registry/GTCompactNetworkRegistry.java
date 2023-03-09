package gregtech.api.registry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import javax.annotation.Nonnull;

public class GTCompactNetworkRegistry<K, V> extends GTSimpleRegistry<K, V> {

    private final Int2ObjectMap<V> hashToValue = new Int2ObjectOpenHashMap<>();

    @Override
    public void putObject(@Nonnull K key, @Nonnull V value) {
        super.putObject(key, value);
        hashToValue.put(key.hashCode(), value);
    }

    public V getObject(int hash) {
        return hashToValue.get(hash);
    }
}
