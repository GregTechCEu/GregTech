package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.util.ValidationResult;

public class PrimitiveRecipeBuilder extends RecipeBuilder<PrimitiveRecipeBuilder> {

    public PrimitiveRecipeBuilder() {}

    public PrimitiveRecipeBuilder(Recipe recipe, RecipeMap<PrimitiveRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public PrimitiveRecipeBuilder(RecipeBuilder<PrimitiveRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public PrimitiveRecipeBuilder copy() {
        return new PrimitiveRecipeBuilder(this);
    }

    @Override
    public ValidationResult<Recipe> build() {
        this.EUt(1); // secretly force to 1 to allow recipe matching to work properly
        applyProperty(PrimitiveProperty.getInstance(), true);
        return super.build();
    }
}
