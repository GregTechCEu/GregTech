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
                for (int slot = 0; slot < overlayedItemHandler.getSlots(); slot++) {
                    returnedAmount = overlayedItemHandler.insertItemStackKey(slot, entry.getKey(), amountToInsert);
                    if (returnedAmount > 0) {
                        amountToInsert = returnedAmount;
                    }
                    if (returnedAmount == 0) break;
                }
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
                for (int slot = 0; slot < overlayedItemHandler.getSlots(); slot++) {
                    returnedAmount = overlayedItemHandler.insertItemStackKey(slot, entry.getKey(), amountToInsert);
                    if (returnedAmount > 0) {
                        amountToInsert = returnedAmount;
                    }
                    if (returnedAmount == 0) break;
                }
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

    public static int[] adjustMultiplier(boolean mergedAll, int minMultiplier, int multiplier, int maxMultiplier) {
        if (mergedAll) {
            minMultiplier = multiplier;
            int remainder = (maxMultiplier - multiplier) % 2;
            multiplier = multiplier + remainder + (maxMultiplier - multiplier) / 2;
        } else {
            maxMultiplier = multiplier;
            int halfMul = multiplier / 2;
            if (halfMul < minMultiplier) {
                multiplier = minMultiplier + 1;
            } else {
                multiplier = halfMul;
            }
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

            for (Map.Entry<FluidKey, Integer> pair : recipeFluidOutputs.entrySet()) {
                amountLeft = pair.getValue() * multiplier;
                for (int tank = 0; tank < overlayedFluidHandler.getTankProperties().length; tank++) {
                    int inserted = overlayedFluidHandler.insertFluidKey(tank, pair.getKey(), amountLeft);
                    if (inserted > 0) {
                        amountLeft -= inserted;
                        if (amountLeft == 0) {
                            break;
                        }
                    }
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
}
