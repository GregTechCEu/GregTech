package gregtech.api.recipes.builders;

import gregtech.api.recipes.RecipeBuilder;

/**
 * @deprecated use {@link RecipeBuilder} instead
 */
@Deprecated
public class IntCircuitRecipeBuilder extends RecipeBuilder<IntCircuitRecipeBuilder> {

    protected int circuit = -1;

    public IntCircuitRecipeBuilder() {
    }

    public IntCircuitRecipeBuilder(RecipeBuilder<IntCircuitRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public IntCircuitRecipeBuilder copy() {
        return new IntCircuitRecipeBuilder(this);
    }

    /**
     * @deprecated use {@link RecipeBuilder#circuitMeta(int)} instead
     */
    @Deprecated
    @Override
    public IntCircuitRecipeBuilder circuitMeta(int circuit) {
        return super.circuitMeta(circuit);
    }
}
