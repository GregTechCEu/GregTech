package gregtech.api.recipes;

import com.google.common.collect.ImmutableList;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.recipes.crafttweaker.CTRecipe;
import gregtech.api.recipes.crafttweaker.CTRecipeBuilder;
import gregtech.api.recipes.map.AbstractMapIngredient;
import gregtech.api.recipes.map.MapFluidIngredient;
import gregtech.api.recipes.map.MapItemStackIngredient;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.*;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.items.IItemHandlerModifiable;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ZenClass("mods.gregtech.recipe.RecipeMap")
@ZenRegister
public class RecipeMap<R extends RecipeBuilder<R>> {

    private static final Map<String, RecipeMap<?>> RECIPE_MAP_REGISTRY = new HashMap<>();
    public IChanceFunction chanceFunction = (chance, boostPerTier, tier) -> chance + (boostPerTier * tier);

    public final String unlocalizedName;

    private final R recipeBuilderSample;
    private final int minInputs, maxInputs;
    private final int minOutputs, maxOutputs;
    private final int minFluidInputs, maxFluidInputs;
    private final int minFluidOutputs, maxFluidOutputs;
    protected final TByteObjectMap<TextureArea> slotOverlays;
    protected TextureArea specialTexture;
    protected int[] specialTexturePosition;
    protected TextureArea progressBarTexture;
    protected MoveType moveType;
    public final boolean isHidden;

    private final Object2ObjectOpenHashMap<FluidKey, Set<Recipe>> recipeFluidMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<ItemStackKey, Set<Recipe>> recipeItemMap = new Object2ObjectOpenHashMap<>();

    private final Branch LOOKUP = new Branch();
    private final List<Recipe> RECIPES_TO_COMPILE = new ObjectArrayList<>();
    private final Set<AbstractMapIngredient> ROOT = new ObjectOpenHashSet<>();
    private static final ItemStack[] EMPTY_ITEM = new ItemStack[0];
    private static final FluidStack[] EMPTY_FLUID = new FluidStack[0];


    private static final Comparator<Recipe> RECIPE_DURATION_THEN_EU =
            Comparator.comparingInt(Recipe::getDuration)
                    .thenComparingInt(Recipe::getEUt)
                    .thenComparing(Recipe::hashCode);

    private final Set<Recipe> recipeSet = new TreeSet<>(RECIPE_DURATION_THEN_EU);

    private Consumer<RecipeBuilder<?>> onRecipeBuildAction;

    protected SoundEvent sound;

    private RecipeMap<?> smallRecipeMap;

    public RecipeMap(String unlocalizedName,
                    int minInputs, int maxInputs, int minOutputs, int maxOutputs,
                    int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs,
                    R defaultRecipe, boolean isHidden) {
        this.unlocalizedName = unlocalizedName;
        this.slotOverlays = new TByteObjectHashMap<>();
        this.progressBarTexture = GuiTextures.PROGRESS_BAR_ARROW;
        this.moveType = MoveType.HORIZONTAL;

        this.minInputs = minInputs;
        this.minFluidInputs = minFluidInputs;
        this.minOutputs = minOutputs;
        this.minFluidOutputs = minFluidOutputs;

        this.maxInputs = maxInputs;
        this.maxFluidInputs = maxFluidInputs;
        this.maxOutputs = maxOutputs;
        this.maxFluidOutputs = maxFluidOutputs;

        this.isHidden = isHidden;
        defaultRecipe.setRecipeMap(this);
        this.recipeBuilderSample = defaultRecipe;
        RECIPE_MAP_REGISTRY.put(unlocalizedName, this);
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
    public IChanceFunction getChanceFunction() {
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
            GTLog.logger.error("Error happened during processing ore registration of prefix {} and material {}. " +
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
        return this
                .setSlotOverlay(isOutput, isFluid, false, slotOverlay)
                .setSlotOverlay(isOutput, isFluid, true, slotOverlay);
    }

    public RecipeMap<R> setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, TextureArea slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public RecipeMap<R> setSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    @ZenMethod("setChanceFunction")
    public RecipeMap<R> setChanceFunction(IChanceFunction function) {
        chanceFunction = function;
        return this;
    }

    public RecipeMap<R> onRecipeBuild(Consumer<RecipeBuilder<?>> consumer) {
        onRecipeBuildAction = consumer;
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
     * This is alternative case when machine can input given fluid
     * If this method returns true, machine will receive given fluid even if getRecipesForFluid doesn't have
     * any recipe for this fluid
     */
    public boolean canInputFluidForce(Fluid fluid) {
        return false;
    }

    public Collection<Recipe> getRecipesForFluid(FluidStack fluid) {
        return recipeFluidMap.getOrDefault(new FluidKey(fluid), Collections.emptySet());
    }

    public Collection<Recipe> getRecipesForFluid(FluidKey fluidKey) {
        return recipeFluidMap.getOrDefault(fluidKey, Collections.emptySet());
    }

    private static boolean foundInvalidRecipe = false;

    //internal usage only, use buildAndRegister()
    public void addRecipe(ValidationResult<Recipe> validationResult) {
        validationResult = postValidateRecipe(validationResult);
        switch (validationResult.getType()) {
            case SKIP:
                return;
            case INVALID:
                setFoundInvalidRecipe(true);
                return;
        }
        Recipe recipe = validationResult.getResult();
        if (recipeSet.add(recipe)) {
            compileRecipe(recipe);
            /*
            for (CountableIngredient countableIngredient : recipe.getInputs()) {
                ItemStack[] stacks = countableIngredient.getIngredient().getMatchingStacks();
                for (ItemStack itemStack : stacks) {
                    ItemStackKey stackKey = KeySharedStack.getRegisteredStack(itemStack);
                    recipeItemMap.computeIfPresent(stackKey, (k, v) -> {
                        v.add(recipe);
                        return v;
                    });
                    recipeItemMap.computeIfAbsent(stackKey, k -> new HashSet<>()).add(recipe);
                }
            }
            for (FluidStack fluid : recipe.getFluidInputs()) {
                if (fluid.tag != null && fluid.tag.hasKey("nonConsumable")) {
                    fluid = fluid.copy();
                    fluid.tag.removeTag("nonConsumable");
                    if (fluid.tag.isEmpty()) {
                        fluid.tag = null;
                    }
                }
                FluidKey fluidKey = new FluidKey(fluid);
                recipeFluidMap.computeIfPresent(fluidKey, (k, v) -> {
                    v.add(recipe);
                    return v;
                });
                recipeFluidMap.computeIfAbsent(fluidKey, k -> new HashSet<>()).add(recipe);
            }*/
        } else if (ConfigHolder.misc.debug) {
            GTLog.logger.warn("Recipe: {} for Recipe Map {} is a duplicate and was not added", recipe.toString(), this.unlocalizedName);
            if(recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Recipe: %s for Recipe Map %s is a duplicate and was not added", recipe.toString(), this.unlocalizedName));
            }
        }
    }

    public void compileRecipe(Recipe recipe) {
        if (recipe == null)
            return;
        // boolean flag = false;
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe, true);

        // Recipe r = recurseItemTreeFind(items, map, rr -> true);
        // if (r != null) {
        // Antimatter.LOGGER.warn("Recipe collision, adding both but only first is
        // available.");
        // }
        if (recurseItemTreeAdd(recipe, items, LOOKUP, 0, 0)) {
            items.forEach(t -> t.forEach(ing -> ROOT.add(ing)));
        }
    }

    public boolean removeRecipe(Recipe recipe) {
        //if we actually removed this recipe
        if (recipeSet.remove(recipe)) {
            //also iterate trough fluid mappings and remove recipe from them
            recipeFluidMap.values().forEach(fluidMap ->
                    fluidMap.removeIf(fluidRecipe -> fluidRecipe == recipe));
            recipeItemMap.values().forEach(itemMap ->
                    itemMap.removeIf(itemRecipe -> itemRecipe == recipe));

            return true;
        }
        return false;
    }

    protected ValidationResult<Recipe> postValidateRecipe(ValidationResult<Recipe> validationResult) {
        EnumValidationResult recipeStatus = validationResult.getType();
        Recipe recipe = validationResult.getResult();
        if (!GTUtility.isBetweenInclusive(getMinInputs(), getMaxInputs(), recipe.getInputs().size())) {
            GTLog.logger.error("Invalid amount of recipe inputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getInputs().size(), getMinInputs(), getMaxInputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            if(recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe inputs. Actual: %s. Should be between %s and %s inclusive.", recipe.getInputs().size(), getMinInputs(), getMaxInputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (!GTUtility.isBetweenInclusive(getMinOutputs(), getMaxOutputs(), recipe.getOutputs().size() + recipe.getChancedOutputs().size())) {
            GTLog.logger.error("Invalid amount of recipe outputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getOutputs().size() + recipe.getChancedOutputs().size(), getMinOutputs(), getMaxOutputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            if(recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe outputs. Actual: %s. Should be between %s and %s inclusive.", recipe.getOutputs().size() + recipe.getChancedOutputs().size(), getMinOutputs(), getMaxOutputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (!GTUtility.isBetweenInclusive(getMinFluidInputs(), getMaxFluidInputs(), recipe.getFluidInputs().size())) {
            GTLog.logger.error("Invalid amount of recipe fluid inputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getFluidInputs().size(), getMinFluidInputs(), getMaxFluidInputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));
            if(recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe fluid inputs. Actual: %s. Should be between %s and %s inclusive.", recipe.getFluidInputs().size(), getMinFluidInputs(), getMaxFluidInputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (!GTUtility.isBetweenInclusive(getMinFluidOutputs(), getMaxFluidOutputs(), recipe.getFluidOutputs().size())) {
            GTLog.logger.error("Invalid amount of recipe fluid outputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getFluidOutputs().size(), getMinFluidOutputs(), getMaxFluidOutputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Outputs"));
            if(recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe fluid outputs. Actual: %s. Should be between %s and %s inclusive.", recipe.getFluidOutputs().size(), getMinFluidOutputs(), getMaxFluidOutputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Outputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        return ValidationResult.newResult(recipeStatus, recipe);
    }

    @Nullable
    public Recipe findRecipe(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int outputFluidTankCapacity) {
        return this.findRecipe(voltage, GTUtility.itemHandlerToList(inputs), GTUtility.fluidHandlerToList(fluidInputs), outputFluidTankCapacity);
    }

    /**
     * Finds a Recipe matching the Fluid and/or ItemStack Inputs.
     *
     * @param voltage                 Voltage of the Machine or Long.MAX_VALUE if it has no Voltage
     * @param inputs                  the Item Inputs
     * @param fluidInputs             the Fluid Inputs
     * @param outputFluidTankCapacity minimal capacity of output fluid tank, used for fluid canner recipes for example
     * @return the Recipe it has found or null for no matching Recipe
     */
    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, int outputFluidTankCapacity) {
        return findRecipe(voltage, inputs, fluidInputs, outputFluidTankCapacity, false);
    }

    /**
     * Finds a Recipe matching the Fluid and/or ItemStack Inputs.
     *
     * @param voltage                 Voltage of the Machine or Long.MAX_VALUE if it has no Voltage
     * @param inputs                  the Item Inputs
     * @param fluidInputs             the Fluid Inputs
     * @param outputFluidTankCapacity minimal capacity of output fluid tank, used for fluid canner recipes for example
     * @param exactVoltage            should require exact voltage matching on recipe. used by craftweaker
     * @return the Recipe it has found or null for no matching Recipe
     */

    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, int outputFluidTankCapacity, boolean exactVoltage) {
        if (recipeSet.isEmpty())
            return null;
        if (minFluidInputs > 0 && GTUtility.amountOfNonNullElements(fluidInputs) < minFluidInputs) {
            return null;
        }
        //if (minInputs > 0 && GTUtility.amountOfNonEmptyStacks(inputs) < minInputs) {
        //    return null;
        //}
        return find(inputs.stream().filter(t -> t != null && !t.isEmpty()).toArray(ItemStack[]::new), fluidInputs.stream().filter(t -> t != null).toArray(FluidStack[]::new), a -> a.matches(false, inputs, fluidInputs, matchingMode));
    }

    public boolean acceptsItem(ItemStack item) {
        return ROOT.contains(new MapItemStackIngredient(item, false));
    }

    public boolean acceptsFluid(FluidStack fluid) {
        return ROOT.contains(new MapFluidIngredient(fluid, false));
    }

    @Nullable
    public Recipe find(@Nonnull ItemStack[] items, @Nonnull FluidStack[] fluids, @Nonnull Predicate<Recipe> canHandle) {
        // First, check if items and fluids are valid.
        if (items.length + fluids.length > Long.SIZE) {
            return null;
        }
        if (items.length == 0 && fluids.length == 0)
            return null;
        // Filter out empty fluids.

        // Build input.
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(items.length + fluids.length);
        if (items.length > 0) {
            buildFromItemStacks(list, uniqueItems(items));
        }
        if (fluids.length > 0) {
            List<FluidStack> stack = new ObjectArrayList<>(fluids.length);
            for (FluidStack f : fluids) {
                if (!(f.amount == 0))
                    stack.add(f);
            }
            if (stack.size() > 0)
                buildFromFluidStacks(list, stack, false);
        }
        if (list.size() == 0)
            return null;
        // Find recipe.
        // long current = System.nanoTime();
        Recipe r = recurseItemTreeFind(list, LOOKUP, canHandle);
        // Antimatter.LOGGER.info("Time to lookup (µs): " + ((System.nanoTime() -
        // current) / 1000));

        return r;
    }


    // In the case of split stacks, merge the items, 2 aluminium dust in separate
    // stacks -> 1 stack with additive count.
    public static ItemStack[] uniqueItems(ItemStack[] input) {
        List<ItemStack> list = new ObjectArrayList<>(input.length);
        loop: for (ItemStack item : input) {
            for (ItemStack obj : list) {
                if (item.isItemEqual(obj)) {
                    obj.grow(item.getCount());
                    continue loop;
                }
            }
            // Add a copy here or it might mutate the stack.
            list.add(item.copy());
        }
        return list.toArray(new ItemStack[0]);
    }
    /**
     * Recursively finds a recipe, top level. call this to find a recipe
     *
     * @param items the items part
     * @param map   the root branch to search from.
     * @return a recipe
     */
    Recipe recurseItemTreeFind(@Nonnull List<List<AbstractMapIngredient>> items, @Nonnull Branch map,
                               @Nonnull Predicate<Recipe> canHandle) {
        // Try each ingredient as a starting point, adding it to the skiplist.
        for (int i = 0; i < items.size(); i++) {
            Recipe r = recurseItemTreeFind(items, map, canHandle, i, 0, (1L << i));
            if (r != null)
                return r;
        }
        return null;
    }

    /**
     * Recursively finds a recipe
     *
     * @param items     the items part
     * @param map       the current branch of the tree
     * @param canHandle predicate to test found recipe.
     * @param index     the index of the wrapper to get
     * @param count     how deep we are in recursion, < items.length
     * @param skip      bitmap of items to skip, i.e. which items are used in the
     *                  recursion.
     * @return a recipe
     */
    Recipe recurseItemTreeFind(@Nonnull List<List<AbstractMapIngredient>> items, @Nonnull Branch map,
                               @Nonnull Predicate<Recipe> canHandle, int index, int count, long skip) {
        if (count == items.size())
            return null;
        List<AbstractMapIngredient> wr = items.get(index);
        // Iterate over current level of nodes.
        for (AbstractMapIngredient t : wr) {
            Either<List<Recipe>, RecipeMap.Branch> result = map.NODES.get(t);
            if (result != null) {
                // Either return recipe or continue branch.
                Recipe r = result.map(left -> {
                    for (Recipe recipe : left) {
                        if (canHandle.test(recipe)) {
                            return recipe;
                        }
                    }
                    return null;
                }, right -> callback(items, right, canHandle, index, count, skip));
                if (r != null)
                    return r;
            }
            if (map.SPECIAL_NODES.size() > 0) {
                // Iterate over special nodes.
                for (Tuple<AbstractMapIngredient, Either<Recipe, Branch>> tuple : map.SPECIAL_NODES) {
                    AbstractMapIngredient special = tuple.getFirst();
                    if (special.equals(t)) {
                        return tuple.getSecond().map(r -> canHandle.test(r) ? r : null,
                                branch -> callback(items, branch, canHandle, index, count, skip));
                    }
                }
            }
        }
        return null;
    }

    private Recipe callback(@Nonnull List<List<AbstractMapIngredient>> items, @Nonnull Branch map,
                            Predicate<Recipe> canHandle, int index, int count, long skip) {
        // We loop around items.size() if we reach the end.
        int counter = (index + 1) % items.size();
        while (counter != index) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << counter)) == 0)) {
                // Recursive call.
                Recipe found = recurseItemTreeFind(items, map, canHandle, counter, count + 1, skip | (1L << counter));
                if (found != null)
                    return found;
            }
            counter = (counter + 1) % items.size();
        }
        return null;
    }


    @Nullable
    private Recipe findByInputsAndFluids(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, boolean exactVoltage) {
        HashSet<Recipe> iteratedRecipes = new HashSet<>();
        HashSet<ItemStackKey> searchedItems = new HashSet<>();
        HashSet<FluidKey> searchedFluids = new HashSet<>();
        HashMap<Integer, LinkedList<Recipe>> priorityRecipeMap = new HashMap<>();
        HashMap<Recipe, Integer> promotedTimes = new HashMap<>();

        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
                ItemStackKey itemStackKey = KeySharedStack.getRegisteredStack(stack);
                if (!searchedItems.contains(itemStackKey) && recipeItemMap.containsKey(itemStackKey)) {
                    searchedItems.add(itemStackKey);
                    for (Recipe tmpRecipe : recipeItemMap.get(itemStackKey)) {
                        if (!exactVoltage && voltage < tmpRecipe.getEUt()) {
                            continue;
                        } else if (exactVoltage && voltage != tmpRecipe.getEUt()) {
                            continue;
                        }
                        calculateRecipePriority(tmpRecipe, promotedTimes, priorityRecipeMap);
                    }
                }
            }
        }

        for (FluidStack fluidStack : fluidInputs) {
            if (fluidStack != null) {
                FluidKey fluidKey = new FluidKey(fluidStack);
                if (!searchedFluids.contains(fluidKey) && recipeFluidMap.containsKey(fluidKey)) {
                    searchedFluids.add(fluidKey);
                    for (Recipe tmpRecipe : recipeFluidMap.get(fluidKey)) {
                        if (!exactVoltage && voltage < tmpRecipe.getEUt()) {
                            continue;
                        } else if (exactVoltage && voltage != tmpRecipe.getEUt()) {
                            continue;
                        }
                        calculateRecipePriority(tmpRecipe, promotedTimes, priorityRecipeMap);
                    }
                }
            }
        }

        return prioritizedRecipe(priorityRecipeMap, iteratedRecipes, inputs, fluidInputs);
    }

    private Recipe prioritizedRecipe(Map<Integer, LinkedList<Recipe>> priorityRecipeMap, HashSet<Recipe> iteratedRecipes, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
        for (int i = priorityRecipeMap.size() - 1; i >= 0; i--) {
            if (priorityRecipeMap.containsKey(i)) {
                for (Recipe tmpRecipe : priorityRecipeMap.get(i)) {
                    if (iteratedRecipes.add(tmpRecipe)) {
                        if (tmpRecipe.matches(false, inputs, fluidInputs)) {
                            return tmpRecipe;
                        }
                    }
                }
            }
        }

        return null;
    }

    private void calculateRecipePriority(Recipe recipe, HashMap<Recipe, Integer> promotedTimes, Map<Integer, LinkedList<Recipe>> priorityRecipeMap) {
        Integer p = promotedTimes.get(recipe);
        if (p == null) {
            p = 0;
        }
        promotedTimes.put(recipe, p + 1);
        priorityRecipeMap.computeIfAbsent(p, k -> new LinkedList<>());
        priorityRecipeMap.get(p).add(recipe);
    }

    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(200, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null)
            addSpecialTexture(builder);
        return builder;
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null)
            addSpecialTexture(builder);
        return builder;
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplateNoOutputs(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null)
            addSpecialTexture(builder);
        return builder;
    }

    protected void addInventorySlotGroup(ModularUI.Builder builder, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
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
                    int x = isOutputs ? startInputsX + 18 * (i % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(builder, x, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            }
        }
    }

    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        if (!isFluid) {
            builder.widget(new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs)
                    .setBackgroundTexture(getOverlaysForSlot(isOutputs, false, slotIndex == itemHandler.getSlots() - 1)));
        } else {
            builder.widget(new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18)
                    .setAlwaysShowFull(true)
                    .setBackgroundTexture(getOverlaysForSlot(isOutputs, true, slotIndex == fluidHandler.getTanks() - 1))
                    .setContainerClicking(true, !isOutputs));
        }
    }

    protected TextureArea[] getOverlaysForSlot(boolean isOutput, boolean isFluid, boolean isLast) {
        TextureArea base = isFluid ? GuiTextures.FLUID_SLOT : GuiTextures.SLOT;
        byte overlayKey = (byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0));
        if (slotOverlays.containsKey(overlayKey)) {
            return new TextureArea[]{base, slotOverlays.get(overlayKey)};
        }
        return new TextureArea[]{base};
    }

    protected static int[] determineSlotsGrid(int itemInputsCount) {
        int itemSlotsToLeft;
        int itemSlotsToDown;
        double sqrt = Math.sqrt(itemInputsCount);
        //if the number of input has an integer root
        //return it.
        if (sqrt % 1 == 0) {
            itemSlotsToLeft = itemSlotsToDown = (int) sqrt;
        } else if (itemInputsCount == 3) {
            itemSlotsToLeft = 3;
            itemSlotsToDown = 1;
        } else {
            //if we couldn't fit all into a perfect square,
            //increase the amount of slots to the left
            itemSlotsToLeft = (int) Math.ceil(sqrt);
            itemSlotsToDown = itemSlotsToLeft - 1;
            //if we still can't fit all the slots in a grid,
            //increase the amount of slots on the bottom
            if (itemInputsCount > itemSlotsToLeft * itemSlotsToDown) {
                itemSlotsToDown = itemSlotsToLeft;
            }
        }
        return new int[]{itemSlotsToLeft, itemSlotsToDown};
    }

    /**
     * Adds a recipe to the map. (recursive part)
     *
     * @param recipe      the recipe to add.
     * @param ingredients list of input ingredients.
     * @param map         the current place in the recursion.
     * @param index       where in the ingredients list we are.
     * @param count       how many added already.
     */
    boolean recurseItemTreeAdd(@Nonnull Recipe recipe, @Nonnull List<List<AbstractMapIngredient>> ingredients,
                               @Nonnull Branch map, int index, int count) {
        if (count >= ingredients.size())
            return true;
        if (index >= ingredients.size()) {
            throw new RuntimeException("Index out of bounds for recurseItemTreeAdd, should not happen");
        }
        // Loop through NUMBER_OF_INGREDIENTS times.
        List<AbstractMapIngredient> current = ingredients.get(index);
        Either<List<Recipe>, Branch> r;
        for (AbstractMapIngredient obj : current) {
            // Either add the recipe or create a branch.
            r = map.NODES.compute(obj, (k, v) -> {
                if (count == ingredients.size() - 1) {
                    if (v == null) {
                        v = Either.left(new ObjectArrayList<>());
                    }
                    v.ifLeft(list -> list.add(recipe));
                    return v;
                } else if (v == null) {
                    Branch traverse = new Branch();
                    v = Either.right(traverse);
                }
                return v;
            });
            // At the end, return.
            if (count == ingredients.size() - 1)
                continue;
            // If left was present before. Shouldn't be needed?
            /*
             * if (r.left().isPresent()) { Utils.onInvalidData("COLLISION DETECTED!");
             * current.forEach(map.NODES::remove); return false; }
             */
            // should always be present but this gives no warning.
            if (r.right().map(
                            m -> !recurseItemTreeAdd(recipe, ingredients, m, (index + 1) % ingredients.size(), count + 1))
                    .orElse(false)) {
                current.forEach(map.NODES::remove);
                return false;
            }
        }
        return true;
    }

    protected void buildFromFluids(List<List<AbstractMapIngredient>> builder, List<FluidStack> ingredients,
                                   boolean insideMap) {
        for (FluidStack t : ingredients) {
            builder.add(Collections.singletonList(new MapFluidIngredient(t, insideMap)));
        }
    }

    protected void buildFromFluidStacks(List<List<AbstractMapIngredient>> builder, List<FluidStack> ingredients,
                                        boolean insideMap) {
        for (FluidStack t : ingredients) {
            builder.add(Collections.singletonList(new MapFluidIngredient(t, insideMap)));
        }
    }

    protected List<List<AbstractMapIngredient>> fromRecipe(Recipe r, boolean insideMap) {
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(
                (r.getInputs().size())
                        + r.getFluidInputs().size());
        if (r.getInputs().size() > 0) {
            buildFromItems(list, r.getInputs(), insideMap);
        }
        if (r.getFluidInputs().size() > 0) {
            buildFromFluids(list, r.getFluidInputs(), insideMap);
        }
        return list;
    }

    protected void buildFromItems(List<List<AbstractMapIngredient>> list, List<CountableIngredient> ingredients, boolean insideMap) {
        for (CountableIngredient r : ingredients) {
            Ingredient t = r.getIngredient();
            List<AbstractMapIngredient> inner = new ObjectArrayList<>(t.getMatchingStacks().length);
            for (ItemStack stack : t.getMatchingStacks()) {
                inner.add(new MapItemStackIngredient(stack, insideMap));
            }
            list.add(inner);
            /*
            if (!false) {
                java.util.Optional<ResourceLocation> rl = java.util.Optional.empty();//MapTagIngredient.findCommonTag(t, tags);
                if (rl.isPresent()) {
                    list.add(Collections.singletonList(null));
                } else {
                    List<AbstractMapIngredient> inner = new ObjectArrayList<>(t.getMatchingStacks().length);
                    for (ItemStack stack : t.getMatchingStacks()) {
                        //if (r.ignoreNbt()) {
                        //    inner.add(new MapItemIngredient(stack.getItem(), insideMap));
                        //} else {
                        inner.add(new MapItemStackIngredient(stack, insideMap));
                        //}
                    }
                    list.add(inner);
                }
            } else {
               // list.add(Collections.singletonList(new SpecialIngredientWrapper(t)));
            }
             */
        }
    }

    protected void buildFromItemStacks(List<List<AbstractMapIngredient>> list, ItemStack[] ingredients) {
        for (ItemStack t : ingredients) {
            //OreDictionary.getOreIDs()
            List<AbstractMapIngredient> ls = new ObjectArrayList<>(2);
            ls.add(new MapItemStackIngredient(t, false));
            /*for (ResourceLocation rl : t.getItem().getTags()) {
                ls.add(new MapTagIngredient(rl, false));
            }*/
            list.add(ls);
        }
    }


    protected RecipeMap<R> setSpecialTexture(int x, int y, int width, int height, TextureArea area) {
        this.specialTexturePosition = new int[]{x, y, width, height};
        this.specialTexture = area;
        return this;
    }

    protected ModularUI.Builder addSpecialTexture(ModularUI.Builder builder) {
        builder.image(specialTexturePosition[0], specialTexturePosition[1], specialTexturePosition[2], specialTexturePosition[3], specialTexture);
        return builder;
    }


    public Collection<Recipe> getRecipeList() {
        return Collections.unmodifiableList(new ArrayList<>(recipeSet));
    }

    public SoundEvent getSound() {
        return sound;
    }

    @ZenMethod("findRecipe")
    @Method(modid = GTValues.MODID_CT)
    @Nullable
    public CTRecipe ctFindRecipe(long maxVoltage, IItemStack[] itemInputs, ILiquidStack[] fluidInputs, @Optional(valueLong = Integer.MAX_VALUE) int outputFluidTankCapacity) {
        List<ItemStack> mcItemInputs = itemInputs == null ? Collections.emptyList() :
                Arrays.stream(itemInputs)
                        .map(CraftTweakerMC::getItemStack)
                        .collect(Collectors.toList());
        List<FluidStack> mcFluidInputs = fluidInputs == null ? Collections.emptyList() :
                Arrays.stream(fluidInputs)
                        .map(CraftTweakerMC::getLiquidStack)
                        .collect(Collectors.toList());
        Recipe backingRecipe = findRecipe(maxVoltage, mcItemInputs, mcFluidInputs, outputFluidTankCapacity, true);
        return backingRecipe == null ? null : new CTRecipe(this, backingRecipe);
    }

    @ZenGetter("recipes")
    @Method(modid = GTValues.MODID_CT)
    public List<CTRecipe> ccGetRecipeList() {
        return getRecipeList().stream()
                .map(recipe -> new CTRecipe(this, recipe))
                .collect(Collectors.toList());
    }

    @ZenGetter("localizedName")
    public String getLocalizedName() {
        return LocalizationUtils.format("recipemap." + unlocalizedName + ".name");
    }

    @ZenGetter("unlocalizedName")
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public R recipeBuilder() {
        return recipeBuilderSample.copy().onBuild(onRecipeBuildAction);
    }

    @ZenMethod("recipeBuilder")
    @Method(modid = GTValues.MODID_CT)
    public CTRecipeBuilder ctRecipeBuilder() {
        return new CTRecipeBuilder(recipeBuilder());
    }

    @ZenGetter("minInputs")
    public int getMinInputs() {
        return minInputs;
    }

    @ZenGetter("maxInputs")
    public int getMaxInputs() {
        return maxInputs;
    }

    @ZenGetter("minOutputs")
    public int getMinOutputs() {
        return minOutputs;
    }

    @ZenGetter("maxOutputs")
    public int getMaxOutputs() {
        return maxOutputs;
    }

    @ZenGetter("minFluidInputs")
    public int getMinFluidInputs() {
        return minFluidInputs;
    }

    @ZenGetter("maxFluidInputs")
    public int getMaxFluidInputs() {
        return maxFluidInputs;
    }

    @ZenGetter("minFluidOutputs")
    public int getMinFluidOutputs() {
        return minFluidOutputs;
    }

    @ZenGetter("maxFluidOutputs")
    public int getMaxFluidOutputs() {
        return maxFluidOutputs;
    }

    @Override
    @ZenMethod
    public String toString() {
        return "RecipeMap{" +
                "unlocalizedName='" + unlocalizedName + '\'' +
                '}';
    }

    @FunctionalInterface
    @ZenClass("mods.gregtech.recipe.IChanceFunction")
    @ZenRegister
    public interface IChanceFunction {
        int chanceFor(int chance, int boostPerTier, int boostTier);
    }

    protected static class Branch {

        private Map<AbstractMapIngredient, Either<List<Recipe>, Branch>> NODES = new Object2ObjectOpenHashMap<>();

        private final List<Tuple<AbstractMapIngredient, Either<Recipe, Branch>>> SPECIAL_NODES = new ObjectArrayList<>();

        public Stream<Recipe> getRecipes(boolean filterHidden) {
            Stream<Recipe> stream = NODES.values().stream()
                    .flatMap(t -> t.map(Collection::stream, branch -> branch.getRecipes(filterHidden)));
            if (SPECIAL_NODES.size() > 0) {
                stream = Stream.concat(stream, SPECIAL_NODES.stream()
                        .flatMap(t -> t.getSecond().map(Stream::of, branch -> branch.getRecipes(filterHidden))));
            }
            if (filterHidden)
                stream = stream.filter(t -> !t.isHidden());
            return stream;
        }

        public void clear() {
            NODES = new Object2ObjectOpenHashMap<>();
            SPECIAL_NODES.clear();
        }

        public void finish() {
            // NODES.forEach((k,v) -> v.ifRight(Branch::finish));
            // this.NODES = ImmutableMap.<AbstractMapIngredient, Either<List<Recipe>,
            // Branch>>builder().putAll(NODES).build();
        }
    }


    public abstract static class Either<L, R> {

        private static final class Left<L, R> extends Either<L, R> {
            private final L value;

            public Left(final L value) {
                this.value = value;
            }

            @Override
            public <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2) {
                return new Left<>(f1.apply(value));
            }

            @Override
            public <T> T map(final Function<? super L, ? extends T> l, final Function<? super R, ? extends T> r) {
                return l.apply(value);
            }

            @Override
            public Either<L, R> ifLeft(Consumer<? super L> consumer) {
                consumer.accept(value);
                return this;
            }

            @Override
            public Either<L, R> ifRight(Consumer<? super R> consumer) {
                return this;
            }

            @Override
            public java.util.Optional<L> left() {
                return java.util.Optional.of(value);
            }

            @Override
            public java.util.Optional<R> right() {
                return java.util.Optional.empty();
            }

            @Override
            public String toString() {
                return "Left[" + value + "]";
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                final Left<?, ?> left = (Left<?, ?>) o;
                return Objects.equals(value, left.value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(value);
            }
        }

        private static final class Right<L, R> extends Either<L, R> {
            private final R value;

            public Right(final R value) {
                this.value = value;
            }

            @Override
            public <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2) {
                return new Right<>(f2.apply(value));
            }

            @Override
            public <T> T map(final Function<? super L, ? extends T> l, final Function<? super R, ? extends T> r) {
                return r.apply(value);
            }

            @Override
            public Either<L, R> ifLeft(Consumer<? super L> consumer) {
                return this;
            }

            @Override
            public Either<L, R> ifRight(Consumer<? super R> consumer) {
                consumer.accept(value);
                return this;
            }

            @Override
            public java.util.Optional<L> left() {
                return java.util.Optional.empty();
            }

            @Override
            public java.util.Optional<R> right() {
                return java.util.Optional.of(value);
            }

            @Override
            public String toString() {
                return "Right[" + value + "]";
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                final Right<?, ?> right = (Right<?, ?>) o;
                return Objects.equals(value, right.value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(value);
            }
        }

        private Either() {
        }

        public abstract <C, D> Either<C, D> mapBoth(final Function<? super L, ? extends C> f1, final Function<? super R, ? extends D> f2);

        public abstract <T> T map(final Function<? super L, ? extends T> l, Function<? super R, ? extends T> r);

        public abstract Either<L, R> ifLeft(final Consumer<? super L> consumer);

        public abstract Either<L, R> ifRight(final Consumer<? super R> consumer);

        public abstract java.util.Optional<L> left();

        public abstract java.util.Optional<R> right();

        public <T> Either<T, R> mapLeft(final Function<? super L, ? extends T> l) {
            return map(t -> left(l.apply(t)), Either::right);
        }

        public <T> Either<L, T> mapRight(final Function<? super R, ? extends T> l) {
            return map(Either::left, t -> right(l.apply(t)));
        }

        public static <L, R> Either<L, R> left(final L value) {
            return new Left<>(value);
        }

        public static <L, R> Either<L, R> right(final R value) {
            return new Right<>(value);
        }

        public L orThrow() {
            return map(l -> l, r -> {
                if (r instanceof Throwable) {
                    throw new RuntimeException((Throwable) r);
                }
                throw new RuntimeException(r.toString());
            });
        }

        public Either<R, L> swap() {
            return map(Either::right, Either::left);
        }

        public <L2> Either<L2, R> flatMap(final Function<L, Either<L2, R>> function) {
            return map(function, Either::right);
        }
    }
}
