package gregtech.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.*;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.ItemStackKey;
import gregtech.api.util.StreamUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gregtech.api.util.Predicates.not;

public class ParallelLogic {
    /**
     *
     * Multiplies the passed {@link Recipe} by the amount specified by multiplier
     *
     * @param recipe The Recipe to be multiplied
     * @param recipeMap The Recipe Map that the provided recipe is from
     * @param multiplier Amount to multiply the recipe by
     * @return the builder holding the multiplied recipe
     */

    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> multiply(Recipe recipe, RecipeMap<R> recipeMap, int multiplier) {

        RecipeBuilder<R> newRecipeBuilder = recipeMap.recipeBuilder();

        for (Map.Entry<RecipeProperty<?>, Object> property : recipe.getPropertyValues()) {
            newRecipeBuilder.applyProperty(property.getKey().getKey(), property.getValue());
        }

        // Create holders for the various parts of the new multiplied Recipe
        List<CountableIngredient> newRecipeInputs = new ArrayList<>();
        List<FluidStack> newFluidInputs = new ArrayList<>();
        List<ItemStack> outputItems = new ArrayList<>();
        List<FluidStack> outputFluids = new ArrayList<>();

        // Populate the various holders of the multiplied Recipe
        multiplyInputsAndOutputs(newRecipeInputs, newFluidInputs, outputItems, outputFluids, recipe, multiplier);

        // Build the new Recipe with multiplied components
        newRecipeBuilder.inputsIngredients(newRecipeInputs);
        newRecipeBuilder.fluidInputs(newFluidInputs);
        newRecipeBuilder.outputs(outputItems);
        newRecipeBuilder.fluidOutputs(outputFluids);
        newRecipeBuilder.EUt(recipe.getEUt());
        newRecipeBuilder.duration(recipe.getDuration() * multiplier);

        copyChancedItemOutputs(newRecipeBuilder, recipe, multiplier);

        return newRecipeBuilder;
    }

    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> append(RecipeBuilder<R> recipeBuilder, Recipe recipe, int multiplier) {

        for (Map.Entry<RecipeProperty<?>, Object> property : recipe.getPropertyValues()) {
            recipeBuilder.applyProperty(property.getKey().getKey(), property.getValue());
        }

        // Create holders for the various parts of the new multiplied Recipe
        List<CountableIngredient> newRecipeInputs = new ArrayList<>();
        List<FluidStack> newFluidInputs = new ArrayList<>();
        List<ItemStack> outputItems = new ArrayList<>();
        List<FluidStack> outputFluids = new ArrayList<>();

        // Populate the various holders of the multiplied Recipe
        multiplyInputsAndOutputs(newRecipeInputs, newFluidInputs, outputItems, outputFluids, recipe, multiplier);

        // Build the new Recipe with multiplied components
        recipeBuilder.inputsIngredients(newRecipeInputs);
        recipeBuilder.fluidInputs(newFluidInputs);
        recipeBuilder.outputs(outputItems);
        recipeBuilder.fluidOutputs(outputFluids);
        recipeBuilder.EUt(recipe.getEUt());
        recipeBuilder.duration(recipe.getDuration());

        copyChancedItemOutputs(recipeBuilder, recipe, multiplier);

        return recipeBuilder;
    }

    /**
     * @param recipe The recipe
     * @param inputs The item inputs
     * @param fluidInputs the fluid inputs
     * @param parallelAmount hard cap on the amount returned
     * @return returns the amount of possible time a recipe can be made from a given input inventory
     */

    public static int getMaxRecipeMultiplier(Recipe recipe, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int parallelAmount) {
        // Find all the items in the combined Item Input inventories and create oversized ItemStacks
        HashMap<ItemStackKey,Integer> ingredientStacks = findAllItemsInInputs(inputs);

        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        HashMap<FluidKey,Integer> fluidStacks = findAllFluidsInInputs(fluidInputs);

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
        int maxMultiplier = 0;
        if (recipe.getOutputs().size() > 0) {
            maxMultiplier = limitParallelByItems(recipe, outputs, parallelAmount);
            if (maxMultiplier == 0) {
                return 0;
            }
        }
        if (recipe.getFluidOutputs().size() > 0) {
            maxMultiplier = limitParallelByFluids(recipe, fluidOutputs, parallelAmount);
        }
        return maxMultiplier;
    }

    /**
     *
     * @param recipe the recipe from which we get the input to product to ratio
     * @param outputInventory the output inventory we want to merge into
     * @param multiplier the maximum possible multiplied we can get from the input inventory
     *                   see {@link ParallelLogic#getMaxRecipeMultiplier(Recipe, IItemHandlerModifiable, IMultipleTankHandler, int)}
     * @return the amount of times a {@link Recipe} outputs can be merged into an inventory without
     * voiding products.
     *
     * uses a binary-search-like for the merge of recipes that have more than one kind of item
     */
    public static int limitParallelByItems(Recipe recipe, IItemHandlerModifiable outputInventory, int multiplier) {
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        //mirror the output slots status into a map. Keep the empty slots,
        //so we can simulate the merge without copying stacks.

        Map<Integer, Triple<ItemStackKey, Integer, Integer>> outputInvMap = mapInvHandler(outputInventory);
        List<Pair<ItemStackKey, Integer>> recipeOutputs = stackList2stackKeyList(recipe.getOutputs());

        int amount = 0;

        if (recipeOutputs.size() == 1) {
            amount = MetaTileEntity.simulateAddHashedItemToInvMap(recipeOutputs.get(0).getLeft(), recipeOutputs.get(0).getRight(), outputInvMap);
            if (amount > 0 ) {
                multiplier -=  amount / recipe.getOutputs().get(0).getCount();
                if (amount % recipe.getOutputs().get(0).getCount() != 0) {
                    multiplier -= 1;
                }
            }
        } else {
            while (minMultiplier != maxMultiplier) {
                Map<Integer, Triple<ItemStackKey, Integer, Integer>> invCopyMap = new LinkedHashMap<>(outputInvMap);
                for (Pair<ItemStackKey, Integer> pair : recipeOutputs) {
                    int amountToInsert = pair.getRight() * multiplier;
                    amount = MetaTileEntity.simulateAddHashedItemToInvMap(pair.getLeft(), amountToInsert, invCopyMap);
                    if (amount > 0) {
                        break;
                    }
                }

                int[] bin = adjustMultiplier(amount == 0, minMultiplier, multiplier, maxMultiplier);
                minMultiplier = bin[0];
                multiplier = bin[1];
                maxMultiplier = bin[2];

            }
        }
        return multiplier;
    }

    public static int[] adjustMultiplier(boolean mergedAll, int minMultiplier, int multiplier, int maxMultiplier) {
        if (mergedAll) {
            if (maxMultiplier - minMultiplier <= 1) {
                multiplier = maxMultiplier = minMultiplier;
            } else {
                minMultiplier = multiplier;
                int remainder = (maxMultiplier - multiplier) % 2;
                multiplier = multiplier + remainder + (maxMultiplier - multiplier) / 2;
            }
        } else {
            maxMultiplier = multiplier;
            multiplier /= 2;
            if (multiplier < minMultiplier) {
                multiplier += 1;
            }
        }
        return new int[]{minMultiplier, multiplier, maxMultiplier};
    }

    public static Map<Integer, Triple<ItemStackKey, Integer, Integer>> mapInvHandler(IItemHandler itemHandlerModifiable) {

        Map<Integer, Triple<ItemStackKey, Integer, Integer>> handlerMap = new LinkedHashMap<>();
        for (int slot = 0; slot < itemHandlerModifiable.getSlots(); slot++) {
            ItemStack stack = itemHandlerModifiable.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                handlerMap.put(slot,
                        Triple.of(KeySharedStack.getRegisteredStack(stack),
                                stack.getCount(), itemHandlerModifiable.getSlotLimit(slot)));
            } else {
                handlerMap.put(slot,
                        Triple.of(null,
                                0, itemHandlerModifiable.getSlotLimit(slot)));
            }
        }
        return handlerMap;
    }

    public static List<Pair<ItemStackKey, Integer>> stackList2stackKeyList(List<ItemStack> stackList) {
        List<Pair<ItemStackKey, Integer>> stackKeyList = new LinkedList<>();
        for (ItemStack stack : stackList) {
            if (!stack.isEmpty()) {
                stackKeyList.add(
                        Pair.of(KeySharedStack.getRegisteredStack(stack),
                                stack.getCount()));
            } else {
                stackKeyList.add(
                        Pair.of(null,
                                stack.getCount()));
            }
        }
        return stackKeyList;
    }

    public static int limitParallelByFluids(Recipe recipe, IMultipleTankHandler fluidOutputs, int multiplier) {
        int minMultiplier = 0;
        int maxMultiplier = multiplier;

        //mirror the output slots status into a map. Keep the empty slots,
        //so we can simulate the merge without copying stacks.

        Map<Integer, Triple<FluidKey, Integer, Integer>> outputFluidList = mapFluidHandler(fluidOutputs);

        int amount = 0;

        if (fluidOutputs.getTanks() == 1) {
            amount = MetaTileEntity.simulateAddHashedFluidToTankMap(outputFluidList.get(0).getLeft(), outputFluidList.get(0).getMiddle(), outputFluidList);
            if (amount > 0) {
                multiplier -= amount / recipe.getOutputs().get(0).getCount();
                if (amount % recipe.getOutputs().get(0).getCount() != 0) {
                    multiplier -= 1;
                }
            }
        } else {
            while (minMultiplier != maxMultiplier) {
                Map<Integer, Triple<FluidKey, Integer, Integer>> outputFluidListCopy = new LinkedHashMap<>(outputFluidList);

                for (int recipeTankSlot = 0; recipeTankSlot < recipe.getFluidOutputs().size(); recipeTankSlot++) {
                    FluidStack fluidStack = recipe.getFluidOutputs().get(recipeTankSlot);
                    int amountToInsert = fluidStack.amount * multiplier;
                    amount = MetaTileEntity.simulateAddHashedFluidToTankMap(new FluidKey(fluidStack), amountToInsert, outputFluidListCopy);
                    if (amount > 0) {
                        break;
                    }
                }

                int[] bin = adjustMultiplier(amount > 0, minMultiplier, multiplier, maxMultiplier);
                minMultiplier = bin[0];
                multiplier = bin[1];
                maxMultiplier = bin[2];

            }
        }
        return multiplier;
    }

    public static Map<Integer, Triple<FluidKey, Integer, Integer>> mapFluidHandler(IFluidHandler fluidHandler) {
        Map<Integer, Triple<FluidKey, Integer, Integer>> tankHandlerMap = new LinkedHashMap<>();

        for (int tank = 0; tank < fluidHandler.getTankProperties().length; tank++) {
            IFluidTankProperties tankProperties = fluidHandler.getTankProperties()[tank];
            FluidStack fluidStack = tankProperties.getContents();
            if (fluidStack != null) {
                tankHandlerMap.put(tank,
                        Triple.of(new FluidKey(fluidStack),
                                fluidStack.amount, tankProperties.getCapacity()));
            } else {
                tankHandlerMap.put(tank,
                        Triple.of(null, 0, tankProperties.getCapacity()));
            }
        }
        return tankHandlerMap;
    }

        /**
         * Copies the chanced outputs of a Recipe and expands them for the number of parallel recipes performed
         *
         * @param newRecipe An instance of the recipe after the inputs and outputs have been multiplied from the number of parallels
         * @param oldRecipe The original recipe before any parallel multiplication
         * @param numberOfOperations The number of parallel operations that have been performed
         */
    protected static void copyChancedItemOutputs(RecipeBuilder<?> newRecipe, Recipe oldRecipe, int numberOfOperations) {

        // Iterate through the chanced outputs
        for(Recipe.ChanceEntry entry : oldRecipe.getChancedOutputs()) {

            int chance = entry.getChance();
            int boost = entry.getBoostPerTier();

            // Add individual chanced outputs per number of parallel operations performed, to mimic regular recipes.
            // This is done instead of simply batching the chanced outputs by the number of parallel operations performed
            IntStream.range(0, numberOfOperations).forEach(value -> {
                ItemStack itemStack = entry.getItemStack().copy();
                newRecipe.chancedOutput(itemStack, chance, boost);
            });
        }
    }

    /**
     * Copies all items in the input inventory into single oversized stacks per unique item.
     * Skips Empty slots
     *
     * @param inputs The inventory handler for the input inventory
     * @return a {@link HashMap} of {@link ItemStackKey}s comprising of oversized stacks for each unique item in the input inventory
     */
    protected static HashMap<ItemStackKey,Integer> findAllItemsInInputs(IItemHandlerModifiable inputs) {
        final Supplier<Map<ItemStackKey, Integer>> mapSupplier = Object2IntOpenHashMap::new;

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
     * Finds the maximum number of Recipes that can be performed at the same time based on the items in the item input inventory
     * @param countIngredients a {@link HashMap} of {@link ItemStackKey}s that is the result of calling {@link ParallelLogic#findAllItemsInInputs(IItemHandlerModifiable)}
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
     * Finds all unique Fluids in the combined Fluid Input inventory, and combines them into a {@link HashMap} of oversized {@link FluidKey}s
     * Skips Empty Fluid Tanks
     *
     * @param fluidInputs The combined fluid input inventory handler, in the form of an {@link IMultipleTankHandler}
     * @return a {@link Set} of unique {@link FluidKey}s for each fluid in the handler. Will be oversized stacks if required
     */
    protected static HashMap<FluidKey,Integer> findAllFluidsInInputs(IMultipleTankHandler fluidInputs) {
        final Supplier<Map<FluidKey, Integer>> mapSupplier = Object2IntOpenHashMap::new;

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
     * Finds the maximum number of a specific recipe that can be performed based upon the fluids in the fluid inputs
     *
     * @param countFluid a {@link Set} of {@link FluidStack}s that is the result of calling {@link ParallelLogic#findAllFluidsInInputs(IMultipleTankHandler)}
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

    protected static ItemStack copyItemStackWithCount(ItemStack itemStack, int count) {
        ItemStack itemCopy = itemStack.copy();
        itemCopy.setCount(count);
        return itemCopy;
    }

    protected static FluidStack copyFluidStackWithAmount(FluidStack fluidStack, int count) {
        FluidStack fluidCopy = fluidStack.copy();
        fluidCopy.amount = count;
        return fluidCopy;
    }

    protected static void multiplyInputsAndOutputs(List<CountableIngredient> newRecipeInputs,
                                            List<FluidStack> newFluidInputs,
                                            List<ItemStack> outputItems,
                                            List<FluidStack> outputFluids,
                                            Recipe recipe,
                                            int numberOfOperations) {

        recipe.getInputs().forEach(ci ->
                newRecipeInputs.add(new CountableIngredient(ci.getIngredient(),
                        ci.getCount() * numberOfOperations)));

        recipe.getFluidInputs().forEach(fluidStack ->
                newFluidInputs.add(new FluidStack(fluidStack.getFluid(),
                        fluidStack.amount * numberOfOperations)));

        recipe.getOutputs().forEach(itemStack ->
                outputItems.add(copyItemStackWithCount(itemStack,
                        itemStack.getCount() * numberOfOperations)));

        recipe.getFluidOutputs().forEach(fluidStack ->
                outputFluids.add(copyFluidStackWithAmount(fluidStack,
                        fluidStack.amount * numberOfOperations)));
    }
}
