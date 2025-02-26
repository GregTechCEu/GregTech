package gregtech.api.capability.data.query;

import gregtech.api.recipes.Recipe;

import org.jetbrains.annotations.NotNull;

public class RecipeDataQuery extends DataQueryObject {

    private final Recipe recipe;

    private boolean found = false;

    public RecipeDataQuery(Recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public @NotNull DataQueryFormat getFormat() {
        return DataQueryFormat.RECIPE;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setFound() {
        this.found = true;
        this.setShouldTriggerWalker(true);
    }

    public boolean isFound() {
        return found;
    }
}
