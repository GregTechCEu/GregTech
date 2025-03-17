package gregtech.api.recipes.lookup;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public final class CompositeRecipeLookup extends AbstractRecipeLookup {

    private final @Nullable AbstractRecipeLookup internalLookup;

    private final @NotNull AbstractRecipeLookup @NotNull [] lookups;

    private @Nullable ObjectArrayList<Recipe> built;

    /**
     * @param internalLookup the lookup that all modifications of this composite lookup will be directed to. Can be
     *                       null.
     * @param lookups        the lookups that will additionally be searched for recipes when this lookup is searched.
     */
    public CompositeRecipeLookup(@Nullable AbstractRecipeLookup internalLookup,
                                 @NotNull AbstractRecipeLookup @NotNull... lookups) {
        if (lookups.length == 0)
            throw new IllegalArgumentException("Should not use a composite recipe lookup without additional lookups!");
        this.internalLookup = internalLookup;
        this.lookups = lookups;
    }

    @ApiStatus.Internal
    public @Nullable AbstractRecipeLookup getInternalLookup() {
        return internalLookup;
    }

    @ApiStatus.Internal
    public AbstractRecipeLookup[] getLookups() {
        return lookups;
    }

    @Override
    public boolean addRecipe(@NotNull Recipe recipe) {
        if (internalLookup == null) return false;
        else return internalLookup.addRecipe(recipe);
    }

    @Override
    public boolean removeRecipe(@NotNull Recipe recipe) {
        if (internalLookup == null) return false;
        else return internalLookup.removeRecipe(recipe);
    }

    @Override
    public void clear() {
        if (internalLookup != null) internalLookup.clear();
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Recipe> getPendingRecipes() {
        Collection<Recipe> collection = new ObjectArrayList<>();
        if (internalLookup != null) collection.addAll(internalLookup.getPendingRecipes());
        for (AbstractRecipeLookup lookup : lookups) {
            collection.addAll(lookup.getPendingRecipes());
        }
        return collection;
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Recipe> getBuiltRecipes() {
        if (built == null) {
            built = new ObjectArrayList<>();
            if (internalLookup != null) built.addAll(internalLookup.getPendingRecipes());
            for (AbstractRecipeLookup lookup : lookups) {
                built.addAll(lookup.getPendingRecipes());
            }
            built.trim();
        }
        return built;
    }

    @Override
    public @NotNull CompactibleIterator<Recipe> findRecipes(@NotNull List<ItemStack> items,
                                                            @NotNull List<FluidStack> fluids,
                                                            @Nullable PropertySet properties) {
        List<CompactibleIterator<Recipe>> iterators = new ObjectArrayList<>();
        if (internalLookup != null) iterators.add(internalLookup.findRecipes(items, fluids, properties));
        for (AbstractRecipeLookup lookup : lookups) {
            iterators.add(lookup.findRecipes(items, fluids, properties));
        }
        return new CompoundCompactibleIterator(iterators);
    }

    /**
     * Internal lookups should have their rebuilds triggered separately.
     */
    @Override
    protected void rebuild() {
        built = null;
    }

    private final static class CompoundCompactibleIterator implements CompactibleIterator<Recipe> {

        List<CompactibleIterator<Recipe>> iterators;

        Recipe next;

        public CompoundCompactibleIterator(@NotNull List<CompactibleIterator<Recipe>> iterators) {
            iterators.removeIf(i -> !i.hasNext());
            this.iterators = iterators;
        }

        @Override
        public boolean hasNext() {
            if (next == null) computeNext();
            return next != null;
        }

        @Override
        public Recipe next() {
            if (!hasNext()) throw new NoSuchElementException();
            Recipe temp = next;
            next = null;
            return temp;
        }

        private void computeNext() {
            if (iterators.isEmpty()) return;
            while (!iterators.get(0).hasNext()) {
                iterators.remove(0);
                if (iterators.isEmpty()) return;
            }
            next = iterators.get(0).next();
        }

        @Override
        public @NotNull Iterator<Recipe> compact() {
            return new CompoundIterator(iterators.stream()
                    .map(CompactibleIterator::compact).collect(Collectors.toList()));
        }
    }

    private final static class CompoundIterator implements Iterator<Recipe> {

        List<Iterator<Recipe>> iterators;

        Recipe next;

        public CompoundIterator(@NotNull List<Iterator<Recipe>> iterators) {
            iterators.removeIf(i -> !i.hasNext());
            this.iterators = iterators;
        }

        @Override
        public boolean hasNext() {
            if (next == null) computeNext();
            return next != null;
        }

        @Override
        public Recipe next() {
            if (!hasNext()) throw new NoSuchElementException();
            Recipe temp = next;
            next = null;
            return temp;
        }

        private void computeNext() {
            if (iterators.isEmpty()) return;
            while (!iterators.get(0).hasNext()) {
                iterators.remove(0);
                if (iterators.isEmpty()) return;
            }
            next = iterators.get(0).next();
        }
    }
}
