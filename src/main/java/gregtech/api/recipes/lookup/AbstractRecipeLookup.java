package gregtech.api.recipes.lookup;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.OreItemIngredient;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractRecipeLookup {

    private static final Set<AbstractRecipeLookup> LOOKUPS = Collections.newSetFromMap(new WeakHashMap<>());

    protected static final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public AbstractRecipeLookup() {
        LOOKUPS.add(this);
    }

    /**
     * @param recipe recipe to add
     * @return if the recipe will be added on next rebuild.
     */
    public abstract boolean addRecipe(@NotNull Recipe recipe);

    /**
     * @param recipe recipe to remove
     * @return if the recipe will be removed on next rebuild.
     */
    public abstract boolean removeRecipe(@NotNull Recipe recipe);

    /**
     * Empties the pending recipes. Recipes discoverable in this lookup will not change until a rebuild occurs.
     */
    public abstract void clear();

    /**
     * Gets all recipes that will be in the lookup on the next rebuild.
     * 
     * @return recipes that will be discoverable after the next rebuild.
     */
    public abstract @NotNull @UnmodifiableView Collection<Recipe> getPendingRecipes();

    /**
     * Gets all recipes currently built in the lookup.
     * 
     * @return recipes that are currently discoverable until the next rebuild.
     */
    public abstract @NotNull @UnmodifiableView Collection<Recipe> getBuiltRecipes();

    /**
     * Finds all recipes matching with the given lookup information.
     * 
     * @param items      items to search with
     * @param fluids     fluids to search with
     * @param properties properties to search with. Null to ignore properties.
     * @return an iterator of matching recipes.
     */
    @NotNull
    public abstract CompactibleIterator<Recipe> findRecipes(@NotNull List<ItemStack> items,
                                                            @NotNull List<FluidStack> fluids,
                                                            @Nullable PropertySet properties);

    /**
     * Triggers a rebuild of all recipe lookups. This is an expensive operation that may freeze many threads, use
     * sparingly!
     */
    @ApiStatus.Internal
    public static void rebuildRecipeLookups() {
        OreItemIngredient.invalidateCaches();
        try {
            lock.writeLock().lock();
            for (AbstractRecipeLookup lookup : LOOKUPS) {
                lookup.rebuild();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Triggers a rebuild of this recipe lookup. This is an expensive operation that may freeze many threads, use
     * sparingly!
     */
    @ApiStatus.Internal
    public final void triggerRebuild() {
        try {
            lock.writeLock().lock();
            rebuild();
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected abstract void rebuild();
}
