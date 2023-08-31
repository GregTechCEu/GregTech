package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.ComputationProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import org.jetbrains.annotations.NotNull;

public class ComputationRecipeBuilder extends RecipeBuilder<ComputationRecipeBuilder> {

    public ComputationRecipeBuilder() {/**/}

    public ComputationRecipeBuilder(Recipe recipe, RecipeMap<ComputationRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public ComputationRecipeBuilder(RecipeBuilder<ComputationRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public ComputationRecipeBuilder copy() {
        return new ComputationRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(ComputationProperty.KEY)) {
            this.CWUt(((Number) value).intValue());
            return true;
        }
        return super.applyProperty(key, value);
    }

    public ComputationRecipeBuilder CWUt(int cwut) {
        if (cwut < 0) {
            GTLog.logger.error("CWU/t cannot be less than 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(ComputationProperty.getInstance(), cwut);
        return this;
    }
}
