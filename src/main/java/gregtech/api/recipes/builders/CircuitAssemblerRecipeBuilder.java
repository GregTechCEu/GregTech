package gregtech.api.recipes.builders;

import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;

import javax.annotation.Nonnull;

public class CircuitAssemblerRecipeBuilder extends RecipeBuilder<CircuitAssemblerRecipeBuilder> {

    private int solderMultiplier = 1;

    public CircuitAssemblerRecipeBuilder() {
    }

    public CircuitAssemblerRecipeBuilder(Recipe recipe, RecipeMap<CircuitAssemblerRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public CircuitAssemblerRecipeBuilder(RecipeBuilder<CircuitAssemblerRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    @Nonnull
    public CircuitAssemblerRecipeBuilder copy() {
        return new CircuitAssemblerRecipeBuilder(this);
    }

    public CircuitAssemblerRecipeBuilder solderMultiplier(int multiplier) {
        if (!GTUtility.isBetweenInclusive(1, 64000, (long) GTValues.L * multiplier)) {
            GTLog.logger.error("Fluid multiplier cannot exceed 64000mb total. Multiplier: {}", multiplier);
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.solderMultiplier = multiplier;
        return this;
    }

    public int getSolderMultiplier() {
        return this.solderMultiplier;
    }

}
