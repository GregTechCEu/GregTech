package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.util.ValidationResult;

public class NoEnergyRecipeBuilder extends RecipeBuilder<NoEnergyRecipeBuilder> {

    public NoEnergyRecipeBuilder() {}

    @SuppressWarnings("unused")
    public NoEnergyRecipeBuilder(Recipe recipe, RecipeMap<NoEnergyRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public NoEnergyRecipeBuilder(RecipeBuilder<NoEnergyRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    public NoEnergyRecipeBuilder copy() {
        return new NoEnergyRecipeBuilder(this);
    }

    public ValidationResult<Recipe> build() {
        this.EUt(-1);
        this.applyProperty(PrimitiveProperty.getInstance(), true);
        return super.build();
    }
}
