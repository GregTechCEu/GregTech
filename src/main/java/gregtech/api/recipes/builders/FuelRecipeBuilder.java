package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.FuelProperty;

import gregtech.api.util.ValidationResult;

import org.jetbrains.annotations.NotNull;

public class FuelRecipeBuilder extends RecipeBuilder<FuelRecipeBuilder> {

    public FuelRecipeBuilder() {}

    public FuelRecipeBuilder(Recipe recipe, RecipeMap<FuelRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public FuelRecipeBuilder(RecipeBuilder<FuelRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public FuelRecipeBuilder copy() {
        return new FuelRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(FuelProperty.KEY)) {
            this.applyProperty(FuelProperty.getInstance(), value);
            return true;
        }
        return super.applyProperty(key, value);
    }

    @Override
    public ValidationResult<Recipe> build() {
        applyProperty(FuelProperty.getInstance(), recipeMap.getUnlocalizedName());
        return super.build();
    }
}
