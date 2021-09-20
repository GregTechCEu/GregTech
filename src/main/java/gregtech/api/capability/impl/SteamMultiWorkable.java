package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Collection;
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
            currentRecipe = findRecipe(maxVoltage, importInventory, importFluids, MatchingMode.IGNORE_FLUIDS);
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
    protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode mode) {
        int currentItemsEngaged = 0;
        final ArrayList<CountableIngredient> recipeInputs = new ArrayList<>();
        final ArrayList<ItemStack> recipeOutputs = new ArrayList<>();
        int recipeEUt = 0;
        int recipeDuration = 1;
        float speedBonusPercent = 0.0F; // Currently unused

        /* Iterate over input items looking for more items to process until we
         * have touched every item, or are at maximum item capacity
         */
        boolean matchedRecipe = false;
        boolean canFitOutputs = true;

        for (int index = 0; index < inputs.getSlots() && currentItemsEngaged < MAX_PROCESSES; index++) {
            final ItemStack currentInputItem = inputs.getStackInSlot(index);

            // Skip slot if empty
            if (currentInputItem.isEmpty())
                continue;

            // Check recipe for item in slot
            Recipe matchingRecipe = recipeMap.findRecipe(maxVoltage,
                    Collections.singletonList(currentInputItem),
                    Collections.emptyList(), 0,
                    MatchingMode.DEFAULT);
            CountableIngredient inputIngredient;
            if (matchingRecipe != null) {
                matchedRecipe = true;
                if (matchingRecipe.getOutputs().isEmpty())
                    return doChancedOnlyRecipe(matchingRecipe, currentInputItem);
                inputIngredient = matchingRecipe.getInputs().get(0);
                recipeEUt = matchingRecipe.getEUt();
                recipeDuration = matchingRecipe.getDuration();
            } else
                continue;

            // Some error handling, probably unnecessary
            if (inputIngredient == null)
                throw new IllegalStateException(
                        String.format("Recipe with null ingredient %s", matchingRecipe));

            // Check to see if we have enough output slots
            int itemsLeftUntilMax = (MAX_PROCESSES - currentItemsEngaged);
            if (itemsLeftUntilMax >= inputIngredient.getCount()) {

                // Make sure we don't go over maximum of 8 items per craft
                int recipeMultiplier = Math.min((currentInputItem.getCount() / inputIngredient.getCount()),
                        (itemsLeftUntilMax / inputIngredient.getCount()));

                // Process to see how many slots the output will take
                ArrayList<ItemStack> temp = new ArrayList<>(recipeOutputs);
                computeOutputItemStacks(temp, matchingRecipe.getOutputs().get(0), recipeMultiplier);

                // Check to see if we have output space available for the recipe
                canFitOutputs = InventoryUtils.simulateItemStackMerge(temp, this.getOutputInventory());
                if (!canFitOutputs)
                    break;

                // Create output ItemStack list
                temp.removeAll(recipeOutputs);
                recipeOutputs.addAll(temp);

                // Add ingredients to list of items to process
                recipeInputs.add(new CountableIngredient(inputIngredient.getIngredient(),
                        inputIngredient.getCount() * recipeMultiplier));

                currentItemsEngaged += inputIngredient.getCount() * recipeMultiplier;
            }
        }

        this.invalidInputsForRecipes = !matchedRecipe;
        this.isOutputsFull = !canFitOutputs;

        if (recipeInputs.isEmpty()) {
            return null;
        }

        return recipeMap.recipeBuilder()
                .inputsIngredients(recipeInputs)
                .outputs(recipeOutputs)
                .EUt(Math.min(32, (int) Math.ceil(recipeEUt * 1.33)))
                .duration(Math.max(recipeDuration, (int) (recipeDuration * (100.0F / (100.0F + speedBonusPercent)) * 1.5)))
                .build().getResult();
    }

    private void computeOutputItemStacks(Collection<ItemStack> recipeOutputs, ItemStack outputStack, int recipeAmount) {
        if (!outputStack.isEmpty()) {
            int finalAmount = outputStack.getCount() * recipeAmount;
            int maxCount = outputStack.getMaxStackSize();
            int numStacks = finalAmount / maxCount;
            int remainder = finalAmount % maxCount;

            for (int fullStacks = numStacks; fullStacks > 0; fullStacks--) {
                ItemStack full = outputStack.copy();
                full.setCount(maxCount);
                recipeOutputs.add(full);
            }

            if (remainder > 0) {
                ItemStack partial = outputStack.copy();
                partial.setCount(remainder);
                recipeOutputs.add(partial);
            }
        }
    }

    // This does no checking to see if outputs can fit, similar to other chanced output only recipes
    private Recipe doChancedOnlyRecipe(Recipe matchingRecipe, ItemStack stack) {
        RecipeBuilder<?> builder = recipeMap.recipeBuilder()
                .inputs(new CountableIngredient(matchingRecipe.getInputs().get(0).getIngredient(), stack.getCount()))
                .EUt(Math.min(32, (int) Math.ceil(matchingRecipe.getEUt() * 1.33)))
                .duration((int) (matchingRecipe.getDuration() * 1.5));

        Recipe.ChanceEntry entry = matchingRecipe.getChancedOutputs().get(0);
        int maxProcesses = Math.min(MAX_PROCESSES, stack.getCount());
        for (int i = 0; i < maxProcesses; i++)
            builder.chancedOutput(entry.getItemStack(), entry.getChance(), entry.getBoostPerTier());

        return builder.build().getResult();
    }
}
