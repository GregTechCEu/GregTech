package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

public class AssemblerRecipeBuilder extends RecipeBuilder<AssemblerRecipeBuilder> {

    public AssemblerRecipeBuilder() {/**/}

    @SuppressWarnings("unused")
    public AssemblerRecipeBuilder(Recipe recipe, RecipeMap<AssemblerRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public AssemblerRecipeBuilder(AssemblerRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public AssemblerRecipeBuilder copy() {
        return new AssemblerRecipeBuilder(this);
    }
}
