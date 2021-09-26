package gregtech.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.*;
import java.util.stream.IntStream;

public class ParallelLogic {

    /**
     * Attempts to multiply the passed {@link Recipe} from the items and fluids in the input inventories, up to a maximum limit.
     * This is a rather strict implementation, and can create Recipes that will be too large for the available output inventory slots
     * or to be combined into the available space in the output inventory
     *
     * @param inputs The Item Input inventory handler
     * @param recipeMap The Recipe Map that the provided recipe is from
     * @param fluidInputs The Fluid Input inventory handler
     * @param recipe The Recipe to be multiplied
     * @param parallelAmount The hard limit on the amount of parallel Recipes that can be performed
     * @return A Recipe that has had all factors scaled by the number of parallel operations
     */
    protected Recipe multiplyRecipe(Recipe recipe, RecipeMap<?> recipeMap, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int parallelAmount) {

        if(parallelAmount == 1) {
            return recipe;
        }

        // Find all the items in the combined Item Input inventories and create oversized ItemStacks
        Set<ItemStack> ingredientStacks = findAllItemsInInputs(inputs);

        // Find all the fluids in the combined Fluid Input inventories and create oversized FluidStacks
        Set<FluidStack> fluidStacks = findAllFluidsInInputs(fluidInputs);

        // Find the maximum number of recipes that can be performed from the items in the item input inventories
        int itemMultiplier = getMinRatioItem(ingredientStacks, recipe, parallelAmount);
        // Find the maximum number of recipes that be be performed from the items in the fluid input inventories
        int fluidMultiplier = getMinRatioFluid(fluidStacks, recipe, parallelAmount);

        // Find the maximum number of recipes that can be performed from all available inputs
        int minMultiplier = Math.min(itemMultiplier, fluidMultiplier);

        // No fluids or items were found in the input inventories that match the recipe's inputs
        if(minMultiplier == Integer.MAX_VALUE) {
            return null;
        }

        // Create holders for the various parts of the new multiplied Recipe
        List<CountableIngredient> newRecipeInputs = new ArrayList<>();
        List<FluidStack> newFluidInputs = new ArrayList<>();
        List<ItemStack> outputItems = new ArrayList<>();
        List<FluidStack> outputFluids = new ArrayList<>();

        // Populate the various holders of the multiplied Recipe
        this.multiplyInputsAndOutputs(newRecipeInputs, newFluidInputs, outputItems, outputFluids, recipe, minMultiplier);

        // Build the new Recipe with multiplied components
        RecipeBuilder<?> newRecipe = recipeMap.recipeBuilder()
                .inputsIngredients(newRecipeInputs)
                .fluidInputs(newFluidInputs)
                .outputs(outputItems)
                .fluidOutputs(outputFluids)
                .EUt(recipe.getEUt())
                .duration(recipe.getDuration());

        // Add the chanced outputs to the multiplied recipe
        copyChancedItemOutputs(newRecipe, recipe, minMultiplier);

        // Return the multiplied Recipe
        return newRecipe.build().getResult();
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
     * @return a {@link Set} of {@link ItemStack}s comprising of oversized stacks for each unique item in the input inventory
     */
    protected static Set<ItemStack> findAllItemsInInputs(IItemHandlerModifiable inputs) {
        Set<ItemStack> countIngredients = new HashSet<>();

        // Iterate through the entire input inventory
        for(int slot = 0; slot < inputs.getSlots(); slot++) {
            ItemStack wholeItemStack = inputs.getStackInSlot(slot);

            // Skip empty slots
            if(wholeItemStack.isEmpty()) {
                continue;
            }

            // Populate the initially empty Set with an initial value
            if(countIngredients.isEmpty()) {
                countIngredients.add(wholeItemStack.copy());
            }
            else {
                // Iterate through the existing Set, attempting to match the item from the input inventory to an entry in the Set
                boolean found = false;
                for(ItemStack stack : countIngredients) {
                    if(ItemStack.areItemsEqual(stack, wholeItemStack)) {
                        // If a matching item was found, increment the count of the item in the Set
                        stack.setCount(stack.getCount() + wholeItemStack.getCount());
                        found = true;
                        break;
                    }
                }
                // If no matching ItemStack was found in the Set, add a new entry to the Set
                if(!found) {
                    countIngredients.add(wholeItemStack.copy());
                }
            }
        }
        return countIngredients;
    }

    /**
     * Finds the maximum number of Recipes that can be performed at the same time based on the items in the item input inventory
     * @param countIngredients a {@link Set} of {@link ItemStack}s that is the result of calling {@link ParallelLogic#findAllItemsInInputs(IItemHandlerModifiable)}
     * @param recipe The {@link Recipe} for which to find the maximum that can be ran simultaneously
     * @param parallelAmount The limit on the amount of recipes that can be performed at one time
     * @return The Maximum number of Recipes that can be performed at a single time based on the available Items
     */
    protected int getMinRatioItem(Set<ItemStack> countIngredients, Recipe recipe, int parallelAmount) {

        int minMultiplier = Integer.MAX_VALUE;

        // Iterate through the recipe inputs
        for(CountableIngredient recipeInputs : recipe.getInputs()) {

            // Skip not consumed inputs
            if(recipeInputs.getCount() == 0) {
                continue;
            }

            // For every stack in the ingredients gathered from the input bus. This is most likely going to be oversized stacks
            for(ItemStack wholeItemStack : countIngredients) {

                if(recipeInputs.getIngredient().apply(wholeItemStack)) {
                    //The ratio will either be set by the parallel limit, or the oversized stack divided by the amount of inputs the recipe takes
                    int ratio = Math.min(parallelAmount, wholeItemStack.getCount() / recipeInputs.getCount());
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
     * Finds all unique Fluids in the combined Fluid Input inventory, and combines them into a {@link Set} of oversized {@link FluidStack}s
     * Skips Empty Fluid Tanks
     *
     * @param fluidInputs The combined fluid input inventory handler, in the form of an {@link IMultipleTankHandler}
     * @return a {@link Set} of unique {@link FluidStack}s for each fluid in the handler. Will be oversized stacks if required
     */
    protected static Set<FluidStack> findAllFluidsInInputs(IMultipleTankHandler fluidInputs) {

        Set<FluidStack> combinedFluids = new HashSet<>();

        // Iterate through the different tanks that make up the structure
        for(IFluidTank tank : fluidInputs) {

            // Check if the tank contains a Fluid
            if(tank.getFluid() != null) {

                // Populate the set with an initial value on the first passthrough
                if(combinedFluids.isEmpty()) {
                    combinedFluids.add(new FluidStack(tank.getFluid(), tank.getFluidAmount()));
                }
                else {

                    // Create a FluidStack from the information provided from the tank
                    FluidStack tankFluid = new FluidStack(tank.getFluid(), tank.getFluidAmount());

                    boolean found = false;

                    // Iterate through the Set of FluidStacks, checking if the created FluidStack already exists
                    for(FluidStack fs : combinedFluids) {
                        if(fs.isFluidEqual(tankFluid)) {
                            // Increment the count of the existing FluidStack to create oversized stacks
                            fs.amount = fs.amount + tank.getFluidAmount();
                            found = true;
                            break;
                        }
                    }
                    // If a matching FluidStack was not found in the Set, add a new entry
                    if(!found) {
                        combinedFluids.add(tankFluid.copy());
                    }
                }
            }
        }

        return combinedFluids;
    }

    /**
     * Finds the maximum number of a specific recipe that can be performed based upon the fluids in the fluid inputs
     *
     * @param countFluid a {@link Set} of {@link FluidStack}s that is the result of calling {@link ParallelLogic#findAllFluidsInInputs(IMultipleTankHandler)}
     * @param recipe The {@link Recipe} for which to find the maximum that can be ran simultaneously
     * @param parallelAmount The limit on the amount of recipes that can be performed at one time
     * @return The Maximum number of Recipes that can be performed at a single time based on the available Fluids
     */
    protected int getMinRatioFluid(Set<FluidStack> countFluid, Recipe recipe, int parallelAmount) {

        int minMultiplier = Integer.MAX_VALUE;

        // Iterate through the fluid inputs in the recipe
        for(FluidStack fs : recipe.getFluidInputs()) {

            // Skip Not consumed Fluid inputs
            if(fs.amount == 0) {
                continue;
            }

            // Iterate through the fluids in the input hatches. This will likely be oversized stacks
            for(FluidStack inputStack : countFluid) {

                if(fs.isFluidEqual(inputStack)) {
                    //The ratio will either be set by the parallel limit, or the oversized stack divided by the amount of inputs the recipe takes
                    int ratio = Math.min(parallelAmount, inputStack.amount / fs.amount);

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

    protected void multiplyInputsAndOutputs(List<CountableIngredient> newRecipeInputs,
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
