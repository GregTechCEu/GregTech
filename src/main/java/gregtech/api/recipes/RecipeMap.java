package gregtech.api.recipes;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.recipes.map.*;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.*;
import gregtech.common.ConfigHolder;
import gregtech.integration.crafttweaker.CTRecipeHelper;
import gregtech.integration.crafttweaker.recipe.CTRecipe;
import gregtech.integration.crafttweaker.recipe.CTRecipeBuilder;
import gregtech.integration.groovy.GroovyScriptModule;
import gregtech.integration.groovy.VirtualizedRecipeMap;
import gregtech.modules.GregTechModules;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.google.common.collect.ImmutableList;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import stanhebben.zenscript.annotations.*;
import stanhebben.zenscript.annotations.Optional;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ZenClass("mods.gregtech.recipe.RecipeMap")
@ZenRegister
public class RecipeMap<R extends RecipeBuilder<R>> {

    private static final Map<String, RecipeMap<?>> RECIPE_MAP_REGISTRY = new Object2ReferenceOpenHashMap<>();

    private static final Comparator<Recipe> RECIPE_DURATION_THEN_EU = Comparator.comparingInt(Recipe::getDuration)
            .thenComparingInt(Recipe::getEUt)
            .thenComparing(Recipe::hashCode);

    private static boolean foundInvalidRecipe = false;

    public static final ChanceBoostFunction DEFAULT_CHANCE_FUNCTION = ChanceBoostFunction.OVERCLOCK;

    public ChanceBoostFunction chanceFunction = DEFAULT_CHANCE_FUNCTION;

    public final String unlocalizedName;

    private final R recipeBuilderSample;
    private int maxInputs;
    private int maxOutputs;
    private int maxFluidInputs;
    private int maxFluidOutputs;
    private final boolean modifyItemInputs;
    private final boolean modifyItemOutputs;
    private final boolean modifyFluidInputs;
    private final boolean modifyFluidOutputs;
    protected final Byte2ObjectMap<TextureArea> slotOverlays;
    protected TextureArea specialTexture;
    protected int[] specialTexturePosition;
    protected TextureArea progressBarTexture;
    protected MoveType moveType;
    public final boolean isHidden;

    private boolean allowEmptyOutput;

    private final Object grsVirtualizedRecipeMap;
    private final Branch lookup = new Branch();
    private boolean hasOreDictedInputs = false;
    private boolean hasNBTMatcherInputs = false;
    private static final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> ingredientRoot = new WeakHashMap<>();
    private final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> fluidIngredientRoot = new WeakHashMap<>();

    private final Map<GTRecipeCategory, List<Recipe>> recipeByCategory = new Object2ObjectOpenHashMap<>();

    private Consumer<R> onRecipeBuildAction;
    protected SoundEvent sound;
    private RecipeMap<?> smallRecipeMap;

    /**
     * Create and register new instance of RecipeMap with specified properties. All
     * maximum I/O size for item and fluids will be able to be modified.
     *
     * @param unlocalizedName      the unlocalized name for the RecipeMap
     * @param maxInputs            the maximum item inputs
     * @param maxOutputs           the maximum item outputs
     * @param maxFluidInputs       the maximum fluid inputs
     * @param maxFluidOutputs      the maximum fluid outputs
     * @param defaultRecipeBuilder the default RecipeBuilder for the RecipeMap
     * @param isHidden             if the RecipeMap should have a category in JEI
     */
    public RecipeMap(@NotNull String unlocalizedName,
                     int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs,
                     @NotNull R defaultRecipeBuilder,
                     boolean isHidden) {
        this(unlocalizedName,
                maxInputs, true, maxOutputs, true,
                maxFluidInputs, true, maxFluidOutputs, true,
                defaultRecipeBuilder, isHidden);
    }

    /**
     * Create and register new instance of RecipeMap with specified properties.
     *
     * @param unlocalizedName      the unlocalized name for the RecipeMap
     * @param maxInputs            the maximum item inputs
     * @param modifyItemInputs     if modification of the maximum item input is permitted
     * @param maxOutputs           the maximum item outputs
     * @param modifyItemOutputs    if modification of the maximum item output is permitted
     * @param maxFluidInputs       the maximum fluid inputs
     * @param modifyFluidInputs    if modification of the maximum fluid input is permitted
     * @param maxFluidOutputs      the maximum fluid outputs
     * @param modifyFluidOutputs   if modification of the maximum fluid output is permitted
     * @param defaultRecipeBuilder the default RecipeBuilder for the RecipeMap
     * @param isHidden             if the RecipeMap should have a category in JEI
     */
    public RecipeMap(@NotNull String unlocalizedName,
                     int maxInputs, boolean modifyItemInputs,
                     int maxOutputs, boolean modifyItemOutputs,
                     int maxFluidInputs, boolean modifyFluidInputs,
                     int maxFluidOutputs, boolean modifyFluidOutputs,
                     @NotNull R defaultRecipeBuilder,
                     boolean isHidden) {
        this.unlocalizedName = unlocalizedName;
        this.slotOverlays = new Byte2ObjectOpenHashMap<>();
        this.progressBarTexture = GuiTextures.PROGRESS_BAR_ARROW;
        this.moveType = MoveType.HORIZONTAL;

        this.maxInputs = maxInputs;
        this.maxFluidInputs = maxFluidInputs;
        this.maxOutputs = maxOutputs;
        this.maxFluidOutputs = maxFluidOutputs;

        this.modifyItemInputs = modifyItemInputs;
        this.modifyItemOutputs = modifyItemOutputs;
        this.modifyFluidInputs = modifyFluidInputs;
        this.modifyFluidOutputs = modifyFluidOutputs;

        this.isHidden = isHidden;
        defaultRecipeBuilder.setRecipeMap(this);
        defaultRecipeBuilder
                .category(GTRecipeCategory.create(GTValues.MODID, unlocalizedName, getTranslationKey(), this));
        this.recipeBuilderSample = defaultRecipeBuilder;
        RECIPE_MAP_REGISTRY.put(unlocalizedName, this);

        this.grsVirtualizedRecipeMap = GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_GRS) ?
                new VirtualizedRecipeMap(this) : null;
    }

    @ZenMethod
    public static List<RecipeMap<?>> getRecipeMaps() {
        return ImmutableList.copyOf(RECIPE_MAP_REGISTRY.values());
    }

    @ZenMethod
    public static RecipeMap<?> getByName(String unlocalizedName) {
        return RECIPE_MAP_REGISTRY.get(unlocalizedName);
    }

    @ZenMethod
    public ChanceBoostFunction getChanceFunction() {
        return chanceFunction;
    }

    public static boolean isFoundInvalidRecipe() {
        return foundInvalidRecipe;
    }

    public static void setFoundInvalidRecipe(boolean foundInvalidRecipe) {
        RecipeMap.foundInvalidRecipe |= foundInvalidRecipe;
        OrePrefix currentOrePrefix = OrePrefix.getCurrentProcessingPrefix();
        if (currentOrePrefix != null) {
            Material currentMaterial = OrePrefix.getCurrentMaterial();
            GTLog.logger.error(
                    "Error happened during processing ore registration of prefix {} and material {}. " +
                            "Seems like cross-mod compatibility issue. Report to GTCEu github.",
                    currentOrePrefix, currentMaterial);
        }
    }

    public RecipeMap<R> setProgressBar(TextureArea progressBar, MoveType moveType) {
        this.progressBarTexture = progressBar;
        this.moveType = moveType;
        return this;
    }

    public RecipeMap<R> setSlotOverlay(boolean isOutput, boolean isFluid, TextureArea slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true,
                slotOverlay);
    }

    public RecipeMap<R> setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, TextureArea slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public RecipeMap<R> setSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    public RecipeMap<R> setChanceFunction(@NotNull ChanceBoostFunction function) {
        chanceFunction = function;
        return this;
    }

    public RecipeMap<R> onRecipeBuild(Consumer<R> consumer) {
        onRecipeBuildAction = consumer;
        return this;
    }

    public RecipeMap<R> allowEmptyOutput() {
        this.allowEmptyOutput = true;
        return this;
    }

    public RecipeMap<R> setSmallRecipeMap(RecipeMap<?> recipeMap) {
        this.smallRecipeMap = recipeMap;
        return this;
    }

    public RecipeMap<?> getSmallRecipeMap() {
        return smallRecipeMap;
    }

    /**
     * Internal usage <strong>only</strong>, use {@link RecipeBuilder#buildAndRegister()}
     *
     * @param validationResult the validation result from building the recipe
     * @return if adding the recipe was successful
     */
    public boolean addRecipe(@NotNull ValidationResult<Recipe> validationResult) {
        validationResult = postValidateRecipe(validationResult);
        switch (validationResult.getType()) {
            case SKIP -> {
                return false;
            }
            case INVALID -> {
                setFoundInvalidRecipe(true);
                return false;
            }
        }
        Recipe recipe = validationResult.getResult();

        if (recipe.isGroovyRecipe()) {
            this.getGroovyScriptRecipeMap().addScripted(recipe);
        }
        return compileRecipe(recipe);
    }

    /**
     * Compiles a recipe and adds it to the ingredient tree
     *
     * @param recipe the recipe to compile
     * @return if the recipe was successfully compiled
     */
    public boolean compileRecipe(Recipe recipe) {
        if (recipe == null) {
            return false;
        }
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe);
        if (recurseIngredientTreeAdd(recipe, items, lookup, 0, 0)) {
            recipeByCategory.compute(recipe.getRecipeCategory(), (k, v) -> {
                if (v == null) v = new ArrayList<>();
                v.add(recipe);
                return v;
            });
            return true;
        }
        return false;
    }

    /**
     * @param recipe the recipe to remove
     * @return if removal was successful
     */
    public boolean removeRecipe(@NotNull Recipe recipe) {
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe);
        if (recurseIngredientTreeRemove(recipe, items, lookup, 0) != null) {
            if (GroovyScriptModule.isCurrentlyRunning()) {
                this.getGroovyScriptRecipeMap().addBackup(recipe);
            }
            recipeByCategory.compute(recipe.getRecipeCategory(), (k, v) -> {
                if (v != null) v.remove(recipe);
                return v == null || v.isEmpty() ? null : v;
            });
            return true;
        }
        return false;
    }

    /**
     * Removes all recipes.
     *
     * @see GTRecipeHandler#removeAllRecipes(RecipeMap)
     */
    @ApiStatus.Internal
    void removeAllRecipes() {
        if (GroovyScriptModule.isCurrentlyRunning()) {
            this.lookup.getRecipes(false).forEach(this.getGroovyScriptRecipeMap()::addBackup);
        }
        this.lookup.getNodes().clear();
        this.lookup.getSpecialNodes().clear();
        this.recipeByCategory.clear();
    }

    /**
     * Performs additional validation of recipes before adding to the ingredient tree.
     *
     * @param validationResult the current validation result
     * @return the new result based on validation
     */
    @NotNull
    protected ValidationResult<Recipe> postValidateRecipe(@NotNull ValidationResult<Recipe> validationResult) {
        EnumValidationResult recipeStatus = validationResult.getType();
        Recipe recipe = validationResult.getResult();
        if (recipe.isGroovyRecipe()) {
            return validationResult;
        }

        boolean emptyInputs = recipe.getInputs().isEmpty() && recipe.getFluidInputs().isEmpty();
        if (emptyInputs) {
            GTLog.logger.error("Invalid amount of recipe inputs. Recipe inputs are empty.");
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError("Invalid amount of recipe inputs. Recipe inputs are empty.");
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        boolean emptyOutputs = !this.allowEmptyOutput && recipe.getEUt() > 0 && recipe.getOutputs().isEmpty() &&
                recipe.getFluidOutputs().isEmpty() && recipe.getChancedOutputs().getChancedEntries().isEmpty() &&
                recipe.getChancedFluidOutputs().getChancedEntries().isEmpty();
        if (emptyOutputs) {
            GTLog.logger.error("Invalid amount of recipe outputs. Recipe outputs are empty.");
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError("Invalid amount of outputs inputs. Recipe outputs are empty.");
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }

        int amount = recipe.getInputs().size();
        if (amount > getMaxInputs()) {
            GTLog.logger.error("Invalid amount of recipe inputs. Actual: {}. Should be at most {}.", amount,
                    getMaxInputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format(
                        "Invalid amount of recipe inputs. Actual: %s. Should be at most %s.", amount, getMaxInputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }

        amount = recipe.getOutputs().size() + recipe.getChancedOutputs().getChancedEntries().size();
        if (amount > getMaxOutputs()) {
            GTLog.logger.error("Invalid amount of recipe outputs. Actual: {}. Should be at most {}.", amount,
                    getMaxOutputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI
                        .logError(String.format("Invalid amount of recipe outputs. Actual: %s. Should be at most %s.",
                                amount, getMaxOutputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }

        amount = recipe.getFluidInputs().size();
        if (amount > getMaxFluidInputs()) {
            GTLog.logger.error("Invalid amount of recipe fluid inputs. Actual: {}. Should be at most {}.", amount,
                    getMaxFluidInputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(
                        String.format("Invalid amount of recipe fluid inputs. Actual: %s. Should be at most %s.",
                                amount, getMaxFluidInputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }

        amount = recipe.getFluidOutputs().size() + recipe.getChancedFluidOutputs().getChancedEntries().size();
        if (amount > getMaxFluidOutputs()) {
            GTLog.logger.error("Invalid amount of recipe fluid outputs. Actual: {}. Should be at most {}.", amount,
                    getMaxFluidOutputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Outputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(
                        String.format("Invalid amount of recipe fluid outputs. Actual: %s. Should be at most %s.",
                                amount, getMaxFluidOutputs()));
                CraftTweakerAPI.logError("Stacktrace:",
                        new IllegalArgumentException("Invalid number of Fluid Outputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        return ValidationResult.newResult(recipeStatus, recipe);
    }

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
    public Recipe findRecipe(long voltage, final List<ItemStack> inputs, final List<FluidStack> fluidInputs,
                             boolean exactVoltage) {
        final List<ItemStack> items = inputs.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
        final List<FluidStack> fluids = fluidInputs.stream().filter(f -> f != null && f.amount != 0)
                .collect(Collectors.toList());

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
    protected List<List<AbstractMapIngredient>> prepareRecipeFind(@NotNull Collection<ItemStack> items,
                                                                  @NotNull Collection<FluidStack> fluids) {
        // First, check if items and fluids are valid.
        if (items.size() == Integer.MAX_VALUE || fluids.size() == Integer.MAX_VALUE) {
            return null;
        }
        if (items.size() == 0 && fluids.size() == 0) {
            return null;
        }

        // Build input.
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(items.size() + fluids.size());
        if (items.size() > 0) buildFromItemStacks(list, uniqueItems(items));
        if (fluids.size() > 0) buildFromFluidStacks(list, fluids);

        // nothing was added, so return nothing
        if (list.size() == 0) return null;
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
    public Recipe find(@NotNull Collection<ItemStack> items, @NotNull Collection<FluidStack> fluids,
                       @NotNull Predicate<Recipe> canHandle) {
        List<List<AbstractMapIngredient>> list = prepareRecipeFind(items, fluids);
        // couldn't build any inputs to use for search, so no recipe could be found
        if (list == null) return null;
        return recurseIngredientTreeFindRecipe(list, lookup, canHandle);
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
    @NotNull
    public static ItemStack[] uniqueItems(@NotNull Collection<ItemStack> inputs) {
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
    @NotNull
    public static List<GTRecipeInput> uniqueIngredientsList(@NotNull Collection<GTRecipeInput> inputs) {
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
     * Recursively finds a recipe, top level.
     *
     * @param ingredients the ingredients part
     * @param branchRoot  the root branch to search from.
     * @param canHandle   if the found recipe is valid
     * @return a recipe
     */
    @Nullable
    private Recipe recurseIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                   @NotNull Branch branchRoot, @NotNull Predicate<Recipe> canHandle) {
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
    private Recipe recurseIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                   @NotNull Branch branchMap, @NotNull Predicate<Recipe> canHandle,
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
                        potentialBranch -> diveIngredientTreeFindRecipe(ingredients, potentialBranch, canHandle, index,
                                count, skip));
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
    private Recipe diveIngredientTreeFindRecipe(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                @NotNull Branch map,
                                                @NotNull Predicate<Recipe> canHandle, int currentIndex, int count,
                                                long skip) {
        // We loop around ingredients.size() if we reach the end.
        // only end when all ingredients are exhausted, or a recipe is found
        int i = (currentIndex + 1) % ingredients.size();
        while (i != currentIndex) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << i)) == 0)) {
                // Recursive call
                // Increase the count, so the recursion can terminate if needed (ingredients is exhausted)
                // Append the current index to the skip list
                Recipe found = recurseIngredientTreeFindRecipe(ingredients, map, canHandle, i, count + 1,
                        skip | (1L << i));
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
    private void recurseIngredientTreeFindRecipeCollisions(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                           @NotNull Branch branchRoot,
                                                           @NotNull Set<Recipe> collidingRecipes) {
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
    private Recipe recurseIngredientTreeFindRecipeCollisions(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                             @NotNull Branch branchMap, int index, int count, long skip,
                                                             @NotNull Set<Recipe> collidingRecipes) {
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
                        right -> diveIngredientTreeFindRecipeCollisions(ingredients, right, index, count, skip,
                                collidingRecipes));
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
    private Recipe diveIngredientTreeFindRecipeCollisions(@NotNull List<List<AbstractMapIngredient>> ingredients,
                                                          @NotNull Branch map, int currentIndex, int count, long skip,
                                                          @NotNull Set<Recipe> collidingRecipes) {
        // We loop around ingredients.size() if we reach the end.
        // only end when all ingredients are exhausted, or a recipe is found
        int i = (currentIndex + 1) % ingredients.size();
        while (i != currentIndex) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << i)) == 0)) {
                // Recursive call
                // Increase the count, so the recursion can terminate if needed (ingredients is exhausted)
                // Append the current index to the skip list
                Recipe r = recurseIngredientTreeFindRecipeCollisions(ingredients, map, i, count + 1, skip | (1L << i),
                        collidingRecipes);
                if (r != null) {
                    return r;
                }
            }
            // increment the index if the current index is skipped, or the recipe is not found
            i = (i + 1) % ingredients.size();
        }
        return null;
    }

    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems,
                                                 FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(200, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    // this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems,
                                              IItemHandlerModifiable exportItems, FluidTankList importFluids,
                                              FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture,
                moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    // this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplateNoOutputs(DoubleSupplier progressSupplier,
                                                       IItemHandlerModifiable importItems,
                                                       IItemHandlerModifiable exportItems, FluidTankList importFluids,
                                                       FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture,
                moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    protected void addInventorySlotGroup(ModularUI.Builder builder, IItemHandlerModifiable itemHandler,
                                         FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.getTanks();
        boolean invertFluids = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            itemInputsCount = fluidInputsCount;
            fluidInputsCount = tmp;
            invertFluids = true;
        }
        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 70 - itemSlotsToLeft * 18;
        int startInputsY = 33 - (int) (itemSlotsToDown / 2.0 * 18) + yOffset;
        boolean wasGroup = itemHandler.getSlots() + fluidHandler.getTanks() == 12;
        if (wasGroup) startInputsY -= 9;
        else if (itemHandler.getSlots() >= 6 && fluidHandler.getTanks() >= 2 && !isOutputs) startInputsY -= 9;
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                if (slotIndex >= itemInputsCount) break;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                addSlot(builder, x, y, slotIndex, itemHandler, fluidHandler, invertFluids, isOutputs);
            }
        }
        if (wasGroup) startInputsY += 2;
        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                int startSpecX = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int y = startInputsY + 18 * i;
                    addSlot(builder, startSpecX, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            } else {
                int startSpecY = startInputsY + itemSlotsToDown * 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int x = isOutputs ? startInputsX + 18 * (i % 3) :
                            startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(builder, x, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            }
        }
    }

    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler,
                           FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        if (!isFluid) {
            builder.widget(new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs).setBackgroundTexture(
                    getOverlaysForSlot(isOutputs, false, slotIndex == itemHandler.getSlots() - 1)));
        } else {
            builder.widget(new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18).setAlwaysShowFull(true)
                    .setBackgroundTexture(getOverlaysForSlot(isOutputs, true, slotIndex == fluidHandler.getTanks() - 1))
                    .setContainerClicking(true, !isOutputs));
        }
    }

    protected TextureArea[] getOverlaysForSlot(boolean isOutput, boolean isFluid, boolean isLast) {
        TextureArea base = isFluid ? GuiTextures.FLUID_SLOT : GuiTextures.SLOT;
        byte overlayKey = (byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0));
        if (slotOverlays.containsKey(overlayKey)) {
            return new TextureArea[] { base, slotOverlays.get(overlayKey) };
        }
        return new TextureArea[] { base };
    }

    protected static int[] determineSlotsGrid(int itemInputsCount) {
        int itemSlotsToLeft;
        int itemSlotsToDown;
        double sqrt = Math.sqrt(itemInputsCount);
        // if the number of input has an integer root
        // return it.
        if (sqrt % 1 == 0) {
            itemSlotsToLeft = itemSlotsToDown = (int) sqrt;
        } else if (itemInputsCount == 3) {
            itemSlotsToLeft = 3;
            itemSlotsToDown = 1;
        } else {
            // if we couldn't fit all into a perfect square,
            // increase the amount of slots to the left
            itemSlotsToLeft = (int) Math.ceil(sqrt);
            itemSlotsToDown = itemSlotsToLeft - 1;
            // if we still can't fit all the slots in a grid,
            // increase the amount of slots on the bottom
            if (itemInputsCount > itemSlotsToLeft * itemSlotsToDown) {
                itemSlotsToDown = itemSlotsToLeft;
            }
        }
        return new int[] { itemSlotsToLeft, itemSlotsToDown };
    }

    /**
     * This height is used to determine size of background texture on JEI.
     */
    public int getPropertyHeightShift() {
        int maxPropertyCount = 0;
        if (shouldShiftWidgets()) {
            for (Recipe recipe : getRecipeList()) {
                if (recipe.getPropertyCount() > maxPropertyCount)
                    maxPropertyCount = recipe.getPropertyCount();
            }
        }
        return maxPropertyCount * 10; // GTRecipeWrapper#LINE_HEIGHT
    }

    private boolean shouldShiftWidgets() {
        return getMaxInputs() + getMaxOutputs() >= 6 ||
                getMaxFluidInputs() + getMaxFluidOutputs() >= 6;
    }

    @Method(modid = GTValues.MODID_GROOVYSCRIPT)
    private VirtualizedRecipeMap getGroovyScriptRecipeMap() {
        return ((VirtualizedRecipeMap) grsVirtualizedRecipeMap);
    }

    /**
     * This height is used to determine Y position to start drawing info on JEI.
     * 
     * @deprecated remove overrides, this method is no longer used in any way.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public int getPropertyListHeight(Recipe recipe) {
        return 0;
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
    private boolean recurseIngredientTreeAdd(@NotNull Recipe recipe,
                                             @NotNull List<List<AbstractMapIngredient>> ingredients,
                                             @NotNull Branch branchMap, int index, int count) {
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
                                CraftTweakerAPI.logError(String.format(
                                        "Recipe duplicate or conflict found in RecipeMap %s and was not added. See next lines for details.",
                                        this.unlocalizedName));

                                CraftTweakerAPI.logError(String.format("Attempted to add Recipe: %s",
                                        CTRecipeHelper.getRecipeAddLine(this, recipe)));

                                if (v.left().isPresent()) {
                                    CraftTweakerAPI.logError(String.format("Which conflicts with: %s",
                                            CTRecipeHelper.getRecipeAddLine(this, v.left().get())));
                                } else {
                                    CraftTweakerAPI.logError("Could not identify exact duplicate/conflict.");
                                }
                            }
                            if (recipe.isGroovyRecipe()) {
                                GroovyLog log = GroovyLog.get();
                                log.warn(
                                        "Recipe duplicate or conflict found in RecipeMap {} and was not added. See next lines for details",
                                        this.unlocalizedName);

                                log.warn("Attempted to add Recipe: {}", recipe.toString());

                                if (v.left().isPresent()) {
                                    log.warn("Which conflicts with: {}", v.left().get().toString());
                                } else {
                                    log.warn("Could not find exact duplicate/conflict.");
                                }
                            }
                            if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                                GTLog.logger.warn(
                                        "Recipe duplicate or conflict found in RecipeMap {} and was not added. See next lines for details",
                                        this.unlocalizedName);

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
            // If there's a recipe present, the addition is finished for this ingredient
            if (r.left().isPresent()) {
                if (r.left().get() == recipe) {
                    // Cannot return here, since each ingredient to add is a separate path to the recipe
                    continue;
                } else {
                    // exit if a different recipe is already present for this path
                    return false;
                }
            }

            // recursive part: apply the addition for the next ingredient in the list, for the right branch.
            // the right branch only contains ingredients, or is empty when the left branch is present
            boolean addedNextBranch = r.right()
                    .filter(m -> recurseIngredientTreeAdd(recipe, ingredients, m, (index + 1) % ingredients.size(),
                            count + 1))
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
     * Determine the correct root nodes for an ingredient
     *
     * @param ingredient the ingredient to check
     * @param branchMap  the branch containing the nodes
     * @return the correct nodes for the ingredient
     */
    @NotNull
    protected static Map<AbstractMapIngredient, Either<Recipe, Branch>> determineRootNodes(@NotNull AbstractMapIngredient ingredient,
                                                                                           @NotNull Branch branchMap) {
        return ingredient.isSpecialIngredient() ? branchMap.getSpecialNodes() : branchMap.getNodes();
    }

    /**
     * Converts a list of {@link GTRecipeInput}s for Fluids into a List of {@link AbstractMapIngredient}s.
     * Do not supply GTRecipeInputs dealing with any other type of input other than Fluids.
     *
     * @param list        the list of MapIngredients to add to
     * @param fluidInputs the GTRecipeInputs to convert
     */
    protected void buildFromRecipeFluids(@NotNull List<List<AbstractMapIngredient>> list,
                                         @NotNull List<GTRecipeInput> fluidInputs) {
        for (GTRecipeInput fluidInput : fluidInputs) {
            AbstractMapIngredient ingredient = new MapFluidIngredient(fluidInput);
            retrieveCachedIngredient(list, ingredient, fluidIngredientRoot);
        }
    }

    /**
     * Retrieves a cached ingredient, or inserts a default one
     *
     * @param list              the list to append to
     * @param defaultIngredient the ingredient to use as a default value, if not cached
     * @param cache             the ingredient root to retrieve from
     */
    protected static void retrieveCachedIngredient(@NotNull List<List<AbstractMapIngredient>> list,
                                                   @NotNull AbstractMapIngredient defaultIngredient,
                                                   @NotNull WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> cache) {
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
    protected void buildFromFluidStacks(@NotNull List<List<AbstractMapIngredient>> list,
                                        @NotNull Iterable<FluidStack> ingredients) {
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
    @NotNull
    protected List<List<AbstractMapIngredient>> fromRecipe(@NotNull Recipe r) {
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(
                (r.getInputs().size()) + r.getFluidInputs().size());
        if (r.getInputs().size() > 0) {
            buildFromRecipeItems(list, uniqueIngredientsList(r.getInputs()));
        }
        if (r.getFluidInputs().size() > 0) {
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
    protected void buildFromRecipeItems(List<List<AbstractMapIngredient>> list, @NotNull List<GTRecipeInput> inputs) {
        for (GTRecipeInput r : inputs) {
            if (r.isOreDict()) {
                AbstractMapIngredient ingredient;
                this.hasOreDictedInputs = true;
                if (r.hasNBTMatchingCondition()) {
                    hasNBTMatcherInputs = true;
                    ingredient = new MapOreDictNBTIngredient(r.getOreDict(), r.getNBTMatcher(),
                            r.getNBTMatchingCondition());
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
    protected void buildFromItemStacks(@NotNull List<List<AbstractMapIngredient>> list,
                                       @NotNull ItemStack[] ingredients) {
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

    protected RecipeMap<R> setSpecialTexture(int x, int y, int width, int height, TextureArea area) {
        this.specialTexturePosition = new int[] { x, y, width, height };
        this.specialTexture = area;
        return this;
    }

    protected ModularUI.Builder addSpecialTexture(ModularUI.Builder builder) {
        builder.image(specialTexturePosition[0], specialTexturePosition[1], specialTexturePosition[2],
                specialTexturePosition[3], specialTexture);
        return builder;
    }

    public Collection<Recipe> getRecipeList() {
        ObjectOpenHashSet<Recipe> recipes = new ObjectOpenHashSet<>();
        return lookup.getRecipes(true).filter(recipes::add).sorted(RECIPE_DURATION_THEN_EU)
                .collect(Collectors.toList());
    }

    public SoundEvent getSound() {
        return sound;
    }

    @ZenMethod("findRecipe")
    @Method(modid = GTValues.MODID_CT)
    @Nullable
    public CTRecipe ctFindRecipe(long maxVoltage, IItemStack[] itemInputs, ILiquidStack[] fluidInputs,
                                 @Optional(valueLong = Integer.MAX_VALUE) int outputFluidTankCapacity) {
        List<ItemStack> mcItemInputs = itemInputs == null ? Collections.emptyList() :
                Arrays.stream(itemInputs).map(CraftTweakerMC::getItemStack).collect(Collectors.toList());
        List<FluidStack> mcFluidInputs = fluidInputs == null ? Collections.emptyList() :
                Arrays.stream(fluidInputs).map(CraftTweakerMC::getLiquidStack).collect(Collectors.toList());
        Recipe backingRecipe = findRecipe(maxVoltage, mcItemInputs, mcFluidInputs, true);
        return backingRecipe == null ? null : new CTRecipe(this, backingRecipe);
    }

    @ZenGetter("recipes")
    @Method(modid = GTValues.MODID_CT)
    public List<CTRecipe> ccGetRecipeList() {
        return getRecipeList().stream().map(recipe -> new CTRecipe(this, recipe)).collect(Collectors.toList());
    }

    @ZenGetter("localizedName")
    public String getLocalizedName() {
        return LocalizationUtils.format(getTranslationKey());
    }

    @ZenGetter("translationKey")
    public String getTranslationKey() {
        return "recipemap." + unlocalizedName + ".name";
    }

    @ZenGetter("unlocalizedName")
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public R recipeBuilder() {
        return recipeBuilderSample.copy().onBuild(onRecipeBuildAction);
    }

    /**
     * Removes a recipe from the map. (recursive part)
     *
     * @param recipeToRemove the recipe to add.
     * @param ingredients    list of input ingredients.
     * @param branchMap      the current branch in the recursion.
     */
    @Nullable
    private Recipe recurseIngredientTreeRemove(@NotNull Recipe recipeToRemove,
                                               @NotNull List<List<AbstractMapIngredient>> ingredients,
                                               @NotNull Branch branchMap, int depth) {
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
                            potentialBranch -> recurseIngredientTreeRemove(recipeToRemove,
                                    ingredients.subList(1, ingredients.size()), potentialBranch, depth + 1));
                    if (r == recipeToRemove) {
                        found = r;
                    } else {
                        // wasn't the correct recipe
                        if (recipeToRemove.getIsCTRecipe()) {
                            CraftTweakerAPI.logError(String.format("Failed to remove Recipe from RecipeMap %s: %s",
                                    this.unlocalizedName, CTRecipeHelper.getRecipeRemoveLine(this, recipeToRemove)));
                        }
                        if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                            GTLog.logger.warn("Failed to remove recipe from RecipeMap {}. See next lines for details",
                                    this.unlocalizedName);
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

    @ZenMethod("recipeBuilder")
    @Method(modid = GTValues.MODID_CT)
    public CTRecipeBuilder ctRecipeBuilder() {
        return new CTRecipeBuilder(recipeBuilder());
    }

    @ZenGetter("maxInputs")
    public int getMaxInputs() {
        return maxInputs;
    }

    @ZenSetter("maxInputs")
    public void setMaxInputs(int maxInputs) {
        if (modifyItemInputs) {
            this.maxInputs = Math.max(this.maxInputs, maxInputs);
        } else {
            throw new UnsupportedOperationException("Cannot change max item input amount for " + getUnlocalizedName());
        }
    }

    @ZenGetter("maxOutputs")
    public int getMaxOutputs() {
        return maxOutputs;
    }

    @ZenSetter("maxOutputs")
    public void setMaxOutputs(int maxOutputs) {
        if (modifyItemOutputs) {
            this.maxOutputs = Math.max(this.maxOutputs, maxOutputs);
        } else {
            throw new UnsupportedOperationException("Cannot change max item output amount for " + getUnlocalizedName());
        }
    }

    @ZenGetter("maxFluidInputs")
    public int getMaxFluidInputs() {
        return maxFluidInputs;
    }

    @ZenSetter("maxFluidInputs")
    public void setMaxFluidInputs(int maxFluidInputs) {
        if (modifyFluidInputs) {
            this.maxFluidInputs = Math.max(this.maxFluidInputs, maxFluidInputs);
        } else {
            throw new UnsupportedOperationException("Cannot change max fluid input amount for " + getUnlocalizedName());
        }
    }

    @ZenGetter("maxFluidOutputs")
    public int getMaxFluidOutputs() {
        return maxFluidOutputs;
    }

    @ZenSetter("maxFluidOutputs")
    public void setMaxFluidOutputs(int maxFluidOutputs) {
        if (modifyFluidOutputs) {
            this.maxFluidOutputs = Math.max(this.maxFluidOutputs, maxFluidOutputs);
        } else {
            throw new UnsupportedOperationException(
                    "Cannot change max fluid output amount for " + getUnlocalizedName());
        }
    }

    /**
     * <strong>This is not suitable for Recipe Lookup.</strong>
     * Use {@link #findRecipe(long, List, List)} instead.
     *
     * @return the recipes stored by category.
     */
    @NotNull
    public Map<GTRecipeCategory, List<Recipe>> getRecipesByCategory() {
        return Collections.unmodifiableMap(recipeByCategory);
    }

    @Override
    @ZenMethod
    public String toString() {
        return "RecipeMap{" + "unlocalizedName='" + unlocalizedName + '\'' + '}';
    }

    @Override
    public int hashCode() {
        return unlocalizedName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RecipeMap)) return false;
        return ((RecipeMap<?>) obj).unlocalizedName.equals(this.unlocalizedName);
    }
}
