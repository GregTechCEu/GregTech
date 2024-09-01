package gregtech.api.recipes.tree;

import gregtech.api.recipes.Recipe;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.recipes.tree.property.CircuitPropertyFilter;
import gregtech.api.recipes.tree.property.IPropertyFilter;

import gregtech.api.recipes.tree.property.VoltagePropertyFilter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

public class RecipeWrapper {
    private final @NotNull Recipe recipe;

    private final ObjectOpenHashSet<IPropertyFilter> propertyFilters = new ObjectOpenHashSet<>();

    private long flagsThreshold;

    RecipeWrapper(@NotNull Recipe recipe) {
        this.recipe = recipe;
        this.propertyFilters.add(new VoltagePropertyFilter(recipe.getEUt()));
        for (GTRecipeInput input : recipe.getInputs()) {
            if (input instanceof IntCircuitIngredient circuit) {
                propertyFilters.add(CircuitPropertyFilter.get(circuit.getMatchingConfigurations()));
            }
        }
    }

    public @NotNull Recipe getRecipe() {
        return recipe;
    }

    public ObjectOpenHashSet<IPropertyFilter> getPropertyFilters() {
        return propertyFilters;
    }

    public void setFlagsThreshold(long flagsThreshold) {
        this.flagsThreshold = flagsThreshold;
    }

    public boolean matchFlags(long flags) {
        return flags >= flagsThreshold;
    }
}
