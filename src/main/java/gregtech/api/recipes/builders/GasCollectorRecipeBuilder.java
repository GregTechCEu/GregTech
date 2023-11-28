package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.GasCollectorDimensionProperty;

import crafttweaker.CraftTweakerAPI;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GasCollectorRecipeBuilder extends RecipeBuilder<GasCollectorRecipeBuilder> {

    public GasCollectorRecipeBuilder() {}

    public GasCollectorRecipeBuilder(Recipe recipe, RecipeMap<GasCollectorRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public GasCollectorRecipeBuilder(RecipeBuilder<GasCollectorRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public GasCollectorRecipeBuilder copy() {
        return new GasCollectorRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(GasCollectorDimensionProperty.KEY)) {
            if (value instanceof Integer) {
                this.dimension((Integer) value);
            } else if (value instanceof List && !((List<?>) value).isEmpty() &&
                    ((List<?>) value).get(0) instanceof Integer) {
                        IntList dimensionIDs = getDimensionIDs();
                        if (dimensionIDs == IntLists.EMPTY_LIST) {
                            dimensionIDs = new IntArrayList();
                            this.applyProperty(GasCollectorDimensionProperty.getInstance(), dimensionIDs);
                        }
                        dimensionIDs.addAll((List<Integer>) value);
                    } else {
                        if (isCTRecipe) {
                            CraftTweakerAPI.logError("Dimension for Gas Collector needs to be a Integer");
                            return false;
                        }
                        throw new IllegalArgumentException("Invalid Dimension Property Type!");
                    }
            return true;
        }
        return super.applyProperty(key, value);
    }

    public GasCollectorRecipeBuilder dimension(int dimensionID) {
        IntList dimensionIDs = getDimensionIDs();
        if (dimensionIDs == IntLists.EMPTY_LIST) {
            dimensionIDs = new IntArrayList();
            this.applyProperty(GasCollectorDimensionProperty.getInstance(), dimensionIDs);
        }
        dimensionIDs.add(dimensionID);
        return this;
    }

    public IntList getDimensionIDs() {
        return this.recipePropertyStorage == null ? IntLists.EMPTY_LIST :
                this.recipePropertyStorage.getRecipePropertyValue(GasCollectorDimensionProperty.getInstance(),
                        IntLists.EMPTY_LIST);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(GasCollectorDimensionProperty.getInstance().getKey(), getDimensionIDs().toString())
                .toString();
    }
}
