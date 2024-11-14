package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.properties.impl.PrimitiveProperty;
import gregtech.api.util.ValidationResult;

@Deprecated
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
}
