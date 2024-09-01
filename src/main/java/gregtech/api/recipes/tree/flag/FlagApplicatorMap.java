package gregtech.api.recipes.tree.flag;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

public final class FlagApplicatorMap<T> extends Short2ObjectOpenHashMap<FlagApplicator<T>> implements IApplicatorMap<T> {

    public void insertApplicator(int k, @NotNull FlagApplicator<T> applicator) {
        insertApplicator((short) k, applicator);
    }

    public void insertApplicator(short k, final @NotNull FlagApplicator<T> applicator) {
        int pos;
        Object[] values = value;
        if (((k) == ((short) 0))) {
            if (containsNullKey) values[n] = applicator;
            containsNullKey = true;
            pos = n;
        } else {
            short curr;
            final short[] key = this.key;
            // The starting point.
            if (!((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix((k))) & mask]) == ((short) 0))) {
                if (((curr) == (k))) values[pos] = ((FlagApplicator<T>) values[pos]).union(applicator);
                while (!((curr = key[pos = (pos + 1) & mask]) == ((short) 0)))
                    if (((curr) == (k))) values[pos] = ((FlagApplicator<T>) values[pos]).union(applicator);
            }
        }
        key[pos] = k;
        values[pos] = applicator;
        if (size++ >= maxFill) rehash(arraySize(size + 1, f));
    }

    @Override
    public void applyFlags(FlagMap flags, T context) {
        for (var entry : this.short2ObjectEntrySet()) {
            if (flags.getFilter().get(Short.toUnsignedInt(entry.getShortKey()))) continue;
            flags.applyToEntry(entry.getShortKey(), context, entry.getValue());
        }
    }
}
