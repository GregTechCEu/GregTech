package gregtech.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.*;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.ItemStackKey;
import gregtech.api.util.OverlayedFluidHandler;
import gregtech.api.util.OverlayedItemHandler;
import gregtech.api.util.StreamUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gregtech.api.util.Predicates.not;

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
        HashMap<ItemStackKey,Integer> ingredientStacks = itemHandler2StackKeyMap(inputs);

        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        HashMap<FluidKey,Integer> fluidStacks = fluidHandler2FluidKeyMap(fluidInputs);

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
    public static int limitByOutputMerging(Recipe recipe, IItemHandlerModifiable outputs, IMultipleTankHandler fluidOutputs, int parallelAmount){
        int maxMultiplier = parallelAmount;
        if (recipe.getOutputs().size() > 0) {
            maxMultiplier = limitParallelByItems(recipe, new OverlayedItemHandler(outputs), parallelAmount);
            if (maxMultiplier == 0) {
                return 0;
            }
        }
        if (recipe.getFluidOutputs().size() > 0) {
            maxMultiplier = limitParallelByFluids(recipe, new OverlayedFluidHandler(fluidOutputs), parallelAmount);
        }
        return maxMultiplier;
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

        HashMap<ItemStackKey, Integer> recipeOutputs = itemCollection2StackKeyMap(recipe.getOutputs());

        while (minMultiplier != maxMultiplier) {
            overlayedItemHandler.reset();

            int returnedAmount = 0;

            for (Map.Entry<ItemStackKey, Integer> pair : recipeOutputs.entrySet()) {
                int amountToInsert = pair.getValue() * multiplier;
                for (int slot = 0; slot < overlayedItemHandler.getSlots(); slot++) {
                    returnedAmount = overlayedItemHandler.insertItemStackKey(slot, pair.getKey(), amountToInsert);
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

        while (minMultiplier != maxMultiplier) {
            overlayedFluidHandler.reset();

            int amountLeft = 0;

            for (int recipeTankSlot = 0; recipeTankSlot < recipe.getFluidOutputs().size(); recipeTankSlot++) {
                FluidStack fluidStack = recipe.getFluidOutputs().get(recipeTankSlot);
                amountLeft = fluidStack.amount * multiplier;
                for (int tank = 0; tank < overlayedFluidHandler.getTankProperties().length; tank++) {
                    int inserted = overlayedFluidHandler.insertFluidKey(tank, new FluidKey(fluidStack), amountLeft);
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
     * Maps all items in the {@link IItemHandler} into a {@link ItemStackKey}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @return a {@link HashMap} of {@link ItemStackKey}s comprising of oversized stacks for each unique item in the input inventory
     */
    public static HashMap<ItemStackKey,Integer> itemHandler2StackKeyMap(IItemHandler inputs) {
        final Supplier<Map<ItemStackKey, Integer>> mapSupplier = Object2IntLinkedOpenHashMap::new;

        // Create a single stack of the combined count for each item
        return new HashMap<>(StreamUtils.streamFrom(inputs)
                // keep only non-empty item stacks
                .filter(not(ItemStack::isEmpty))
                // Track the number of identical items
                .collect(Collectors.toMap(KeySharedStack::getRegisteredStack,
                        ItemStack::getCount,
                        Math::addExact,
                        mapSupplier)));
    }

    /**
     * Maps all items in the {@link ItemStack} {@link Collection} into a {@link ItemStackKey}, {@link Integer} value as amount
     *
     * @param inputs The inventory handler of the inventory
     * @return a {@link HashMap} of {@link ItemStackKey}s comprising of oversized stacks for each unique item in the input inventory
     */
    public static HashMap<ItemStackKey,Integer> itemCollection2StackKeyMap(Collection<ItemStack> inputs) {
        final Supplier<Map<ItemStackKey, Integer>> mapSupplier = Object2IntLinkedOpenHashMap::new;

        // Create a single stack of the combined count for each item
        return new HashMap<>(inputs.stream()
                // keep only non-empty item stacks
                .filter(not(ItemStack::isEmpty))
                // Track the number of identical items
                .collect(Collectors.toMap(KeySharedStack::getRegisteredStack,
                        ItemStack::getCount,
                        Math::addExact,
                        mapSupplier)));
    }

    /**
     * Finds the maximum number of Recipes that can be performed at the same time based on the items in the item input inventory
     * @param countIngredients a {@link HashMap} of {@link ItemStackKey}s that is the result of calling {@link ParallelLogic#itemHandler2StackKeyMap(IItemHandler)}
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
     * Maps all fluids in the {@link IFluidHandler} into a {@link FluidKey}, {@link Integer} value as amount
     *
     * @param fluidInputs The combined fluid input inventory handler, in the form of an {@link IFluidHandler}
     * @return a {@link Set} of unique {@link FluidKey}s for each fluid in the handler. Will be oversized stacks if required
     */
    public static HashMap<FluidKey,Integer> fluidHandler2FluidKeyMap(IFluidHandler fluidInputs) {
        final Supplier<Map<FluidKey, Integer>> mapSupplier = Object2IntLinkedOpenHashMap::new;

        // Create a single stack of the combined count for each item
        return new HashMap<>(StreamUtils.streamFrom(fluidInputs)
                // keep only non-empty item stacks
                .filter(Objects::nonNull)
                // Track the number of identical items
                .collect(Collectors.toMap(FluidKey::new,
                        (fluidStack -> fluidStack.amount),
                        Math::addExact,
                        mapSupplier)));
    }

    /**
     * Maps all fluids in the {@link FluidStack} {@link Collection} into a {@link FluidKey}, {@link Integer} value as amount
     *
     * @param fluidInputs The combined fluid input inventory handler, in the form of an {@link IFluidHandler}
     * @return a {@link Set} of unique {@link FluidKey}s for each fluid in the handler. Will be oversized stacks if required
     */
    public static HashMap<FluidKey,Integer> fluidCollection2FluidKeyMap(Collection<FluidStack> fluidInputs) {
        final Supplier<Map<FluidKey, Integer>> mapSupplier = Object2IntLinkedOpenHashMap::new;

        // Create a single stack of the combined count for each item
        return new HashMap<>(fluidInputs.stream()
                // keep only non-empty item stacks
                .filter(Objects::nonNull)
                // Track the number of identical items
                .collect(Collectors.toMap(FluidKey::new,
                        (fluidStack -> fluidStack.amount),
                        Math::addExact,
                        mapSupplier)));
    }

    /**
     * Finds the maximum number of a specific recipe that can be performed based upon the fluids in the fluid inputs
     *
     * @param countFluid a {@link Set} of {@link FluidStack}s that is the result of calling {@link ParallelLogic#fluidHandler2FluidKeyMap(IFluidHandler)}
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
