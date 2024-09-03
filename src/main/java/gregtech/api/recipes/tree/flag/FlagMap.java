package gregtech.api.recipes.tree.flag;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.tree.RecipeTree;

import gregtech.api.recipes.tree.RecipeWrapper;

import it.unimi.dsi.fastutil.shorts.Short2LongArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;

public final class FlagMap {

    private final RecipeTree tree;
    private final BitSet filter;

    private final long[] flagArray;

    public FlagMap(@NotNull RecipeTree tree, BitSet filter) {
        this.tree = tree;
        this.filter = filter;
        this.flagArray = new long[tree.getRecipeCount()];
    }

    public BitSet getFilter() {
        return filter;
    }

    public <T> void applyToEntry(short k, T context, FlagApplicator<T> operator) {
        int index = Short.toUnsignedInt(k);
        flagArray[index] = operator.apply(context, flagArray[index]);
    }

    public Iterator<Recipe> matchedIterator() {
        return new MatchIterator();
    }

    private class MatchIterator implements Iterator<Recipe> {
        private Recipe next = null;
        private int pointer = -1;


        @Override
        public boolean hasNext() {
            while (next == null && movePointer()) {
                RecipeWrapper wrapper = tree.getRecipeByIndex(pointer);
                if (wrapper.matchFlags(flagArray[pointer])) next = wrapper.getRecipe();
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
