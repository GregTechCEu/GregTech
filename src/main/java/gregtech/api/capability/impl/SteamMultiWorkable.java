package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import net.minecraftforge.items.IItemHandlerModifiable;


/**
 * General Recipe Handler for Steam Multiblocks.
 * Will do up to the passed value of items in one process.
 * Not recommended to use this Handler if you do not
 * need multi-recipe logic for your Multi.
 */
public class SteamMultiWorkable extends SteamMultiblockRecipeLogic {

    private final int MAX_PROCESSES;

    public SteamMultiWorkable(RecipeMapSteamMultiblockController tileEntity, double conversionRate, int maxProcesses) {
        super(tileEntity, tileEntity.recipeMap, tileEntity.getSteamFluidTank(), conversionRate);
        MAX_PROCESSES = maxProcesses;
    }

    @Override
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.APPEND;
    }

    @Override
    protected void trySearchNewRecipe() {
        long maxVoltage = getMaxVoltage(); // Will always be LV voltage
        Recipe currentRecipe = null;
        IItemHandlerModifiable importInventory = getInputInventory();
        IMultipleTankHandler importFluids = getInputTank();
        IItemHandlerModifiable exportInventory = getOutputInventory();
        IMultipleTankHandler exportFluids = getOutputTank();

        //inverse of logic in normal AbstractRecipeLogic
        //for MultiSmelter, we can reuse previous recipe if inputs didn't change
        //otherwise, we need to recompute it for new ingredients
        //but technically, it means we can cache multi smelter recipe, but changing inputs have more priority
        if (hasNotifiedInputs() ||
                previousRecipe == null ||
                !previousRecipe.matches(false, importInventory, importFluids, MatchingMode.IGNORE_FLUIDS)) {
            //Inputs changed, try searching new recipe for given inputs
            currentRecipe = findParallelRecipe(
                    this,
                    null,
                    importInventory,
                    importFluids,
                    exportInventory,
                    exportFluids,
                    maxVoltage, MAX_PROCESSES);
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

    @Override
    public void applyParallelBonus(RecipeBuilder<?> builder) {
        builder.EUt(5).duration(192);
    }
}
