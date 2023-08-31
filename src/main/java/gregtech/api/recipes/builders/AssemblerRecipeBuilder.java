package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;

public class AssemblerRecipeBuilder extends RecipeBuilder<AssemblerRecipeBuilder> {

    private boolean withRecycling;

    public AssemblerRecipeBuilder() {/**/}

    @SuppressWarnings("unused")
    public AssemblerRecipeBuilder(Recipe recipe, RecipeMap<AssemblerRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public AssemblerRecipeBuilder(AssemblerRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
        if (recipeBuilder.isWithRecycling()) {
            this.withRecycling = true;
        }
    }

    @Override
    public AssemblerRecipeBuilder copy() {
        var builder = new AssemblerRecipeBuilder(this);
        if (withRecycling) {
            return builder.withRecycling();
        }
        return builder;
    }

    public AssemblerRecipeBuilder withRecycling() {
        withRecycling = true;
        return this;
    }

    public boolean isWithRecycling() {
        return withRecycling;
    }
}
