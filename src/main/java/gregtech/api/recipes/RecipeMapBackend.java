package gregtech.api.recipes;

import crafttweaker.CraftTweakerAPI;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.recipes.map.*;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.CTRecipeHelper;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import gregtech.common.ConfigHolder;
import gregtech.integration.groovy.GroovyScriptCompat;
import gregtech.integration.groovy.VirtualizedRecipeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeMapBackend<R extends RecipeBuilder<R>> {

    private static final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> ingredientRoot = new WeakHashMap<>();

    private static boolean foundInvalidRecipe = false;

    private final String unlocalizedName;
    protected final RecipeBuilder<R> defaultRecipebuilder;
    private final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> fluidIngredientRoot = new WeakHashMap<>();
    private final Branch lookup = new Branch();

    private VirtualizedRecipeMap virtualizedRecipeMap;

    private boolean hasOreDictedInputs = false;
    private boolean hasNBTMatcherInputs = false;

    public RecipeMapBackend(@Nonnull String unlocalizedName, @Nonnull RecipeBuilder<R> defaultRecipebuilder) {
        this.unlocalizedName = unlocalizedName;
        this.defaultRecipebuilder = defaultRecipebuilder;
    }

    protected void setVirtualizedRecipeMap(@Nullable VirtualizedRecipeMap virtualizedRecipeMap) {
        this.virtualizedRecipeMap = virtualizedRecipeMap;
    }

    /**
     * Builds a list of unique ItemStacks from the given Collection of ItemStacks.
     * Used to reduce the number inputs, if for example there is more than one of the same input,
     * pack them into one.
     * This uses a strict comparison, so it will not pack the same item with different NBT tags,
     * to allow the presence of, for example, more than one configured circuit in the input.
     *
     * @param inputs The Collection of GTRecipeInputs.
     * @return an array of unique itemstacks.
     */
    @Nonnull
    public static ItemStack[] uniqueItems(@Nonnull Collection<ItemStack> inputs) {
        int index = 0;
        ItemStack[] uniqueItems = new ItemStack[inputs.size()];
        main:
        for (ItemStack input : inputs) {
            if (input.isEmpty()) {
                continue;
            }
            if (index > 0) {
                for (ItemStack unique : uniqueItems) {
                    if (unique == null) break;
                    else if (input.isItemEqual(unique) && ItemStack.areItemStackTagsEqual(input, unique)) {
                        continue main;
                    }
                }
            }
            uniqueItems[index++] = input;
        }
        if (index == uniqueItems.length) {
            return uniqueItems;
        }
        ItemStack[] retUniqueItems = new ItemStack[index];
        System.arraycopy(uniqueItems, 0, retUniqueItems, 0, index);
        return retUniqueItems;
    }

    /**
     * Builds a list of unique inputs from the given list GTRecipeInputs.
     * Used to reduce the number inputs, if for example there is more than one of the same input, pack them into one.
     *
     * @param inputs The list of GTRecipeInputs.
     * @return The list of unique inputs.
     */
    @Nonnull
    public static List<GTRecipeInput> uniqueIngredientsList(@Nonnull Collection<GTRecipeInput> inputs) {
        List<GTRecipeInput> list = new ObjectArrayList<>(inputs.size());
        for (GTRecipeInput item : inputs) {
            boolean isEqual = false;
            for (GTRecipeInput obj : list) {
                if (item.equalIgnoreAmount(obj)) {
                    isEqual = true;
                    break;
                }
            }
            if (isEqual) continue;
            if (item instanceof IntCircuitIngredient) {
                list.add(0, item);
            } else {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * Determine the correct root nodes for an ingredient
     *
     * @param ingredient the ingredient to check
     * @param branchMap  the branch containing the nodes
     * @return the correct nodes for the ingredient
     */
    @Nonnull
    protected static Map<AbstractMapIngredient, Either<Recipe, Branch>> determineRootNodes(@Nonnull AbstractMapIngredient ingredient,
                                                                                           @Nonnull Branch branchMap) {
        return ingredient.isSpecialIngredient() ? branchMap.getSpecialNodes() : branchMap.getNodes();
    }

    /**
     * Internal usage <strong>only</strong>. Only call from {@link RecipeMap#addRecipe(ValidationResult)}
     *
     * @param recipe the recipe to add
     */
    public void addRecipe(@Nonnull Recipe recipe) {
        if (recipe.isGroovyRecipe()) {
            this.virtualizedRecipeMap.addScripted(recipe);
        }
        compileRecipe(recipe);
    }

    public static void setFoundInvalidRecipe(boolean foundInvalidRecipe) {
        RecipeMapBackend.foundInvalidRecipe = RecipeMapBackend.foundInvalidRecipe || foundInvalidRecipe;
        OrePrefix currentOrePrefix = OrePrefix.getCurrentProcessingPrefix();
        if (currentOrePrefix != null) {
            Material currentMaterial = OrePrefix.getCurrentMaterial();
            GTLog.logger.error("Error happened during processing ore registration of prefix {} and material {}. " + "Seems like cross-mod compatibility issue. Report to GTCEu github.", currentOrePrefix, currentMaterial);
        }
    }

    public static boolean isFoundInvalidRecipe() {
        return foundInvalidRecipe;
    }

    /**
     * Compiles a recipe and adds it to the ingredient tree
     *
     * @param recipe the recipe to compile
     */
    public void compileRecipe(Recipe recipe) {
        if (recipe == null) return;
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe);
        recurseIngredientTreeAdd(recipe, items, lookup, 0, 0);
    }

    /**
     * @param recipe the recipe to remove
     * @return if removal was successful
     */
    public boolean removeRecipe(@Nonnull Recipe recipe) {
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe);
        if (recurseIngredientTreeRemove(recipe, items, lookup, 0) != null) {
            if (GroovyScriptCompat.isCurrentlyRunning()) {
                this.virtualizedRecipeMap.addBackup(recipe);
            }
            return true;
        }
        return false;
    }

    /**
     * Finds a Recipe matching the Fluid and/or ItemStack Inputs.
     *
     * @param voltage     Voltage of the Machine or Long.MAX_VALUE if it has no Voltage
     * @param inputs      the Item Inputs
     * @param fluidInputs the Fluid Inputs
     * @return the Recipe it has found or null for no matching Recipe
     */
    @Nullable
    public Recipe findRecipe(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
        return this.findRecipe(voltage, GTUtility.itemHandlerToList(inputs), GTUtility.fluidHandlerToList(fluidInputs));
    }

    /**
     * Finds a Recipe matching the Fluid and/or ItemStack Inputs.
     *
     * @param voltage     Voltage of the Machine or Long.MAX_VALUE if it has no Voltage
     * @param inputs      the Item Inputs
     * @param fluidInputs the Fluid Inputs
     * @return the Recipe it has found or null for no matching Recipe
     */
    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
        return findRecipe(voltage, inputs, fluidInputs, false);
    }

    /**
     * Finds a Recipe matching the Fluid and/or ItemStack Inputs.
     *
     * @param voltage      Voltage of the Machine or Long.MAX_VALUE if it has no Voltage
     * @param inputs       the Item Inputs
     * @param fluidInputs  the Fluid Inputs
     * @param exactVoltage should require exact voltage matching on recipe. used by craftweaker
     * @return the Recipe it has found or null for no matching Recipe
     */
    @Nullable
    public Recipe findRecipe(long voltage, @Nonnull final List<ItemStack> inputs, @Nonnull final List<FluidStack> fluidInputs, boolean exactVoltage) {
        final List<ItemStack> items = inputs.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
        final List<FluidStack> fluids = fluidInputs.stream().filter(f -> f != null && f.amount != 0).collect(Collectors.toList());

        return find(items, fluids, recipe -> {
            if (exactVoltage && recipe.getEUt() != voltage) {
                // if exact voltage is required, the recipe is not considered valid
                return false;
            }
            if (recipe.getEUt() > voltage) {
                // there is not enough voltage to consider the recipe valid
                return false;
            }
            return recipe.matches(false, inputs, fluidInputs);
        });
    }

    /**
     * Prepares Items and Fluids for use in recipe search
     *
     * @param items  the items to prepare
     * @param fluids the fluids to prepare
     * @return a List of Lists of AbstractMapIngredients used for finding recipes
     */
    @Nullable
    protected List<List<AbstractMapIngredient>> prepareRecipeFind(@Nonnull Collection<ItemStack> items, @Nonnull Collection<FluidStack> fluids) {
        // First, check if items and fluids are valid.
        if (items.size() == Integer.MAX_VALUE || fluids.size() == Integer.MAX_VALUE) {
            return null;
        }
        if (items.isEmpty() && fluids.isEmpty()) {
            return null;
        }

        // Build input.
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(items.size() + fluids.size());
        if (!items.isEmpty()) buildFromItemStacks(list, uniqueItems(items));
        if (!fluids.isEmpty()) buildFromFluidStacks(list, fluids);

        // nothing was added, so return nothing
        if (list.isEmpty()) return null;
        return list;
    }

    /**
     * Finds a recipe using Items and Fluids.
     *
     * @param items     a collection of items
     * @param fluids    a collection of fluids
     * @param canHandle a predicate for determining if a recipe is valid
     * @return the recipe found
     */
    @Nullable
    public Recipe find(@Nonnull Collection<ItemStack> items, @Nonnull Collection<FluidStack> fluids, @Nonnull Predicate<Recipe> canHandle) {
        List<List<AbstractMapIngredient>> list = prepareRecipeFind(items, fluids);
        // couldn't build any inputs to use for search, so no recipe could be found
        if (list == null) return null;
        return recurseIngredientTreeFindRecipe(list, lookup, canHandle);
    }

    /**
     * Recursively finds a recipe, top level.
     *
     * @param ingredients the ingredients part
     * @param branchRoot  the root branch to search from.
     * @param canHandle   if the found recipe is valid
     * @return a recipe
     */
    @Nullable
    private Recipe recurseIngredientTreeFindRecipe(@Nonnull List<List<AbstractMapIngredient>> ingredients,
                                                   @Nonnull Branch branchRoot, @Nonnull Predicate<Recipe> canHandle) {
        // Try each ingredient as a starting point, adding it to the skip-list.
        // The skip-list is a packed long, where each 1 bit represents an index to skip
        for (int i = 0; i < ingredients.size(); i++) {
            Recipe r = recurseIngredientTreeFindRecipe(ingredients, branchRoot, canHandle, i, 0, (1L << i));
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    /**
     * Recursively finds a recipe
     *
     * @param ingredients the ingredients part
     * @param branchMap   the current branch of the tree
     * @param canHandle   predicate to test found recipe.
     * @param index       the index of the wrapper to get
     * @param count       how deep we are in recursion, < ingredients.length
     * @param skip        bitmap of ingredients to skip, i.e. which ingredients are already used in the recursion.
     * @return a recipe
     */
    @Nullable
    private Recipe recurseIngredientTreeFindRecipe(@Nonnull List<List<AbstractMapIngredient>> ingredients,
                                                   @Nonnull Branch branchMap, @Nonnull Predicate<Recipe> canHandle,
                                                   int index, int count, long skip) {
        // exhausted all the ingredients, and didn't find anything
        if (count == ingredients.size()) return null;

        // Iterate over current level of nodes.
        for (AbstractMapIngredient obj : ingredients.get(index)) {
            // determine the root nodes
            Map<AbstractMapIngredient, Either<Recipe, Branch>> targetMap = determineRootNodes(obj, branchMap);

            Either<Recipe, Branch> result = targetMap.get(obj);
            if (result != null) {
                // if there is a recipe (left mapping), return it immediately as found, if it can be handled
                // Otherwise, recurse and go to the next branch.
                Recipe r = result.map(potentialRecipe -> canHandle.test(potentialRecipe) ? potentialRecipe : null,
                        potentialBranch -> diveIngredientTreeFindRecipe(ingredients, potentialBranch, canHandle, index, count, skip));
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * Recursively finds a recipe
     *
     * @param ingredients  the ingredients part
     * @param map          the current branch of the tree
     * @param canHandle    predicate to test found recipe.
     * @param currentIndex the index of the wrapper to get
     * @param count        how deep we are in recursion, < ingredients.length
     * @param skip         bitmap of ingredients to skip, i.e. which ingredients are already used in the recursion.
     * @return a recipe
     */
    @Nullable
    private Recipe diveIngredientTreeFindRecipe(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch map,
                                                @Nonnull Predicate<Recipe> canHandle, int currentIndex, int count, long skip) {
        // We loop around ingredients.size() if we reach the end.
        // only end when all ingredients are exhausted, or a recipe is found
        int i = (currentIndex + 1) % ingredients.size();
        while (i != currentIndex) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << i)) == 0)) {
                // Recursive call
                // Increase the count, so the recursion can terminate if needed (ingredients is exhausted)
                // Append the current index to the skip list
                Recipe found = recurseIngredientTreeFindRecipe(ingredients, map, canHandle, i, count + 1, skip | (1L << i));
                if (found != null) {
                    return found;
                }
            }
            // increment the index if the current index is skipped, or the recipe is not found
            i = (i + 1) % ingredients.size();
        }
        return null;
    }

    /**
     * Exhaustively gathers all recipes that can be crafted with the given ingredients, into a Set.
     *
     * @param items  the ingredients, in the form of a List of ItemStack. Usually the inputs of a Recipe
     * @param fluids the ingredients, in the form of a List of FluidStack. Usually the inputs of a Recipe
     * @return a Set of recipes that can be crafted with the given ingredients
     */
    @Nullable
    public Set<Recipe> findRecipeCollisions(Collection<ItemStack> items, Collection<FluidStack> fluids) {
        List<List<AbstractMapIngredient>> list = prepareRecipeFind(items, fluids);
        if (list == null) return null;
        Set<Recipe> collidingRecipes = new ObjectOpenHashSet<>();
        recurseIngredientTreeFindRecipeCollisions(list, lookup, collidingRecipes);
        return collidingRecipes;
    }

    /**
     * @param ingredients      the ingredients to search with
     * @param branchRoot       the root branch to start searching from
     * @param collidingRecipes the list to store recipe collisions
     */
    private void recurseIngredientTreeFindRecipeCollisions(@Nonnull List<List<AbstractMapIngredient>> ingredients,
                                                           @Nonnull Branch branchRoot, @Nonnull Set<Recipe> collidingRecipes) {
        // Try each ingredient as a starting point, adding it to the skip-list.
        // The skip-list is a packed long, where each 1 bit represents an index to skip
        for (int i = 0; i < ingredients.size(); i++) {
            recurseIngredientTreeFindRecipeCollisions(ingredients, branchRoot, i, 0, (1L << i), collidingRecipes);
        }
    }

    /**
     * Recursively finds all colliding recipes
     *
     * @param ingredients      the ingredients part
     * @param branchMap        the current branch of the tree
     * @param index            the index of the wrapper to get
     * @param count            how deep we are in recursion, < ingredients.length
     * @param skip             bitmap of ingredients to skip, i.e. which ingredients are already used in the recursion.
     * @param collidingRecipes the set to store the recipes in
     */
    @Nullable
    private Recipe recurseIngredientTreeFindRecipeCollisions(@Nonnull List<List<AbstractMapIngredient>> ingredients,
                                                             @Nonnull Branch branchMap, int index, int count, long skip,
                                                             @Nonnull Set<Recipe> collidingRecipes) {
        // exhausted all the ingredients, and didn't find anything
        if (count == ingredients.size()) return null;

        List<AbstractMapIngredient> wr = ingredients.get(index);
        // Iterate over current level of nodes.
        for (AbstractMapIngredient obj : wr) {
            // determine the root nodes
            Map<AbstractMapIngredient, Either<Recipe, Branch>> targetMap = determineRootNodes(obj, branchMap);

            Either<Recipe, Branch> result = targetMap.get(obj);
            if (result != null) {
                // if there is a recipe (left mapping), return it immediately as found
                // Otherwise, recurse and go to the next branch.
                Recipe r = result.map(recipe -> recipe,
                        right -> diveIngredientTreeFindRecipeCollisions(ingredients, right, index, count, skip, collidingRecipes));
                if (r != null) {
                    collidingRecipes.add(r);
                }
            }
        }
        return null;
    }

    /**
     * Recursively finds a recipe
     *
     * @param ingredients      the ingredients part
     * @param map              the current branch of the tree
     * @param currentIndex     the index of the wrapper to get
     * @param count            how deep we are in recursion, < ingredients.length
     * @param skip             bitmap of ingredients to skip, i.e. which ingredients are already used in the recursion.
     * @param collidingRecipes the set to store the recipes in
     * @return a recipe
     */
    @Nullable
    private Recipe diveIngredientTreeFindRecipeCollisions(@Nonnull List<List<AbstractMapIngredient>> ingredients,
                                                          @Nonnull Branch map, int currentIndex, int count, long skip, @Nonnull Set<Recipe> collidingRecipes) {
        // We loop around ingredients.size() if we reach the end.
        // only end when all ingredients are exhausted, or a recipe is found
        int i = (currentIndex + 1) % ingredients.size();
        while (i != currentIndex) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << i)) == 0)) {
                // Recursive call
                // Increase the count, so the recursion can terminate if needed (ingredients is exhausted)
                // Append the current index to the skip list
                Recipe r = recurseIngredientTreeFindRecipeCollisions(ingredients, map, i, count + 1, skip | (1L << i), collidingRecipes);
                if (r != null) {
                    return r;
                }
            }
            // increment the index if the current index is skipped, or the recipe is not found
            i = (i + 1) % ingredients.size();
        }
        return null;
    }

    /**
     * Adds a recipe to the map. (recursive part)
     *
     * @param recipe      the recipe to add.
     * @param ingredients list of input ingredients representing the recipe.
     * @param branchMap   the current branch in the recursion.
     * @param index       where in the ingredients list we are.
     * @param count       how many branches were added already.
     */
    private boolean recurseIngredientTreeAdd(@Nonnull Recipe recipe, @Nonnull List<List<AbstractMapIngredient>> ingredients,
                                             @Nonnull Branch branchMap, int index, int count) {
        if (count >= ingredients.size()) return true;
        if (index >= ingredients.size()) {
            throw new RuntimeException("Index out of bounds for recurseItemTreeAdd, should not happen");
        }
        // Loop through NUMBER_OF_INGREDIENTS times.

        // the current contents to be added to a node in the branch
        final List<AbstractMapIngredient> current = ingredients.get(index);
        final Branch branchRight = new Branch();
        Either<Recipe, Branch> r;

        // for every ingredient, add it to a node
        for (AbstractMapIngredient obj : current) {
            // determine the root nodes
            Map<AbstractMapIngredient, Either<Recipe, Branch>> targetMap = determineRootNodes(obj, branchMap);

            // Either add the recipe or create a branch.
            r = targetMap.compute(obj, (k, v) -> {
                if (count == ingredients.size() - 1) {
                    // handle very last ingredient
                    if (v != null) {
                        // handle the existing branch
                        if (!v.left().isPresent() || v.left().get() != recipe) {
                            // the recipe already there was not the one being added, so there is a conflict
                            if (recipe.getIsCTRecipe()) {
                                CraftTweakerAPI.logError(String.format("Recipe duplicate or conflict found in RecipeMap %s and was not added. See next lines for details.", this.unlocalizedName));

                                CraftTweakerAPI.logError(String.format("Attempted to add Recipe: %s", CTRecipeHelper.getRecipeAddLine(this.unlocalizedName, recipe)));

                                if (v.left().isPresent()) {
                                    CraftTweakerAPI.logError(String.format("Which conflicts with: %s", CTRecipeHelper.getRecipeAddLine(this.unlocalizedName, v.left().get())));
                                } else {
                                    CraftTweakerAPI.logError("Could not identify exact duplicate/conflict.");
                                }
                            }
                            if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                                GTLog.logger.warn("Recipe duplicate or conflict found in RecipeMap {} and was not added. See next lines for details", this.unlocalizedName);

                                GTLog.logger.warn("Attempted to add Recipe: {}", recipe.toString());

                                if (v.left().isPresent()) {
                                    GTLog.logger.warn("Which conflicts with: {}", v.left().get().toString());
                                } else {
                                    GTLog.logger.warn("Could not find exact duplicate/conflict.");
                                }
                            }
                        }
                        // Return the existing recipe, even on conflicts.
                        // If there was no conflict but a recipe was still present, it was added on an earlier recurse,
                        // and this will carry the result further back in the call stack
                        return v;
                    } else {
                        // nothing exists for this path, so end with the recipe
                        return Either.left(recipe);
                    }
                } else if (v == null) {
                    // no existing ingredient is present, so use the new one
                    return Either.right(branchRight);
                }
                // there is an existing ingredient here already, so use it
                return v;
            });

            // left branches are always either empty or contain recipes.
            // If there's a recipe present, the addition is finished
            if (r.left().isPresent()) return true;

            // recursive part: apply the addition for the next ingredient in the list, for the right branch.
            // the right branch only contains ingredients, or is empty when the left branch is present
            boolean addedNextBranch = r.right()
                    .filter(m -> recurseIngredientTreeAdd(recipe, ingredients, m, (index + 1) % ingredients.size(), count + 1))
                    .isPresent();

            if (!addedNextBranch) {
                // failed to add the next branch, so undo any made changes
                if (count == ingredients.size() - 1) {
                    // was the final ingredient, so the mapping of it to a recipe needs to be removed
                    targetMap.remove(obj);
                } else {
                    // was a regular ingredient
                    if (targetMap.get(obj).right().isPresent()) {
                        // if something was put into the map
                        if (targetMap.get(obj).right().get().isEmptyBranch()) {
                            // if what was put was empty (invalid), remove it
                            targetMap.remove(obj);
                        }
                    }
                }
                // because a branch addition failure happened, fail the recipe addition for this step
                return false;
            }
        }
        // recipe addition was successful
        return true;
    }

    /**
     * Converts a list of {@link GTRecipeInput}s for Fluids into a List of {@link AbstractMapIngredient}s.
     * Do not supply GTRecipeInputs dealing with any other type of input other than Fluids.
     *
     * @param list        the list of MapIngredients to add to
     * @param fluidInputs the GTRecipeInputs to convert
     */
    protected void buildFromRecipeFluids(@Nonnull List<List<AbstractMapIngredient>> list, @Nonnull List<GTRecipeInput> fluidInputs) {
        for (GTRecipeInput fluidInput : fluidInputs) {
            AbstractMapIngredient ingredient = new MapFluidIngredient(fluidInput);
            retrieveCachedIngredient(list, ingredient, fluidIngredientRoot);
        }
    }

    /**
     * Retrieves a cached ingredient, or inserts a default one
     *
     * @param list the list to append to
     * @param defaultIngredient the ingredient to use as a default value, if not cached
     * @param cache the ingredient root to retrieve from
     */
    protected static void retrieveCachedIngredient(@Nonnull List<List<AbstractMapIngredient>> list, @Nonnull AbstractMapIngredient defaultIngredient,
                                                   @Nonnull WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> cache) {
        WeakReference<AbstractMapIngredient> cached = cache.get(defaultIngredient);
        if (cached != null && cached.get() != null) {
            list.add(Collections.singletonList(cached.get()));
        } else {
            cache.put(defaultIngredient, new WeakReference<>(defaultIngredient));
            list.add(Collections.singletonList(defaultIngredient));
        }
    }

    /**
     * Populates a list of MapIngredients from a list of FluidStacks
     *
     * @param list        the list to populate
     * @param ingredients the ingredients to convert
     */
    protected static void buildFromFluidStacks(@Nonnull List<List<AbstractMapIngredient>> list, @Nonnull Iterable<FluidStack> ingredients) {
        for (FluidStack t : ingredients) {
            list.add(Collections.singletonList(new MapFluidIngredient(t)));
        }
    }

    /**
     * Converts a Recipe's {@link GTRecipeInput}s into a List of {@link AbstractMapIngredient}s
     *
     * @param r the recipe to use
     * @return a list of all the AbstractMapIngredients comprising the recipe
     */
    @Nonnull
    protected List<List<AbstractMapIngredient>> fromRecipe(@Nonnull Recipe r) {
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>((r.getInputs().size()) + r.getFluidInputs().size());
        if (!r.getInputs().isEmpty()) {
            buildFromRecipeItems(list, uniqueIngredientsList(r.getInputs()));
        }
        if (!r.getFluidInputs().isEmpty()) {
            buildFromRecipeFluids(list, r.getFluidInputs());
        }
        return list;
    }

    /**
     * Converts a list of {@link GTRecipeInput}s for Items into a List of {@link AbstractMapIngredient}s.
     * Do not supply GTRecipeInputs dealing with any other type of input other than Items.
     *
     * @param list   the list of MapIngredients to add to
     * @param inputs the GTRecipeInputs to convert
     */
    protected void buildFromRecipeItems(List<List<AbstractMapIngredient>> list, @Nonnull List<GTRecipeInput> inputs) {
        for (GTRecipeInput r : inputs) {
            if (r.isOreDict()) {
                AbstractMapIngredient ingredient;
                this.hasOreDictedInputs = true;
                if (r.hasNBTMatchingCondition()) {
                    hasNBTMatcherInputs = true;
                    ingredient = new MapOreDictNBTIngredient(r.getOreDict(), r.getNBTMatcher(), r.getNBTMatchingCondition());
                } else {
                    ingredient = new MapOreDictIngredient(r.getOreDict());
                }

                // use the cached ingredient, if possible
                retrieveCachedIngredient(list, ingredient, ingredientRoot);
            } else {
                // input must be represented as a list of possible stacks
                List<AbstractMapIngredient> ingredients;
                if (r.hasNBTMatchingCondition()) {
                    ingredients = MapItemStackNBTIngredient.from(r);
                    hasNBTMatcherInputs = true;
                } else {
                    ingredients = MapItemStackIngredient.from(r);
                }

                for (int i = 0; i < ingredients.size(); i++) {
                    AbstractMapIngredient mappedIngredient = ingredients.get(i);
                    // attempt to use the cached value if possible, otherwise cache for the next time
                    WeakReference<AbstractMapIngredient> cached = ingredientRoot.get(mappedIngredient);
                    if (cached != null && cached.get() != null) {
                        ingredients.set(i, cached.get());
                    } else {
                        ingredientRoot.put(mappedIngredient, new WeakReference<>(mappedIngredient));
                    }
                }
                list.add(ingredients);
            }
        }
    }

    /**
     * Populates a list of MapIngredients from a list of ItemStacks
     *
     * @param list        the list to populate
     * @param ingredients the ingredients to convert
     */
    protected final void buildFromItemStacks(@Nonnull List<List<AbstractMapIngredient>> list, @Nonnull ItemStack[] ingredients) {
        AbstractMapIngredient ingredient;
        for (ItemStack stack : ingredients) {
            int meta = stack.getMetadata();
            NBTTagCompound nbt = stack.getTagCompound();

            List<AbstractMapIngredient> ls = new ObjectArrayList<>(1);

            // add the regular input
            ls.add(new MapItemStackIngredient(stack, meta, nbt));

            if (hasOreDictedInputs) {

                // add the ore dict inputs
                for (int i : OreDictionary.getOreIDs(stack)) {
                    ingredient = new MapOreDictIngredient(i);
                    ls.add(ingredient);

                    if (hasNBTMatcherInputs) {
                        // add the nbt inputs for the oredict inputs
                        ingredient = new MapOreDictNBTIngredient(i, nbt);
                        ls.add(ingredient);
                    }
                }
            }
            if (hasNBTMatcherInputs) {
                // add the nbt input for the regular input
                ls.add(new MapItemStackNBTIngredient(stack, meta, nbt));
            }
            if (!ls.isEmpty()) list.add(ls);
        }
    }

    /**
     * Removes a recipe from the map. (recursive part)
     *
     * @param recipeToRemove the recipe to add.
     * @param ingredients    list of input ingredients.
     * @param branchMap      the current branch in the recursion.
     */
    @Nullable
    private Recipe recurseIngredientTreeRemove(@Nonnull Recipe recipeToRemove, @Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchMap, int depth) {
        // for every ingredient
        for (List<AbstractMapIngredient> current : ingredients) {
            // for all possibilities as keys
            for (AbstractMapIngredient obj : current) {
                // determine the root nodes
                Map<AbstractMapIngredient, Either<Recipe, Branch>> targetMap = determineRootNodes(obj, branchMap);

                // recursive part:
                Recipe found = null;
                Either<Recipe, Branch> result = targetMap.get(obj);
                if (result != null) {
                    // if there is a recipe (left mapping), return it immediately as found
                    // otherwise, recurse and go to the next branch. Do so by omitting the current ingredient.
                    Recipe r = result.map(potentialRecipe -> potentialRecipe,
                            potentialBranch -> recurseIngredientTreeRemove(recipeToRemove, ingredients.subList(1, ingredients.size()), potentialBranch, depth + 1));
                    if (r == recipeToRemove) {
                        found = r;
                    } else {
                        // wasn't the correct recipe
                        if (recipeToRemove.getIsCTRecipe()) {
                            CraftTweakerAPI.logError(String.format("Failed to remove Recipe from RecipeMap %s: %s",
                                    this.unlocalizedName, CTRecipeHelper.getRecipeRemoveLine(this.unlocalizedName, recipeToRemove)));
                        }
                        if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                            GTLog.logger.warn("Failed to remove recipe from RecipeMap {}. See next lines for details", this.unlocalizedName);
                            GTLog.logger.warn("Failed to remove Recipe: {}", recipeToRemove.toString());
                        }
                    }
                }

                if (found != null) {
                    if (ingredients.size() == 1) {
                        // a recipe was found, and this is the only ingredient, so remove it directly
                        targetMap.remove(obj);
                    } else {
                        if (targetMap.get(obj).right().isPresent()) {
                            Branch branch = targetMap.get(obj).right().get();
                            if (branch.isEmptyBranch()) {
                                // have a branch at this stage, so remove the ingredient for this step
                                targetMap.remove(obj);
                            }
                        }
                    }
                    // return the successfully removed recipe
                    return found;
                }
            }
        }
        // could not remove the recipe
        return null;
    }

    /**
     * Gets all the recipes. Do not use for lookup purposes.
     *
     * @param filterHidden if hidden recipes should be filtered out
     * @return all the recipes contained
     */
    @Nonnull
    public Stream<Recipe> getRecipes(boolean filterHidden) {
        return lookup.getRecipes(filterHidden);
    }

    @Override
    public String toString() {
        return "RecipeMapBackend{unlocalizedName='" + unlocalizedName + "'}";
    }

    @FunctionalInterface
    public interface BackendCreator<R extends RecipeBuilder<R>> {

        /**
         * @see RecipeMapBackend#RecipeMapBackend(String, RecipeBuilder)
         * @return a new RecipeMapBackend
         */
        @Nonnull
        RecipeMapBackend<R> apply(@Nonnull String unlocalizedName, @Nonnull RecipeBuilder<R> defaultRecipebuilder);
    }
}
