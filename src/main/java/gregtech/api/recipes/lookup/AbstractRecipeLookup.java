package gregtech.api.recipes.lookup;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.OreItemIngredient;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class AbstractRecipeLookup {

    private static final Set<AbstractRecipeLookup> LOOKUPS = Collections.newSetFromMap(new WeakHashMap<>());

    public AbstractRecipeLookup() {
        LOOKUPS.add(this);
    }

    public abstract void addRecipe(@NotNull Recipe recipe);

    public abstract int getRecipeCount();

    public abstract CompactibleIterator<Recipe> findRecipes(List<ItemStack> items, List<FluidStack> fluids, PropertySet properties);

    @ApiStatus.Internal
    public static void rebuildRecipeLookups() {
        OreItemIngredient.invalidateCaches();
        for (AbstractRecipeLookup lookup : LOOKUPS) {
            lookup.rebuild();
        }
    }

    public abstract void rebuild();
}
