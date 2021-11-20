package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.logic.ParallelLogic;
import gregtech.api.util.OverlayedItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Collections;

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
            currentRecipe = findAndAppendRecipes(maxVoltage, importInventory);
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

    protected Recipe findAndAppendRecipes(long maxVoltage,
                                          IItemHandlerModifiable inputs) {
        RecipeBuilder<?> recipeBuilder = recipeMap.recipeBuilder();

        boolean matchedRecipe = false;

        OverlayedItemHandler overlayedItemHandler = new OverlayedItemHandler(this.getOutputInventory());

        // Iterate over the input items looking for more things to add until we run either out of input items
        // or we have exceeded the number of items permissible from the smelting bonus
        int engagedItems = 0;

        int recipeEUt = 0;
        int recipeDuration = 1;
        float speedBonusPercent = 0.0F; // Currently unused

        /* Iterate over input items looking for more items to process until we
         * have touched every item, or are at maximum item capacity
         */
        for (int index = 0; index < inputs.getSlots(); index++) {
            // Skip this slot if it is empty.
            final ItemStack currentInputItem = inputs.getStackInSlot(index);
            if (currentInputItem.isEmpty())
                continue;

            // Determine if there is a valid recipe for this item. If not, skip it.
            Recipe matchingRecipe = findRecipe(maxVoltage, currentInputItem);

            CountableIngredient inputIngredient;
            if (matchingRecipe != null) {
                recipeEUt = matchingRecipe.getEUt();
                recipeDuration = matchingRecipe.getDuration();
                inputIngredient = matchingRecipe.getInputs().get(0);
                matchedRecipe = true;
            } else
                continue;

            // There's something not right with this recipe if the ingredient is null.
            if (inputIngredient == null)
                throw new IllegalStateException(
                        String.format("Got recipe with null ingredient %s", matchingRecipe));

            //equivalent of getting the max ratio from the inputs from Parallel logic
            int amountOfCurrentItem = Math.min(MAX_PROCESSES - engagedItems, currentInputItem.getCount());

            //how much we can add to the output inventory
            int limitByOutput = ParallelLogic.limitParallelByItemsIncremental(recipeBuilder.getOutputs(), matchingRecipe.getOutputs(), overlayedItemHandler, amountOfCurrentItem);

            //amount to actually multiply the recipe by
            int multiplierRecipeAmount = Math.min(amountOfCurrentItem, limitByOutput);

            if (multiplierRecipeAmount > 0) {
                recipeBuilder.append(matchingRecipe, multiplierRecipeAmount);
                engagedItems += multiplierRecipeAmount;
            }

            if (engagedItems == MAX_PROCESSES) {
                break;
            }
        }

        this.invalidInputsForRecipes = !matchedRecipe;
        this.isOutputsFull = (matchedRecipe && engagedItems == 0);

        if (recipeBuilder.getInputs().isEmpty()) {
            return null;
        }

        this.parallelRecipesPerformed = engagedItems;

        return recipeBuilder
                .EUt(Math.min(32, (int) Math.ceil(recipeEUt * 1.33)))
                .duration(Math.max(recipeDuration, (int) (recipeDuration * (100.0F / (100.0F + speedBonusPercent)) * 1.5)))
                .build().getResult();
    }

    protected Recipe findRecipe(long maxVoltage, ItemStack itemStack) {
        return recipeMap.findRecipe(maxVoltage,
                Collections.singletonList(itemStack),
                Collections.emptyList(), 0, MatchingMode.IGNORE_FLUIDS);
    }
}
