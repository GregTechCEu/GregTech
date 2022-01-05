package gregtech.api.unification.stack;

import gregtech.api.GTValues;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * HashMap-based implementation of map with key type {@link ItemAndMetadata}
 * This map automatically takes care of wildcard item stacks as keys
 *
 * @param <V> value type
 */
public class WildcardAwareHashMap<V> extends Object2ObjectOpenHashMap<ItemAndMetadata, V> {

    public WildcardAwareHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public WildcardAwareHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public WildcardAwareHashMap() {
    }

    public WildcardAwareHashMap(Map<? extends ItemAndMetadata, ? extends V> m) {
        super(m);
    }

    @Override
    public V get(Object key) {
        ItemAndMetadata itemStack = (ItemAndMetadata) key;
        V resultValue = super.get(key);
        if (resultValue == null && itemStack.itemDamage != GTValues.W) {
            ItemAndMetadata wildcardStack = new ItemAndMetadata(itemStack.item, GTValues.W);
            resultValue = super.get(wildcardStack);
        }
        return resultValue;
    }

    @Override
    public boolean containsKey(Object key) {
        ItemAndMetadata itemStack = (ItemAndMetadata) key;
        boolean resultValue = super.containsKey(key);
        if (!resultValue && itemStack.itemDamage != GTValues.W) {
            ItemAndMetadata wildcardStack = new ItemAndMetadata(itemStack.item, GTValues.W);
            resultValue = super.containsKey(wildcardStack);
        }
        return resultValue;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V value = get(key);
        return value == null ? defaultValue : value;
    }
}
