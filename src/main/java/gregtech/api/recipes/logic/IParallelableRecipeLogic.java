package gregtech.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IParallelableRecipeLogic {

    /**
     * Method which applies bonuses or penalties to the recipe based on the parallelization factor,
     * such as EU consumption or processing speed.
     *
     * @param builder the recipe builder
     */
    default void applyParallelBonus(RecipeBuilder<?> builder) {
    }

    /**
     * Method which finds a recipe which can be parallelized, works by multiplying the recipe by the parallelization factor,
     * and shrinking the recipe till its outputs can fit
     * @param recipeMap the recipe map
     * @param currentRecipe recipe to be parallelized
     * @param inputs input item handler
     * @param fluidInputs input fluid handler
     * @param outputs output item handler
     * @param fluidOutputs output fluid handler
     * @param parallelLimit the maximum number of parallel recipes to be performed
     * @return the recipe builder with the parallelized recipe. returns null the recipe cant fit
     */
    default RecipeBuilder<?> findMultipliedParallelRecipe(RecipeMap<?> recipeMap, Recipe currentRecipe, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, IItemHandlerModifiable outputs, IMultipleTankHandler fluidOutputs, int parallelLimit) {
        return ParallelLogic.doParallelRecipes(
                currentRecipe,
                recipeMap,
                inputs,
                fluidInputs,
                outputs,
                fluidOutputs,
                parallelLimit);
    }

    /**
     * Method which finds a recipe then multiplies it, then appends it to the builds up to the parallelization factor,
     * or filling the output
     * @param recipeMap the recipe map
     * @param inputs input item handler
     * @param fluidInputs input fluid handler
     * @param outputs output item handler
     * @param fluidOutputs output fluid handler
     * @param parallelLimit the maximum number of parallel recipes to be performed
     * @return the recipe builder with the parallelized recipe. returns null the recipe cant fit
     */
    default RecipeBuilder<?> findAppendedParallelRecipe(RecipeMap<?> recipeMap, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, IItemHandlerModifiable outputs, IMultipleTankHandler fluidOutputs, int parallelLimit, long maxVoltage) {
        return ParallelLogic.appendRecipes(
                recipeMap,
                inputs,
                fluidInputs,
                outputs,
                fluidOutputs,
                parallelLimit,
                maxVoltage);
    }

    default Recipe findParallelRecipe(AbstractRecipeLogic logic, Recipe currentRecipe, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, IItemHandlerModifiable outputs, IMultipleTankHandler fluidOutputs, long maxVoltage, int parallelLimit) {
        if (parallelLimit > 1) {
            RecipeBuilder<?> parallelBuilder = null;
            if (logic.getParallelLogicType() == ParallelLogicType.MULTIPLY) {
                parallelBuilder = findMultipliedParallelRecipe(logic.recipeMap, currentRecipe, inputs, fluidInputs, outputs, fluidOutputs, parallelLimit);
            } else if (logic.getParallelLogicType() == ParallelLogicType.APPEND) {
                parallelBuilder = findAppendedParallelRecipe(logic.recipeMap, inputs, fluidInputs, outputs, fluidOutputs, parallelLimit, maxVoltage);
            }

            if (parallelBuilder == null) {
                logic.invalidateInputs();
            } else {
                if (parallelBuilder.getParallel() == 0) {
                    logic.invalidateOutputs();
                    return null;
                } else {
                    logic.setParallelRecipesPerformed(parallelBuilder.getParallel());
                    //apply coil bonus
                    applyParallelBonus(parallelBuilder);
                    return parallelBuilder.build().getResult();
                }
            }
        }
        return currentRecipe;
    }
}
