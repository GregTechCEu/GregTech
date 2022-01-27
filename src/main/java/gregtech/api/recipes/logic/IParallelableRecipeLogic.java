package gregtech.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public interface IParallelableRecipeLogic {

    /**
     * Method which applies bonuses or penalties to the recipe based on the parallelization factor,
     * such as EU consumption or processing speed.
     *
     * @param builder the recipe builder
     */
    default void applyParallelBonus(@Nonnull RecipeBuilder<?> builder) {
    }

    /**
     * Method which finds a recipe which can be parallelized, works by multiplying the recipe by the parallelization factor,
     * and shrinking the recipe till its outputs can fit
     *
     * @param recipeMap     the recipe map
     * @param currentRecipe recipe to be parallelized
     * @param inputs        input item handler
     * @param fluidInputs   input fluid handler
     * @param outputs       output item handler
     * @param fluidOutputs  output fluid handler
     * @param parallelLimit the maximum number of parallel recipes to be performed
     * @param maxVoltage    the voltage limit on the number of parallel recipes to be performed
     * @param mte           the MetaTileEntity performing the parallel recipe
     * @return the recipe builder with the parallelized recipe. returns null the recipe can't fit
     */
    default RecipeBuilder<?> findMultipliedParallelRecipe(RecipeMap<?> recipeMap, Recipe currentRecipe, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, IItemHandlerModifiable outputs, IMultipleTankHandler fluidOutputs, int parallelLimit, long maxVoltage, MetaTileEntity mte) {
        return ParallelLogic.doParallelRecipes(
                currentRecipe,
                recipeMap,
                inputs,
                fluidInputs,
                outputs,
                fluidOutputs,
                parallelLimit,
                maxVoltage,
                mte);
    }

    /**
     * Method which finds a recipe then multiplies it, then appends it to the builds up to the parallelization factor,
     * or filling the output
     *
     * @param recipeMap     the recipe map
     * @param inputs        input item handler
     * @param outputs       output item handler
     * @param parallelLimit the maximum number of parallel recipes to be performed
     * @param maxVoltage    the voltage limit on the number of parallel recipes to be performed
     * @param mte           the MetaTileEntity performing the parallel recipe
     * @return the recipe builder with the parallelized recipe. returns null the recipe can't fit
     */
    default RecipeBuilder<?> findAppendedParallelItemRecipe(RecipeMap<?> recipeMap, IItemHandlerModifiable inputs, IItemHandlerModifiable outputs, int parallelLimit, long maxVoltage, MetaTileEntity mte) {
        return ParallelLogic.appendItemRecipes(
                recipeMap,
                inputs,
                outputs,
                parallelLimit,
                maxVoltage,
                mte);
    }

    // Recipes passed in here should be already trimmed, if desired
    default Recipe findParallelRecipe(AbstractRecipeLogic logic, Recipe currentRecipe, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, IItemHandlerModifiable outputs, IMultipleTankHandler fluidOutputs, long maxVoltage, int parallelLimit) {
        if (parallelLimit > 1) {
            RecipeBuilder<?> parallelBuilder = null;
            if (logic.getParallelLogicType() == ParallelLogicType.MULTIPLY) {
                parallelBuilder = findMultipliedParallelRecipe(logic.getRecipeMap(), currentRecipe, inputs, fluidInputs, outputs, fluidOutputs, parallelLimit, maxVoltage, logic.getMetaTileEntity());
            } else if (logic.getParallelLogicType() == ParallelLogicType.APPEND_ITEMS) {
                parallelBuilder = findAppendedParallelItemRecipe(logic.getRecipeMap(), inputs, outputs, parallelLimit, maxVoltage, logic.getMetaTileEntity());
            }
            // if the builder returned is null, no recipe was found.
            if (parallelBuilder == null) {
                logic.invalidateInputs();
                return null;
            } else {
                //if the builder returned does not parallel, its outputs are full
                if (parallelBuilder.getParallel() == 0) {
                    logic.invalidateOutputs();
                    return null;
                } else {
                    logic.setParallelRecipesPerformed(parallelBuilder.getParallel());
                    //apply any parallel bonus
                    applyParallelBonus(parallelBuilder);
                    return parallelBuilder.build().getResult();
                }
            }
        }
        return currentRecipe;
    }
}
