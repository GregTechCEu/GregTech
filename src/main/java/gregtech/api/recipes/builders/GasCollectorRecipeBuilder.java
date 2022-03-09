package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.GasCollectorDimensionProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class GasCollectorRecipeBuilder extends RecipeBuilder<GasCollectorRecipeBuilder> {

    private List<Integer> dimensionIDs;

    public GasCollectorRecipeBuilder() {
    }

    public GasCollectorRecipeBuilder(Recipe recipe, RecipeMap<GasCollectorRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
        this.dimensionIDs = recipe.getProperty(GasCollectorDimensionProperty.getInstance(), new ArrayList<Integer>());
    }

    public GasCollectorRecipeBuilder(RecipeBuilder<GasCollectorRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public GasCollectorRecipeBuilder copy() {
        return new GasCollectorRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(String key, Object value) {
        if (key.equals(GasCollectorDimensionProperty.KEY)) {
            this.dimension(((Number) value).intValue());
            return true;
        }
        return true;
    }

    public GasCollectorRecipeBuilder dimension(int dimensionID) {
        if (this.dimensionIDs == null)
            this.dimensionIDs = new ArrayList<>();
        this.dimensionIDs.add(dimensionID);
        return this;
    }

    public ValidationResult<Recipe> build() {
        Recipe recipe = new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
                duration, EUt, hidden, isCTRecipe);
        if (!recipe.setProperty(GasCollectorDimensionProperty.getInstance(), dimensionIDs)) {
            return ValidationResult.newResult(EnumValidationResult.INVALID, recipe);
        }

        return ValidationResult.newResult(finalizeAndValidate(), recipe);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(GasCollectorDimensionProperty.getInstance().getKey(), dimensionIDs.toString())
                .toString();
    }
}
