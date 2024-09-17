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

public abstract class AbstractRecipeLookup {

    private static final Set<AbstractRecipeLookup> LOOKUPS = Collections.newSetFromMap(new WeakHashMap<>());

    public AbstractRecipeLookup() {
        LOOKUPS.add(this);
    }

    /**
     * @param recipe recipe to add
     * @return if the recipe was added
     */
    public abstract boolean addRecipe(@NotNull Recipe recipe);

    /**
     * @param recipe recipe to remove
     * @return if the recipe was removed
     */
    public abstract boolean removeRecipe(@NotNull Recipe recipe);

    public abstract void clear();

    public int getRecipeCount() {
        return getAllRecipes().size();
    }

    public abstract @NotNull @UnmodifiableView Collection<Recipe> getAllRecipes();

    @NotNull
    public abstract CompactibleIterator<Recipe> findRecipes(@NotNull List<ItemStack> items,
                                                            @NotNull List<FluidStack> fluids,
                                                            @Nullable PropertySet properties);

    @ApiStatus.Internal
    public static void rebuildRecipeLookups() {
        OreItemIngredient.invalidateCaches();
        for (AbstractRecipeLookup lookup : LOOKUPS) {
            lookup.rebuild();
        }
    }

    protected abstract void rebuild();
}
