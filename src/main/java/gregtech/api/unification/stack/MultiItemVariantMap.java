package gregtech.api.unification.stack;

import gregtech.api.GTValues;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * {@link ItemVariantMap} implementation backed by a hashmap. Each metadata is
 * treated as separate entry in hashmap, and get/set accesses treat each metadata
 * as a unique item variant.
 *
 * @param <E> type of the elements
 */
public final class MultiItemVariantMap<E> implements ItemVariantMap.Mutable<E> {

    @Nullable
    private Short2ObjectOpenHashMap<E> itemDamageEntries;
    @Nullable
    private E wildcardEntry;

    @Override
    public boolean hasNonWildcardEntry() {
        return this.itemDamageEntries != null && !this.itemDamageEntries.isEmpty();
    }

    @Override
    public boolean has(short meta) {
        if (meta == GTValues.W) {
            return this.wildcardEntry != null;
        } else {
            return this.itemDamageEntries != null && this.itemDamageEntries.containsKey(meta);
        }
    }

    @Nullable
    @Override
    public E get(short meta) {
        if (meta == GTValues.W) {
            return this.wildcardEntry;
        } else if (this.itemDamageEntries != null) {
            return this.itemDamageEntries.get(meta);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public E put(short meta, @Nullable E e) {
        if (meta == GTValues.W) {
            E cache = this.wildcardEntry;
            this.wildcardEntry = e;
            return cache;
        } else if (e != null) {
            if (this.itemDamageEntries == null) {
                this.itemDamageEntries = new Short2ObjectOpenHashMap<>();
            }
            return this.itemDamageEntries.put(meta, e);
        } else {
            if (this.itemDamageEntries != null) {
                return this.itemDamageEntries.remove(meta);
            } else {
                return null;
            }
        }
    }

    @Override
    public void clear() {
        this.itemDamageEntries = null;
        this.wildcardEntry = null;
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder().append("MultiItemVariantMap[");
        boolean first = true;
        if (itemDamageEntries != null) {
            for (Short2ObjectMap.Entry<E> e : itemDamageEntries.short2ObjectEntrySet()) {
                if (first) first = false;
                else stb.append(',');
                stb.append(e.getShortKey()).append("=").append(e.getValue());
            }
        }
        if (wildcardEntry != null) {
            if (!first) stb.append(',');
            stb.append("*=").append(wildcardEntry);
        }
        return stb.append(']').toString();
    }
}
