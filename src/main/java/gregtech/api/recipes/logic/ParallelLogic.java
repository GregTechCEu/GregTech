package gregtech.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.*;
import gregtech.api.util.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.*;

public class ParallelLogic {
    /**
     * @param recipe         The recipe
     * @param inputs         The item inputs
     * @param fluidInputs    the fluid inputs
     * @param parallelAmount hard cap on the amount returned
     * @return returns the amount of possible time a recipe can be made from a given input inventory
     */

    public static int getMaxRecipeMultiplier(Recipe recipe, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int parallelAmount) {
        // Find all the items in the combined Item Input inventories and create oversized ItemStacks
        HashMap<ItemStackKey, Integer> ingredientStacks = GTHashMaps.fromItemHandler(inputs);

        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        HashMap<FluidKey, Integer> fluidStacks = GTHashMaps.fromFluidHandler(fluidInputs);

        // Find the maximum number of recipes that can be performed from the items in the item input inventories
        int itemMultiplier = getMaxRatioItem(ingredientStacks, recipe, parallelAmount);
        // Find the maximum number of recipes that can be performed from the fluids in the fluid input inventories
        int fluidMultiplier = getMaxRatioFluid(fluidStacks, recipe, parallelAmount);
        // If both fail to return a valid amount
        if ((itemMultiplier == Integer.MAX_VALUE && recipe.getInputs().size() > 0) || (fluidMultiplier == Integer.MAX_VALUE &&
                recipe.getFluidInputs().size() > 0)) {
            return 0;
        }
        // Find the maximum number of recipes that can be performed from all available inputs
        return Math.min(itemMultiplier, fluidMultiplier);
    }

    /**
     * @param recipe         The recipe
     * @param outputs        the item output inventory
     * @param fluidOutputs   the fluid output tanks
     * @param parallelAmount the maximum expected amount
     * @return returns the amount of recipes that can be merged successfully into a given output inventory
     */
    public static int limitByOutputMerging(Recipe recipe, IItemHandlerModifiable outputs, IMultipleTankHandler fluidOutputs, int parallelAmount) {
        if (recipe.getOutputs().size() > 0) {
            parallelAmount = limitParallelByItems(recipe, new OverlayedItemHandler(outputs), parallelAmount);
            if (parallelAmount == 0) {
                return 0;
            }
        }
        if (recipe.getFluidOutputs().size() > 0) {
            parallelAmount = limitParallelByFluids(recipe, new OverlayedFluidHandler(fluidOutputs), parallelAmount);
        }
        return parallelAmount;
    }

    /**
     * @param recipe     the recipe from which we get the input to product ratio
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
     * @param outputsToAppend  the recipe outputs from the recipe we want to append to the recipe we are building
     * @param multiplier       the maximum possible multiplied we can get from the input inventory
     *                         see {@link ParallelLogic#getMaxRecipeMultiplier(Recipe, IItemHandlerModifiable, IMultipleTankHandler, int)}
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
        recipeOutputsToAppend.forEach((stackKey, amt) -> appendedResultMap.merge(stackKey, amt * multiplier, Integer::sum));

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
     *
     * @param mergedAll     if the merge was successful.
     *                      If true sets {@code minMultiplier} to the as the current multiplier
     *                      then sets {@code multiplier} to the sum of the mean difference between
     *                      {@code multiplier} and {@code maxMultiplier} plus the remainder of the division, if any,
     *                      and itself
     *                      If false, sets {@code maxMultiplier} as the current multiplier, then sets @code multiplier}
     *                      to half of its value limited it to no less or than the value of {@code minMultiplier}
     * @param minMultiplier the last known multiplier what was fully merged
     * @param multiplier    the current multiplier
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
     * @param recipe     the recipe from which we get the fluid input to product ratio
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
     *
     * @param countIngredients a {@link HashMap} of {@link ItemStackKey}s that is the result of calling {@link GTHashMaps#fromItemHandler(IItemHandler)}
     * @param recipe           The {@link Recipe} for which to find the maximum that can be ran simultaneously
     * @param parallelAmount   The limit on the amount of recipes that can be performed at one time
     * @return The Maximum number of Recipes that can be performed at a single time based on the available Items
     */
    protected static int getMaxRatioItem(HashMap<ItemStackKey, Integer> countIngredients, Recipe recipe, int parallelAmount) {
        int minMultiplier = Integer.MAX_VALUE;
        //map the recipe ingredients to account for duplicated and notConsumable ingredients.
        //notConsumable ingredients are not counted towards the max ratio
        IngredientHashStrategy hashStrategy = new IngredientHashStrategy();
        Object2IntOpenCustomHashMap<Ingredient> notConsumableMap = new Object2IntOpenCustomHashMap<>(hashStrategy);
        Object2IntOpenCustomHashMap<Ingredient> countableMap = new Object2IntOpenCustomHashMap<>(hashStrategy);
        for (CountableIngredient recipeInputs : recipe.getInputs()) {
            int ingredientCount = recipeInputs.getCount();
            if (ingredientCount > 0) {
                countableMap.computeIfPresent(recipeInputs.getIngredient(), (k, v) -> v + recipeInputs.getCount());
                countableMap.putIfAbsent(recipeInputs.getIngredient(), recipeInputs.getCount());
            } else {
                notConsumableMap.computeIfPresent(recipeInputs.getIngredient(), (k, v) -> v + 1);
                notConsumableMap.putIfAbsent(recipeInputs.getIngredient(), 1);
            }
        }

        // Iterate through the recipe inputs, excluding the not consumable ingredients from the inventory map
        for (Map.Entry<Ingredient, Integer> recipeInputEntry : notConsumableMap.entrySet()) {
            int needed = recipeInputEntry.getValue();
            int available = 0;
            // For every stack in the ingredients gathered from the input bus.
            for (Map.Entry<ItemStackKey, Integer> inventoryEntry : countIngredients.entrySet()) {
                if (recipeInputEntry.getKey().apply(inventoryEntry.getKey().getItemStackRaw())) {
                    available = inventoryEntry.getValue();
                    if (available > needed) {
                        inventoryEntry.setValue(available - needed);
                        available -= needed;
                        break;
                    } else {
                        inventoryEntry.setValue(0);
                        recipeInputEntry.setValue(needed - available);
                        needed -= available;
                    }
                }
            }
            if (needed > available) {
                return 0;
            }
        }

        // Iterate through the recipe inputs
        for (Map.Entry<Ingredient, Integer> recipeInputEntry : countableMap.entrySet()) {
            int needed = recipeInputEntry.getValue();
            int available = 0;
            // For every stack in the ingredients gathered from the input bus.
            for (Map.Entry<ItemStackKey, Integer> inventoryEntry : countIngredients.entrySet()) {
                if (recipeInputEntry.getKey().apply(inventoryEntry.getKey().getItemStackRaw())) {
                    available += inventoryEntry.getValue();
                }
            }
            if (available >= needed) {
                int ratio = Math.min(parallelAmount, available / needed);
                if (ratio < minMultiplier) {
                    minMultiplier = ratio;
                }
            } else {
                return 0;
            }
        }
        return minMultiplier;
    }

    /**
     * Finds the maximum number of a specific recipe that can be performed based upon the fluids in the fluid inputs
     *
     * @param countFluid     a {@link Set} of {@link FluidStack}s that is the result of calling {@link GTHashMaps#fromFluidHandler(IFluidHandler)}
     * @param recipe         The {@link Recipe} for which to find the maximum that can be ran simultaneously
     * @param parallelAmount The limit on the amount of recipes that can be performed at one time
     * @return The Maximum number of Recipes that can be performed at a single time based on the available Fluids
     */
    protected static int getMaxRatioFluid(HashMap<FluidKey, Integer> countFluid, Recipe recipe, int parallelAmount) {
        int minMultiplier = Integer.MAX_VALUE;
        //map the recipe input fluids to account for duplicated fluids,
        //so their sum is counted against the total of fluids available in the input
        HashMap<FluidKey, Integer> fluidCountMap = new HashMap<>();
        HashMap<FluidKey, Integer> notConsumableMap = new HashMap<>();
        for (FluidStack fluidStack : recipe.getFluidInputs()) {
            int fluidAmount = fluidStack.amount;
            if (fluidAmount == 0) {
                notConsumableMap.computeIfPresent(new FluidKey(fluidStack), (k, v) -> v + 1);
                notConsumableMap.putIfAbsent(new FluidKey(fluidStack), 1);
            } else {
                fluidCountMap.computeIfPresent(new FluidKey(fluidStack), (k, v) -> v + fluidAmount);
                fluidCountMap.putIfAbsent(new FluidKey(fluidStack), fluidAmount);
            }
        }

        // Iterate through the recipe inputs, excluding the not consumable fluids from the fluid inventory map
        for (Map.Entry<FluidKey, Integer> notConsumableFluid : notConsumableMap.entrySet()) {
            int needed = notConsumableFluid.getValue();
            int available = 0;
            // For every fluid gathered from the fluid inputs.
            for (Map.Entry<FluidKey, Integer> inputFluid : countFluid.entrySet()) {
                if (notConsumableFluid.getKey().equals(inputFluid.getKey())) {
                    available = inputFluid.getValue();
                    if (available >= needed) {
                        inputFluid.setValue(available - needed);
                        available -= needed;
                        break;
                    } else {
                        inputFluid.setValue(0);
                        notConsumableFluid.setValue(needed - available);
                        needed -= available;
                    }
                }
            }
            if (needed > available) {
                return 0;
            }
        }

        // Iterate through the fluid inputs in the recipe
        for (Map.Entry<FluidKey, Integer> fs : fluidCountMap.entrySet()) {
            int needed = fs.getValue();
            int available = 0;
            // For every fluid gathered from the fluid inputs.
            for (Map.Entry<FluidKey, Integer> inputFluid : countFluid.entrySet()) {
                if (fs.getKey().equals(inputFluid.getKey())) {
                    available += inputFluid.getValue();
                }
            }
            if (available > needed) {
                int ratio = Math.min(parallelAmount, available / needed);
                if (ratio < minMultiplier) {
                    minMultiplier = ratio;
                }
            } else {
                return 0;
            }
        }
        return minMultiplier;
    }

    public static RecipeBuilder<?> doParallelRecipes(Recipe currentRecipe, RecipeMap<?> recipeMap, IItemHandlerModifiable importInventory, IMultipleTankHandler importFluids, IItemHandlerModifiable exportInventory, IMultipleTankHandler exportFluids, int parallelAmount, long maxVoltage, boolean trimOutputs, boolean canVoidRecipeOutputs) {
        int multiplierByInputs = getMaxRecipeMultiplier(currentRecipe, importInventory, importFluids, parallelAmount);
        if (multiplierByInputs == 0) {
            return null;
        }
        RecipeBuilder<?> recipeBuilder = recipeMap.recipeBuilder();

        // Simulate the merging of the maximum amount of recipes
        // and limit by the amount we can successfully merge
        int limitByOutput = Integer.MAX_VALUE;
        if(!canVoidRecipeOutputs) {
            limitByOutput = ParallelLogic.limitByOutputMerging(currentRecipe, exportInventory, exportFluids, multiplierByInputs);
        }
        int limitByVoltage = Math.abs((int) (maxVoltage / currentRecipe.getEUt()));
        int parallelizable = Math.min(limitByVoltage, Math.min(multiplierByInputs, limitByOutput));

        if (parallelizable > 0) {
            recipeBuilder.append(currentRecipe, parallelizable, false, trimOutputs);
        }

        return recipeBuilder;
    }

    /**
     * Constructs a {@link RecipeBuilder} containing the recipes from the ItemStacks available in the {@code importInventory}
     * Does NOT take fluids into account whatsoever
     *
     * @param recipeMap       The {@link RecipeMap} to search for recipes
     * @param importInventory The {@link IItemHandlerModifiable} that contains the items to be used as inputs
     * @param exportInventory The {@link IItemHandlerModifiable} that contains the items to be used as outputs
     * @param parallelAmount  The maximum amount of recipes that can be performed at one time
     * @param maxVoltage      The maximum voltage of the machine
     * @return A {@link RecipeBuilder} containing the recipes that can be performed in parallel, limited by the ingredients available, and the output space available.
     */
    public static RecipeBuilder<?> appendItemRecipes(RecipeMap<?> recipeMap, IItemHandlerModifiable importInventory, IItemHandlerModifiable exportInventory, int parallelAmount, long maxVoltage, boolean trimOutputs, boolean canVoidRecipeOutputs) {
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
            int ingredientRatio = Math.min(parallelAmount - engagedItems, currentInputItem.getCount() / Math.max(matchingRecipe.getInputs().get(0).getCount(), 1));

            //how much we can add to the output inventory
            int limitByOutput = Integer.MAX_VALUE;
            if(!canVoidRecipeOutputs) {
                limitByOutput = limitParallelByItemsIncremental(recipeBuilder.getOutputs(), matchingRecipe.getOutputs(), overlayedItemHandler, ingredientRatio);
            }

            //amount to actually multiply the recipe by
            int multiplierRecipeAmount = Math.min(ingredientRatio, limitByOutput);

            if (multiplierRecipeAmount > 0) {
                recipeBuilder.append(matchingRecipe, multiplierRecipeAmount, true, trimOutputs);
                engagedItems += multiplierRecipeAmount;
            }

            if (engagedItems == parallelAmount) {
                break;
            }
        }
        return recipeBuilder;
    }
}
