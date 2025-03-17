package gregtech.api.recipes.lookup.flag;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.lookup.CompactibleIterator;
import gregtech.api.recipes.lookup.IndexedRecipeLookup;

import it.unimi.dsi.fastutil.shorts.Short2LongArrayMap;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class FlagMap extends Short2LongArrayMap {

    private final IndexedRecipeLookup lookup;
    private final BitSet filter;

    public FlagMap(@NotNull IndexedRecipeLookup lookup, BitSet filter) {
        super(lookup.getBuiltRecipes().size() / 4);
        this.lookup = lookup;
        this.filter = filter;
    }

    public BitSet getFilter() {
        return filter;
    }

    public <T> void applyToEntry(short k, T context, FlagApplicator<T> operator) {
        this.put(k, operator.apply(context, this.get(k)));
    }

    public CompactibleIterator<Recipe> matchedIterator() {
        return new MatchIterator();
    }

    private class MatchIterator implements CompactibleIterator<Recipe> {

        private Recipe next = null;
        private int pointer = -1;

        @Override
        public boolean hasNext() {
            while (next == null && movePointer()) {
                Recipe recipe = lookup.getRecipeByIndex(pointer);
                if (recipe.ingredientFlags == get((short) pointer)) next = recipe;
            }
            return next != null;
        }

        private boolean movePointer() {
            if (pointer >= lookup.getBuiltRecipes().size()) return false;
            pointer = filter.nextClearBit(pointer + 1);
            return pointer < lookup.getBuiltRecipes().size();
        }

        @Override
        public Recipe next() {
            if (!hasNext()) throw new NoSuchElementException();
            Recipe n = next;
            next = null;
            return n;
        }

        @Override
        public @NotNull Iterator<Recipe> compact() {
            BitSet fullmatch = (BitSet) filter.clone();
            int pointer = this.pointer + 1;
            while (pointer < lookup.getBuiltRecipes().size()) {
                // pre-emptively calculate all the matches to pass on to the compacted iterator
                if (lookup.getRecipeByIndex(pointer).ingredientFlags != get((short) pointer)) fullmatch.set(pointer);
                pointer = filter.nextClearBit(pointer + 1);
            }
            return new CompactedIterator(lookup, fullmatch, this.pointer);
        }
    }

    private static class CompactedIterator implements Iterator<Recipe> {

        private final IndexedRecipeLookup tree;
        private final BitSet filter;

        private Recipe next = null;
        private int pointer;

        public CompactedIterator(IndexedRecipeLookup tree, BitSet filter, int pointer) {
            this.tree = tree;
            this.filter = filter;
            this.pointer = pointer;
        }

        @Override
        public boolean hasNext() {
            while (next == null && movePointer()) {
                next = tree.getRecipeByIndex(pointer);
            }
            return next != null;
        }

        private boolean movePointer() {
            if (pointer >= tree.getBuiltRecipes().size()) return false;
            pointer = filter.nextClearBit(pointer + 1);
            return pointer < tree.getBuiltRecipes().size();
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
