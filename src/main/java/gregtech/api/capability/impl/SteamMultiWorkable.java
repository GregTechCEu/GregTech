package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.IParallelAble;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.logic.ParallelLogic;
import net.minecraftforge.items.IItemHandlerModifiable;


/**
 * General Recipe Handler for Steam Multiblocks.
 * Will do up to the passed value of items in one process.
 * Not recommended to use this Handler if you do not
 * need multi-recipe logic for your Multi.
 */
public class SteamMultiWorkable extends SteamMultiblockRecipeLogic implements IParallelAble {

    private final int MAX_PROCESSES;

    public SteamMultiWorkable(RecipeMapSteamMultiblockController tileEntity, double conversionRate, int maxProcesses) {
        super(tileEntity, tileEntity.recipeMap, tileEntity.getSteamFluidTank(), conversionRate);
        MAX_PROCESSES = maxProcesses;
    }

    @Override
    protected void trySearchNewRecipe() {
        long maxVoltage = getMaxVoltage(); // Will always be LV voltage
        Recipe currentRecipe = null;
        IItemHandlerModifiable importInventory = getInputInventory();
        IMultipleTankHandler importFluids = getInputTank();

        //inverse of logic in normal AbstractRecipeLogic
        //for MultiSmelter, we can reuse previous recipe if inputs didn't change
        //otherwise, we need to recompute it for new ingredients
        //but technically, it means we can cache multi smelter recipe, but changing inputs have more priority
        if (hasNotifiedInputs() ||
                previousRecipe == null ||
                !previousRecipe.matches(false, importInventory, importFluids, MatchingMode.IGNORE_FLUIDS)) {
            //Inputs changed, try searching new recipe for given inputs
            currentRecipe = findRecipe(maxVoltage, importInventory, importFluids);
        } else {
            //if previous recipe still matches inputs, try to use it
            currentRecipe = previousRecipe;
        }
        if (currentRecipe != null)
            // replace old recipe with new one
            this.previousRecipe = currentRecipe;
        // proceed if we have a usable recipe.
        if (currentRecipe != null && setupAndConsumeRecipeInputs(currentRecipe, importInventory))
            setupRecipe(currentRecipe);
        // Inputs have been inspected.
        metaTileEntity.getNotifiedItemInputList().clear();
    }

    protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable importInventory, IMultipleTankHandler importFluids) {
        IItemHandlerModifiable exportInventory = getOutputInventory();
        IMultipleTankHandler exportFluids = getOutputTank();

        RecipeBuilder<?> builder = ParallelLogic.appendRecipes(recipeMap,
                importInventory,
                importFluids,
                exportInventory,
                exportFluids,
                MAX_PROCESSES,
                maxVoltage);
        this.applyBuilderFeatures(builder);
        if (builder != null) {
            return builder.build().getResult();
        } else {
            return null;
        }
    }

    @Override
    public void applyBuilderFeatures(RecipeBuilder<?> builder) {
        if (builder == null) {
            this.invalidInputsForRecipes = true;
        } else {
            if (builder.getParallel() == 0) {
                this.isOutputsFull = true;
            } else {
                this.parallelRecipesPerformed = builder.getParallel();
                //apply coil bonus
                applyParallelBonus(builder);
            }
        }
    }

    @Override
    public void applyParallelBonus(RecipeBuilder<?> builder) {
        builder.EUt(5).duration(192);
    }
}
