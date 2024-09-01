package gregtech.api.recipes.tree.flag;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.tree.RecipeTree;

import gregtech.api.recipes.tree.RecipeWrapper;

import it.unimi.dsi.fastutil.shorts.Short2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

public final class FlagMap extends Short2LongOpenHashMap {

    private final RecipeTree tree;
    private final BitSet filter;

    public FlagMap(@NotNull RecipeTree tree, BitSet filter) {
        super(tree.getRecipeCount() / 2);
        this.tree = tree;
        this.filter = filter;
    }

    public BitSet getFilter() {
        return filter;
    }

    public <T> void applyToEntry(short k, T context, FlagApplicator<T> operator) {
        int pos;
        if (((k) == ((short) 0))) {
            if (containsNullKey) value[n] = operator.apply(context, value[n]);
            containsNullKey = true;
            pos = n;
        } else {
            short curr;
            final short[] key = this.key;
            // The starting point.
            if (!((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix((k))) & mask]) == ((short) 0))) {
                if (((curr) == (k))) value[pos] = operator.apply(context, value[pos]);
                while (!((curr = key[pos = (pos + 1) & mask]) == ((short) 0)))
                    if (((curr) == (k))) value[pos] = operator.apply(context, value[pos]);
            }
        }
        key[pos] = k;
        value[pos] = operator.apply(context, 0);
        if (size++ >= maxFill) rehash(arraySize(size + 1, f));
    }

    public Iterator<Recipe> matchedIterator() {
        return new MatchIterator();
    }

    private class MatchIterator implements Iterator<Recipe> {
        private Recipe next = null;
        private int pointer = 0;


        @Override
        public boolean hasNext() {
            while (next == null && movePointer()) {
                RecipeWrapper wrapper = tree.getRecipeByIndex(pointer);
                if (wrapper.matchFlags(get((short) pointer))) next = wrapper.getRecipe();
            }
            return next != null;
        }

        private boolean movePointer() {
            if (pointer >= tree.getRecipeCount()) return false;
            pointer = filter.nextClearBit(pointer + 1);
            return pointer < tree.getRecipeCount();
        }

        @Override
        public Recipe next() {
            if (!hasNext()) throw new NoSuchElementException();
            Recipe n = next;
            next = null;
            return n;
        }
    }
}
