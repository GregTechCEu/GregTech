package gregtech.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.*;
import gregtech.api.util.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.*;

public class ParallelLogic {
    /**
     * @param recipe The recipe
     * @param inputs The item inputs
     * @param fluidInputs the fluid inputs
     * @param parallelAmount hard cap on the amount returned
     * @return returns the amount of possible time a recipe can be made from a given input inventory
     */

    public static int getMaxRecipeMultiplier(Recipe recipe, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int parallelAmount) {
        // Find all the items in the combined Item Input inventories and create oversized ItemStacks
        HashMap<ItemStackKey,Integer> ingredientStacks = GTHashMaps.fromItemHandler(inputs);

        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        HashMap<FluidKey,Integer> fluidStacks = GTHashMaps.fromFluidHandler(fluidInputs);

        // Find the maximum number of recipes that can be performed from the items in the item input inventories
        int itemMultiplier = getMaxRatioItem(ingredientStacks, recipe, parallelAmount);
        // Find the maximum number of recipes that can be performed from the fluids in the fluid input inventories
        int fluidMultiplier = getMaxRatioFluid(fluidStacks, recipe, parallelAmount);

        // Find the maximum number of recipes that can be performed from all available inputs
        return Math.min(itemMultiplier, fluidMultiplier);
    }

    /**
     *
     * @param recipe The recipe
     * @param outputs the item output inventory
     * @param fluidOutputs the fluid output tanks
     * @param parallelAmount the maximum expected amount
     * @return returns the amount of recipes that can be merged successfully into a given output inventory
     */
    public static int limitByOutputMerging(Recipe recipe, IItemHandlerModifiable outputs, IMultipleTankHandler fluidOutputs, int parallelAmount) {
        int maxMultiplierItems = parallelAmount;
        int maxMultiplierFluids = parallelAmount;
        if (recipe.getOutputs().size() > 0) {
            maxMultiplierItems = limitParallelByItems(recipe, new OverlayedItemHandler(outputs), parallelAmount);
            if (maxMultiplierItems == 0) {
                return 0;
            }
        }
        if (recipe.getFluidOutputs().size() > 0) {
            maxMultiplierFluids = limitParallelByFluids(recipe, new OverlayedFluidHandler(fluidOutputs), parallelAmount);
        }
        return Math.min(maxMultiplierItems, maxMultiplierFluids);
    }

    /**
     *
     * @param recipe the recipe from which we get the input to product ratio
     * @param multiplier the maximum possible multiplied we can get from the input inventory
     *                   see {@link ParallelLogic#getMaxRecipeMultiplier(Recipe, IItemHandlerModifiable, IMultipleTankHandler, int)}
     * @return the amount of times a {@link Recipe} outputs can be merged into an inventory without
     * voiding products.
     */
    public static int limitParallelByItems(Recipe recipe, OverlayedItemHandler overlayedItemHandler, int multiplier) {
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        HashMap<ItemStackKey, Integer> recipeOutputs = GTHashMaps.fromItemStackCollection(recipe.getOutputs());

        while (minMultiplier != maxMultiplier) {
            overlayedItemHandler.reset();

            int returnedAmount = 0;

            for (Map.Entry<ItemStackKey, Integer> entry : recipeOutputs.entrySet()) {
                int amountToInsert = entry.getValue() * multiplier;
                returnedAmount = overlayedItemHandler.insertStackedItemStackKey(entry.getKey(), amountToInsert);
                if (returnedAmount > 0) {
                    break;
                }
            }

            int[] bin = adjustMultiplier(returnedAmount == 0, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];

        }
        return multiplier;
    }

    /**
     * Used by the Multi Smelter and some parallellizable steam multiblocks
     *
     * @param recipeOutputList the recipe outputs from the recipe we are building up to its maximum parallel limit
     * @param outputsToAppend the recipe outputs from the recipe we want to append to the recipe we are building
     * @param multiplier the maximum possible multiplied we can get from the input inventory
     *                   see {@link ParallelLogic#getMaxRecipeMultiplier(Recipe, IItemHandlerModifiable, IMultipleTankHandler, int)}
     * @return the amount of times a {@link Recipe} outputs can be merged into an inventory without
     * voiding products.
     */
    public static int limitParallelByItemsIncremental(List<ItemStack> recipeOutputList, List<ItemStack> outputsToAppend, OverlayedItemHandler overlayedItemHandler, final int multiplier) {
        int minMultiplier = 0;
        int currentMultiplier = multiplier;
        int maxMultiplier = multiplier;
        int previousMutiplier = multiplier;

        HashMap<ItemStackKey, Integer> recipeOutputs = GTHashMaps.fromItemStackCollection(recipeOutputList);
        HashMap<ItemStackKey, Integer> recipeOutputsToAppend = GTHashMaps.fromItemStackCollection(outputsToAppend);

        HashMap<ItemStackKey, Integer> appendedResultMap = new HashMap<>(recipeOutputs);
        recipeOutputsToAppend.forEach((stackKey, amt) -> appendedResultMap.merge(stackKey,amt * multiplier, Integer::sum));

        while (minMultiplier != maxMultiplier) {
            overlayedItemHandler.reset();

            if (currentMultiplier != previousMutiplier) {
                int diff = currentMultiplier - previousMutiplier;
                recipeOutputsToAppend.forEach((sk, amt) -> {
                    appendedResultMap.put(sk, appendedResultMap.get(sk) + (amt * diff));
                });
                previousMutiplier = currentMultiplier;
            }

            int returnedAmount = 0;

            for (Map.Entry<ItemStackKey, Integer> entry : appendedResultMap.entrySet()) {
                int amountToInsert = entry.getValue();
                returnedAmount = overlayedItemHandler.insertStackedItemStackKey(entry.getKey(), amountToInsert);
                if (returnedAmount > 0) {
                    break;
                }
            }

            int[] bin = adjustMultiplier(returnedAmount == 0, minMultiplier, currentMultiplier, maxMultiplier);
            minMultiplier = bin[0];
            currentMultiplier = bin[1];
            maxMultiplier = bin[2];

        }
        return currentMultiplier;
    }

    /**
     * Binary-search-like approach to find the maximum amount that can be inserted
     * @param mergedAll if the merge was successful.
     *                 If true sets {@code minMultiplier} to the as the current multiplier
     *                  then sets {@code multiplier} to the sum of the mean difference between
     *                  {@code multiplier} and {@code maxMultiplier} plus the remainder of the division, if any,
     *                  and itself
     *                  If false, sets {@code maxMultiplier} as the current multiplier, then sets @code multiplier}
     *                  to half of its value limited it to no less or than the value of {@code minMultiplier}
     * @param minMultiplier the last known multiplier what was fully merged
     * @param multiplier the current multiplier
     * @param maxMultiplier the last know multiplier that resulted in simulation failure
     * @return an array consisting of the last known multiplier, new multiplier to be attempted and
     * the last know multiplier that resulted in failure
     */

    public static int[] adjustMultiplier(boolean mergedAll, int minMultiplier, int multiplier, int maxMultiplier) {
        if (mergedAll) {
            minMultiplier = multiplier;
            int remainder = (maxMultiplier - multiplier) % 2;
            multiplier = multiplier + remainder + (maxMultiplier - multiplier) / 2;
        } else {
            maxMultiplier = multiplier;
            multiplier = (multiplier + minMultiplier) / 2;
        }
        if (maxMultiplier - minMultiplier <= 1) {
            multiplier = maxMultiplier = minMultiplier;
        }
        return new int[]{minMultiplier, multiplier, maxMultiplier};
    }

    /**
     *
     * @param recipe the recipe from which we get the fluid input to product ratio
     * @param multiplier the maximum possible multiplied we can get from the input tanks
     *                   see {@link ParallelLogic#getMaxRecipeMultiplier(Recipe, IItemHandlerModifiable, IMultipleTankHandler, int)}
     * @return the amount of times a {@link Recipe} outputs can be merged into a fluid handler without
     * voiding products.
     */
    public static int limitParallelByFluids(Recipe recipe, OverlayedFluidHandler overlayedFluidHandler, int multiplier) {
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        HashMap<FluidKey, Integer> recipeFluidOutputs = GTHashMaps.fromFluidCollection(recipe.getFluidOutputs());

        while (minMultiplier != maxMultiplier) {
            overlayedFluidHandler.reset();

            int amountLeft = 0;

            for (Map.Entry<FluidKey, Integer> entry : recipeFluidOutputs.entrySet()) {
                amountLeft = entry.getValue() * multiplier;
                int inserted = overlayedFluidHandler.insertStackedFluidKey(entry.getKey(), amountLeft);
                if (inserted > 0) {
                    amountLeft -= inserted;
                }
                if (amountLeft > 0) {
                    break;
                }
            }

            int[] bin = adjustMultiplier(amountLeft == 0, minMultiplier, multiplier, maxMultiplier);
            minMultiplier = bin[0];
            multiplier = bin[1];
            maxMultiplier = bin[2];

        }
        return multiplier;
    }

    /**
     * Finds the maximum number of Recipes that can be performed at the same time based on the items in the item input inventory
     * @param countIngredients a {@link HashMap} of {@link ItemStackKey}s that is the result of calling {@link GTHashMaps#fromItemHandler(IItemHandler)}
     * @param recipe The {@link Recipe} for which to find the maximum that can be ran simultaneously
     * @param parallelAmount The limit on the amount of recipes that can be performed at one time
     * @return The Maximum number of Recipes that can be performed at a single time based on the available Items
     */
    protected static int getMaxRatioItem(HashMap<ItemStackKey, Integer> countIngredients, Recipe recipe, int parallelAmount) {

        int minMultiplier = Integer.MAX_VALUE;

        // Iterate through the recipe inputs
        for(CountableIngredient recipeInputs : recipe.getInputs()) {

            // Skip not consumed inputs
            if(recipeInputs.getCount() == 0) {
                continue;
            }

            // For every stack in the ingredients gathered from the input bus. This is most likely going to be oversized stacks
            for(Map.Entry<ItemStackKey, Integer> wholeItemStack : countIngredients.entrySet()) {
                if(recipeInputs.getIngredient().apply(wholeItemStack.getKey().getItemStackRaw())) {
                    //The ratio will either be set by the parallel limit, or the oversized stack divided by the amount of inputs the recipe takes
                    int ratio = Math.min(parallelAmount, wholeItemStack.getValue() / recipeInputs.getCount());
                    //Find the maximum number of recipes that can be performed by decrementing the ratio, which is limited
                    //by the number of machines (as absolute max), or the amount of ingredients in the input bus
                    if(ratio < minMultiplier) {
                        minMultiplier = ratio;
                    }
                    break;
                }

            }
        }
        return minMultiplier;
    }

    /**
     * Finds the maximum number of a specific recipe that can be performed based upon the fluids in the fluid inputs
     *
     * @param countFluid a {@link Set} of {@link FluidStack}s that is the result of calling {@link GTHashMaps#fromFluidHandler(IFluidHandler)}
     * @param recipe The {@link Recipe} for which to find the maximum that can be ran simultaneously
     * @param parallelAmount The limit on the amount of recipes that can be performed at one time
     * @return The Maximum number of Recipes that can be performed at a single time based on the available Fluids
     */
    protected static int getMaxRatioFluid(HashMap<FluidKey, Integer> countFluid, Recipe recipe, int parallelAmount) {

        int minMultiplier = Integer.MAX_VALUE;

        // Iterate through the fluid inputs in the recipe
        for(FluidStack fs : recipe.getFluidInputs()) {

            // Skip Not consumed Fluid inputs
            if(fs.amount == 0) {
                continue;
            }

            // Iterate through the fluids in the input hatches. This will likely be oversized stacks
            for(Map.Entry<FluidKey, Integer> inputStack : countFluid.entrySet()) {

                if(new FluidKey(fs).equals(inputStack.getKey())) {
                    //The ratio will either be set by the parallel limit, or the oversized stack divided by the amount of inputs the recipe takes
                    int ratio = Math.min(parallelAmount, inputStack.getValue() / fs.amount);

                    //Find the maximum number of recipes that can be performed by decrementing the ratio, which is limited
                    //by the number of machines (as absolute max), or the amount of ingredients in the input bus
                    if(ratio < minMultiplier) {
                        minMultiplier = ratio;
                    }
                    break;
                }
            }
        }

        return minMultiplier;
    }

    public static RecipeBuilder<?> doParallelRecipes(RecipeBuilder<?> recipeBuilder, Recipe currentRecipe, IItemHandlerModifiable importInventory, IMultipleTankHandler importFluids, IItemHandlerModifiable exportInventory, IMultipleTankHandler exportFluids, int parallelAmount) {
        int multiplierByInputs = getMaxRecipeMultiplier(currentRecipe, importInventory, importFluids, parallelAmount);
        if (multiplierByInputs > 1) {
            // Simulate the merging of the maximum amount of recipes
            // and limit by the amount we can successfully merge
            int limitByOutput = ParallelLogic.limitByOutputMerging(currentRecipe, exportInventory, exportFluids, multiplierByInputs);
            int parallelizable = Math.min(multiplierByInputs, limitByOutput);

            if (parallelizable > 1) {
                recipeBuilder.append(currentRecipe, parallelizable);
            } else if (parallelizable == 0) {
                return null;
            }
        }
        return recipeBuilder;
    }

    public static RecipeBuilder<?> appendRecipes(RecipeMap<?> recipeMap, IItemHandlerModifiable importInventory, IMultipleTankHandler importFluids, IItemHandlerModifiable exportInventory, IMultipleTankHandler exportFluids, int parallelAmount, long maxVoltage) {
        RecipeBuilder<?> recipeBuilder = null;

        OverlayedItemHandler overlayedItemHandler = new OverlayedItemHandler(exportInventory);

        // Iterate over the input items looking for more things to add until we run either out of input items
        // or we have exceeded the number of items permissible from the smelting bonus
        int engagedItems = 0;

        for (int index = 0; index < importInventory.getSlots(); index++) {
            // Skip this slot if it is empty.
            final ItemStack currentInputItem = importInventory.getStackInSlot(index);
            if (currentInputItem.isEmpty())
                continue;

            // Determine if there is a valid recipe for this item. If not, skip it.
            Recipe matchingRecipe = recipeMap.findRecipe(maxVoltage,
                    Collections.singletonList(currentInputItem),
                    Collections.emptyList(), 0, MatchingMode.IGNORE_FLUIDS);

            CountableIngredient inputIngredient;
            if (matchingRecipe != null) {
                inputIngredient = matchingRecipe.getInputs().get(0);
                if (recipeBuilder == null) {
                    recipeBuilder = recipeMap.recipeBuilder();
                }
            } else
                continue;

            // There's something not right with this recipe if the ingredient is null.
            if (inputIngredient == null)
                throw new IllegalStateException(
                        String.format("Got recipe with null ingredient %s", matchingRecipe));

            //equivalent of getting the max ratio from the inputs from Parallel logic
            int amountOfCurrentItem = Math.min(parallelAmount - engagedItems, currentInputItem.getCount());

            //how much we can add to the output inventory
            int limitByOutput = limitParallelByItemsIncremental(recipeBuilder.getOutputs(), matchingRecipe.getOutputs(), overlayedItemHandler, amountOfCurrentItem);

            //amount to actually multiply the recipe by
            int multiplierRecipeAmount = Math.min(amountOfCurrentItem, limitByOutput);

            if (multiplierRecipeAmount > 0) {
                recipeBuilder.append(matchingRecipe, multiplierRecipeAmount);
                engagedItems += multiplierRecipeAmount;
            }

            if (engagedItems == parallelAmount) {
                break;
            }
        }
        if (engagedItems > 0) {
            return recipeBuilder;
        } else {
            return null;
        }
    }

}
