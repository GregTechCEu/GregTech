package gregtech.api.recipes.tree.flag;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.tree.CompactibleIterator;
import gregtech.api.recipes.tree.RecipeTree;

import it.unimi.dsi.fastutil.shorts.Short2LongArrayMap;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class FlagMap extends Short2LongArrayMap {

    private final RecipeTree tree;
    private final BitSet filter;

    public FlagMap(@NotNull RecipeTree tree, BitSet filter) {
        super(tree.getRecipeCount() / 4);
        this.tree = tree;
        this.filter = filter;
    }

    public BitSet getFilter() {
        return filter;
    }

    public <T> void applyToEntry(short k, T context, FlagApplicator<T> operator) {
        this.put(k, operator.apply(context, this.get(k)));
//        int index = Short.toUnsignedInt(k);
//        flagArray[index] = operator.apply(context, flagArray[index]);
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

        @Override
        public Iterator<Recipe> compact() {
            BitSet fullmatch = (BitSet) filter.clone();
            int pointer = this.pointer + 1;
            while (pointer < tree.getRecipeCount()) {
                // pre-emptively calculate all the matches to pass on to the compacted iterator
                 if (!tree.getRecipeByIndex(pointer).matchFlags(get((short) pointer))) fullmatch.set(pointer);
                pointer = filter.nextClearBit(pointer + 1);
            }
            return new CompactedIterator(tree, fullmatch, this.pointer);
        }
    }

    private static class CompactedIterator implements Iterator<Recipe> {

        private final RecipeTree tree;
        private final BitSet filter;

        private Recipe next = null;
        private int pointer;

        public CompactedIterator(RecipeTree tree, BitSet filter, int pointer) {
            this.tree = tree;
            this.filter = filter;
            this.pointer = pointer;
        }

        @Override
        public boolean hasNext() {
            while (next == null && movePointer()) {
                next = tree.getRecipeByIndex(pointer).getRecipe();
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
