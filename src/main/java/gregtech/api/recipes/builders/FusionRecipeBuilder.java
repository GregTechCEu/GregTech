package gregtech.api.recipes.builders;

import com.google.common.collect.ImmutableMap;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.FusionEUToStartProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.ValidationResult;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class FusionRecipeBuilder extends RecipeBuilder<FusionRecipeBuilder> {

    private long EUToStart;

    public FusionRecipeBuilder() {
    }

    public FusionRecipeBuilder(Recipe recipe, RecipeMap<FusionRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
        this.EUToStart = recipe.getRecipePropertyStorage().getRecipePropertyValue(FusionEUToStartProperty.getInstance(), 0L);
    }

    public FusionRecipeBuilder(RecipeBuilder<FusionRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public FusionRecipeBuilder copy() {
        return new FusionRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(String key, Object value) {
        if (key.equals("eu_to_start")) {
            this.EUToStart(((Number) value).longValue());
            return true;
        }
        return false;
    }

    public FusionRecipeBuilder EUToStart(long EUToStart) {
        if (EUToStart <= 0) {
            GTLog.logger.error("EU to start cannot be less than or equal to 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.EUToStart = EUToStart;
        return this;
    }

    public ValidationResult<Recipe> build() {
        Recipe recipe = new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
                duration, EUt, hidden);
        if (!recipe.getRecipePropertyStorage().store(ImmutableMap.of(FusionEUToStartProperty.getInstance(), EUToStart))) {
            return ValidationResult.newResult(EnumValidationResult.INVALID, recipe);
        }

        return ValidationResult.newResult(finalizeAndValidate(), recipe);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append(FusionEUToStartProperty.getInstance().getKey(), EUToStart)
            .toString();
    }
}
