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
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.recipes.map.*;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.*;
import gregtech.common.ConfigHolder;
import gregtech.integration.GroovyScriptCompat;
import gregtech.integration.VirtualizedRecipeMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    public static final IChanceFunction DEFAULT_CHANCE_FUNCTION = (baseChance, boostPerTier, baseTier, machineTier) -> {
        int tierDiff = machineTier - baseTier;
        if (tierDiff <= 0) return baseChance; // equal or invalid tiers do not boost at all
        if (baseTier == GTValues.ULV) tierDiff--; // LV does not boost over ULV
        return baseChance + (boostPerTier * tierDiff);
    };

    public IChanceFunction chanceFunction = DEFAULT_CHANCE_FUNCTION;

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

    private final VirtualizedRecipeMap virtualizedRecipeMap;
    private final Branch lookup = new Branch();
    private boolean hasOreDictedInputs = false;
    private boolean hasNBTMatcherInputs = false;
    private static final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> ingredientRoot = new WeakHashMap<>();
    private final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> fluidIngredientRoot = new WeakHashMap<>();


    private Consumer<RecipeBuilder<?>> onRecipeBuildAction;
    protected SoundEvent sound;
    private RecipeMap<?> smallRecipeMap;

    public RecipeMap(String unlocalizedName, int minInputs, int maxInputs, int minOutputs, int maxOutputs, int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs, R defaultRecipe, boolean isHidden) {
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

        if (Loader.isModLoaded(GTValues.MODID_GROOVYSCRIPT)) {
            this.virtualizedRecipeMap = GroovyScriptCompat.isLoaded() ? new VirtualizedRecipeMap(this) : null;
        } else {
            this.virtualizedRecipeMap = null;
        }
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
            GTLog.logger.error("Error happened during processing ore registration of prefix {} and material {}. " + "Seems like cross-mod compatibility issue. Report to GTCEu github.", currentOrePrefix, currentMaterial);
        }
    }

    public RecipeMap<R> setProgressBar(TextureArea progressBar, MoveType moveType) {
        this.progressBarTexture = progressBar;
        this.moveType = moveType;
        return this;
    }

    public RecipeMap<R> setSlotOverlay(boolean isOutput, boolean isFluid, TextureArea slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true, slotOverlay);
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

        if (recipe.isGroovyRecipe()) {
            this.virtualizedRecipeMap.addScripted(recipe);
        }
        compileRecipe(recipe);

    }

    public void compileRecipe(Recipe recipe) {
        if (recipe == null) {
            return;
        }
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe);
        recurseIngredientTreeAdd(recipe, items, lookup, 0, 0);
    }

    public boolean removeRecipe(Recipe recipe) {
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe);
        if (recurseIngredientTreeRemove(recipe, items, lookup, 0) != null) {
            if (GroovyScriptCompat.isCurrentlyRunning()) {
                this.virtualizedRecipeMap.addBackup(recipe);
            }
            return true;
        }
        return false;
    }

    protected ValidationResult<Recipe> postValidateRecipe(ValidationResult<Recipe> validationResult) {
        EnumValidationResult recipeStatus = validationResult.getType();
        Recipe recipe = validationResult.getResult();
        if (recipe.isGroovyRecipe()) {
            return validationResult;
        }
        if (!GTUtility.isBetweenInclusive(getMinInputs(), getMaxInputs(), recipe.getInputs().size())) {
            GTLog.logger.error("Invalid amount of recipe inputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getInputs().size(), getMinInputs(), getMaxInputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe inputs. Actual: %s. Should be between %s and %s inclusive.", recipe.getInputs().size(), getMinInputs(), getMaxInputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (!GTUtility.isBetweenInclusive(getMinOutputs(), getMaxOutputs(), recipe.getOutputs().size() + recipe.getChancedOutputs().size())) {
            GTLog.logger.error("Invalid amount of recipe outputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getOutputs().size() + recipe.getChancedOutputs().size(), getMinOutputs(), getMaxOutputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe outputs. Actual: %s. Should be between %s and %s inclusive.", recipe.getOutputs().size() + recipe.getChancedOutputs().size(), getMinOutputs(), getMaxOutputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (!GTUtility.isBetweenInclusive(getMinFluidInputs(), getMaxFluidInputs(), recipe.getFluidInputs().size())) {
            GTLog.logger.error("Invalid amount of recipe fluid inputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getFluidInputs().size(), getMinFluidInputs(), getMaxFluidInputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe fluid inputs. Actual: %s. Should be between %s and %s inclusive.", recipe.getFluidInputs().size(), getMinFluidInputs(), getMaxFluidInputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (!GTUtility.isBetweenInclusive(getMinFluidOutputs(), getMaxFluidOutputs(), recipe.getFluidOutputs().size())) {
            GTLog.logger.error("Invalid amount of recipe fluid outputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getFluidOutputs().size(), getMinFluidOutputs(), getMaxFluidOutputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Outputs"));
            if (recipe.getIsCTRecipe()) {
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
        return find(inputs.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList()), fluidInputs.stream().filter(Objects::nonNull).collect(Collectors.toList()), recipe -> {
            if (exactVoltage && recipe.getEUt() != voltage) {
                return false;
            }
            return recipe.getEUt() <= voltage && recipe.matches(false, inputs, fluidInputs);
        });
    }

    @Nullable
    public Recipe find(@Nonnull List<ItemStack> items, @Nonnull List<FluidStack> fluids, @Nonnull Predicate<Recipe> canHandle) {
        // First, check if items and fluids are valid.
        if (items.size() == Integer.MAX_VALUE || fluids.size() == Integer.MAX_VALUE) {
            return null;
        }
        if (items.size() == 0 && fluids.size() == 0) {
            return null;
        }

        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(items.size() + fluids.size());
        if (items.size() > 0) {
            buildFromItemStacks(list, uniqueItems(items));
        }
        if (fluids.size() > 0) {
            List<FluidStack> stack = new ObjectArrayList<>(fluids.size());
            for (FluidStack f : fluids) {
                if (f == null || f.amount == 0) {
                    continue;
                }
                stack.add(f);
            }
            if (stack.size() > 0) {
                buildFromFluidStacks(list, stack);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        return recurseIngredientTreeFindRecipe(list, lookup, canHandle);
    }

    /**
     * Builds a list of unique ItemStacks from the given Collection of ItemStacks.
     * Used to reduce the number inputs, if for example there is more than one of the same input,
     * pack them into one.
     * This uses a strict comparison, so it will not pack the same item with different NBT tags,
     * to allow the presence of, for example, more than one configured circuit in the input.
     * @param inputs The Collection of GTRecipeInputs.
     * @return an array of unique itemstacks.
     */

    public static ItemStack[] uniqueItems(Collection<ItemStack> inputs) {
        int index = 0;
        ItemStack[] uniqueItems = new ItemStack[inputs.size()];
        main: for (ItemStack input : inputs) {
            if (input.isEmpty()) {
                continue;
            }
            if (index > 0) {
                for (int i = 0; i < uniqueItems.length; i++) {
                    ItemStack unique = uniqueItems[i];
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
     * Used to reduce the number inputs, if for example there is more than one of the same input,
     * pack them into one.
     *
     * @param input The list of GTRecipeInputs.
     * @return The list of unique inputs.
     */

    public static List<GTRecipeInput> uniqueIngredientsList(List<GTRecipeInput> input) {
        List<GTRecipeInput> list = new ObjectArrayList<>(input.size());
        for (GTRecipeInput item : input) {
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
     * Recursively finds a recipe, top level. call this to find a recipe
     *
     * @param ingredients the ingredients part
     * @param branchRoot  the root branch to search from.
     * @return a recipe
     */
    private Recipe recurseIngredientTreeFindRecipe(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchRoot, @Nonnull Predicate<Recipe> canHandle) {
        // Try each ingredient as a starting point, adding it to the skiplist.
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
     * @param skip        bitmap of ingredients to skip, i.e. which ingredients are used in the
     *                    recursion.
     * @return a recipe
     */
    private Recipe recurseIngredientTreeFindRecipe(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchMap, @Nonnull Predicate<Recipe> canHandle, int index, int count, long skip) {
        if (count == ingredients.size()) {
            return null;
        }
        List<AbstractMapIngredient> wr = ingredients.get(index);
        // Iterate over current level of nodes.
        for (AbstractMapIngredient t : wr) {
            Map<AbstractMapIngredient, Either<Recipe, Branch>> targetMap;
            if (t.isSpecialIngredient()) {
                targetMap = branchMap.getSpecialNodes();
            } else {
                targetMap = branchMap.getNodes();
            }

            Either<Recipe, Branch> result = targetMap.get(t);
            if (result != null) {
                // Either return recipe or continue branch.
                Recipe r = result.map(recipe -> canHandle.test(recipe) ? recipe : null, right -> diveIngredientTreeFindRecipe(ingredients, right, canHandle, index, count, skip));
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private Recipe diveIngredientTreeFindRecipe(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch map, Predicate<Recipe> canHandle, int index, int count, long skip) {
        // We loop around ingredients.size() if we reach the end.
        int counter = (index + 1) % ingredients.size();
        while (counter != index) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << counter)) == 0)) {
                // Recursive call.
                Recipe found = recurseIngredientTreeFindRecipe(ingredients, map, canHandle, counter, count + 1, skip | (1L << counter));
                if (found != null) {
                    return found;
                }
            }
            counter = (counter + 1) % ingredients.size();
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
    public Set<Recipe> findRecipeCollisions(List<ItemStack> items, List<FluidStack> fluids) {
        // First, check if items and fluids are valid.
        if (items.size() == Integer.MAX_VALUE || fluids.size() == Integer.MAX_VALUE) {
            return null;
        }
        if (items.size() == 0 && fluids.size() == 0) {
            return null;
        }
        // Filter out empty fluids.

        // Build input.
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(items.size() + fluids.size());
        if (items.size() > 0) {
            buildFromItemStacks(list, uniqueItems(items));
        }
        if (fluids.size() > 0) {
            List<FluidStack> stack = new ObjectArrayList<>(fluids.size());
            for (FluidStack f : fluids) {
                if (f == null || f.amount == 0) {
                    continue;
                }
                stack.add(f);
            }
            if (stack.size() > 0) {
                buildFromFluidStacks(list, stack);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        Set<Recipe> collidingRecipes = new HashSet<>();
        return recurseIngredientTreeFindRecipeCollisions(list, lookup, collidingRecipes);
    }

    private Set<Recipe> recurseIngredientTreeFindRecipeCollisions(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchRoot, Set<Recipe> collidingRecipes) {
        // Try each ingredient as a starting point, adding it to the skiplist.
        for (int i = 0; i < ingredients.size(); i++) {
            recurseIngredientTreeFindRecipeCollisions(ingredients, branchRoot, i, 0, (1L << i), collidingRecipes);
        }
        return collidingRecipes;
    }

    private Recipe recurseIngredientTreeFindRecipeCollisions(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchMap, int index, int count, long skip, Set<Recipe> collidingRecipes) {
        if (count == ingredients.size()) {
            return null;
        }
        List<AbstractMapIngredient> wr = ingredients.get(index);
        // Iterate over current level of nodes.
        for (AbstractMapIngredient t : wr) {
            Map<AbstractMapIngredient, Either<Recipe, Branch>> targetMap;
            if (t.isSpecialIngredient()) {
                targetMap = branchMap.getSpecialNodes();
            } else {
                targetMap = branchMap.getNodes();
            }

            Either<Recipe, Branch> result = targetMap.get(t);
            if (result != null) {
                // Either return recipe or continue branch.
                Recipe r = result.map(recipe -> recipe, right -> diveIngredientTreeFindRecipeCollisions(ingredients, right, index, count, skip, collidingRecipes));
                if (r != null) {
                    collidingRecipes.add(r);
                }
            }
        }
        return null;
    }

    private Recipe diveIngredientTreeFindRecipeCollisions(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch map, int index, int count, long skip, Set<Recipe> collidingRecipes) {
        // We loop around ingredients.size() if we reach the end.
        int counter = (index + 1) % ingredients.size();
        while (counter != index) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << counter)) == 0)) {
                // Recursive call.
                Recipe r = recurseIngredientTreeFindRecipeCollisions(ingredients, map, counter, count + 1, skip | (1L << counter), collidingRecipes);
                if (r != null) {
                    return r;
                }
            }
            counter = (counter + 1) % ingredients.size();
        }
        return null;
    }

    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(200, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplateNoOutputs(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
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
            builder.widget(new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs).setBackgroundTexture(getOverlaysForSlot(isOutputs, false, slotIndex == itemHandler.getSlots() - 1)));
        } else {
            builder.widget(new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18).setAlwaysShowFull(true).setBackgroundTexture(getOverlaysForSlot(isOutputs, true, slotIndex == fluidHandler.getTanks() - 1)).setContainerClicking(true, !isOutputs));
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
     * @param branchMap   the current branch in the recursion.
     * @param index       where in the ingredients list we are.
     * @param count       how many added already.
     */
    boolean recurseIngredientTreeAdd(@Nonnull Recipe recipe, @Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchMap, int index, int count) {
        if (count >= ingredients.size()) return true;
        if (index >= ingredients.size()) {
            throw new RuntimeException("Index out of bounds for recurseItemTreeAdd, should not happen");
        }
        // Loop through NUMBER_OF_INGREDIENTS times.
        List<AbstractMapIngredient> current = ingredients.get(index);
        Either<Recipe, Branch> r;
        final Branch branchRight = new Branch();
        for (AbstractMapIngredient obj : current) {
            Map<AbstractMapIngredient, Either<Recipe, Branch>> targetMap;
            if (obj.isSpecialIngredient()) {
                targetMap = branchMap.getSpecialNodes();
            } else {
                targetMap = branchMap.getNodes();
            }

            // Either add the recipe or create a branch.
            r = targetMap.compute(obj, (k, v) -> {
                if (count == ingredients.size() - 1) {
                    if (v != null) {
                        if (v.left().isPresent() && v.left().get() == recipe) {
                            return v;
                        } else {
                            if (recipe.getIsCTRecipe()) {
                                CraftTweakerAPI.logError(String.format("Recipe: %s for Recipe Map %s is a duplicate and was not added", CTRecipeHelper.getRecipeAddLine(this, recipe), this.unlocalizedName));
                            }
                            if (ConfigHolder.misc.debug) {
                                GTLog.logger.warn("Recipe: {} for Recipe Map {} is a duplicate and was not added", recipe.toString(), this.unlocalizedName);
                            }
                        }
                    } else {
                        v = Either.left(recipe);
                    }
                    return v;
                } else if (v == null) {
                    v = Either.right(branchRight);
                }
                return v;
            });

            if (r.right().map(m -> !recurseIngredientTreeAdd(recipe, ingredients, m, (index + 1) % ingredients.size(), count + 1)).orElse(false)) {
                current.forEach(i -> {
                    if (count == ingredients.size() - 1) {
                        targetMap.remove(obj);
                    } else {
                        if (targetMap.get(obj).right().isPresent()) {
                            Branch branch = targetMap.get(obj).right().get();
                            if (branch.isEmptyBranch()) {
                                targetMap.remove(obj);
                            }
                        }
                    }
                });
                return false;
            }
        }
        return true;
    }

    protected void buildFromRecipeFluids(List<List<AbstractMapIngredient>> builder, List<GTRecipeInput> fluidInputs) {
        for (GTRecipeInput fluidInput : fluidInputs) {
            AbstractMapIngredient ingredient;
            ingredient = new MapFluidIngredient(fluidInput);
            WeakReference<AbstractMapIngredient> cached = fluidIngredientRoot.get(ingredient);
            if (cached != null && cached.get() != null) {
                builder.add(Collections.singletonList(cached.get()));
            } else {
                fluidIngredientRoot.put(ingredient, new WeakReference<>(ingredient));
                builder.add(Collections.singletonList(ingredient));
            }
        }
    }

    protected void buildFromFluidStacks(List<List<AbstractMapIngredient>> builder, List<FluidStack> ingredients) {
        for (FluidStack t : ingredients) {
            builder.add(Collections.singletonList(new MapFluidIngredient(t)));
        }
    }

    protected List<List<AbstractMapIngredient>> fromRecipe(Recipe r) {
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>((r.getInputs().size()) + r.getFluidInputs().size());
        if (r.getInputs().size() > 0) {
            buildFromRecipeItems(list, uniqueIngredientsList(r.getInputs()));
        }
        if (r.getFluidInputs().size() > 0) {
            buildFromRecipeFluids(list, r.getFluidInputs());
        }
        return list;
    }

    protected void buildFromRecipeItems(List<List<AbstractMapIngredient>> list, List<GTRecipeInput> ingredients) {
        for (GTRecipeInput r : ingredients) {
            AbstractMapIngredient ingredient;
            if (r.isOreDict()) {
                hasOreDictedInputs = true;
                if (r.hasNBTMatchingCondition()) {
                    hasNBTMatcherInputs = true;
                    ingredient = new MapOreDictNBTIngredient(r.getOreDict(), r.getNBTMatcher(), r.getNBTMatchingCondition());
                } else {
                    ingredient = new MapOreDictIngredient(r.getOreDict());
                }

                WeakReference<AbstractMapIngredient> cached = ingredientRoot.get(ingredient);
                if (cached != null && cached.get() != null) {
                    list.add(Collections.singletonList(cached.get()));
                } else {
                    ingredientRoot.put(ingredient, new WeakReference<>(ingredient));
                    list.add(Collections.singletonList(ingredient));
                }

            } else {
                List<AbstractMapIngredient> inner = new ObjectArrayList<>(1);
                if (r.hasNBTMatchingCondition()) {
                    inner.addAll(MapItemStackNBTIngredient.from(r));
                    hasNBTMatcherInputs = true;
                } else {
                    inner.addAll(MapItemStackIngredient.from(r));
                }

                for (int i = 0; i < inner.size(); i++) {
                    AbstractMapIngredient mappedIngredient = inner.get(i);
                    WeakReference<AbstractMapIngredient> cached = ingredientRoot.get(mappedIngredient);
                    if (cached != null && cached.get() != null) {
                        inner.set(i,cached.get());
                    } else {
                        ingredientRoot.put(mappedIngredient, new WeakReference<>(mappedIngredient));
                    }
                }
                list.add(inner);
            }
        }
    }

    protected void buildFromItemStacks(List<List<AbstractMapIngredient>> list, ItemStack[] ingredients) {
        AbstractMapIngredient ingredient;
        for (ItemStack stack : ingredients) {
            int meta = stack.getMetadata();
            NBTTagCompound nbt = stack.getTagCompound();

            List<AbstractMapIngredient> ls = new ObjectArrayList<>(1);
            ls.add(new MapItemStackIngredient(stack, meta, nbt));
            if (hasOreDictedInputs) {
                for (int i : OreDictionary.getOreIDs(stack)) {
                    ingredient = new MapOreDictIngredient(i);
                    ls.add(ingredient);
                    if (hasNBTMatcherInputs) {
                        ingredient = new MapOreDictNBTIngredient(i, nbt);
                        ls.add(ingredient);
                    }
                }
            }
            if (hasNBTMatcherInputs) {
                ls.add(new MapItemStackNBTIngredient(stack, meta, nbt));
            }
            if (ls.size() > 0) {
                list.add(ls);
            }
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
        return lookup.getRecipes(true).sorted(RECIPE_DURATION_THEN_EU).collect(Collectors.toList());
    }

    public SoundEvent getSound() {
        return sound;
    }

    @ZenMethod("findRecipe")
    @Method(modid = GTValues.MODID_CT)
    @Nullable
    public CTRecipe ctFindRecipe(long maxVoltage, IItemStack[] itemInputs, ILiquidStack[] fluidInputs, @Optional(valueLong = Integer.MAX_VALUE) int outputFluidTankCapacity) {
        List<ItemStack> mcItemInputs = itemInputs == null ? Collections.emptyList() : Arrays.stream(itemInputs).map(CraftTweakerMC::getItemStack).collect(Collectors.toList());
        List<FluidStack> mcFluidInputs = fluidInputs == null ? Collections.emptyList() : Arrays.stream(fluidInputs).map(CraftTweakerMC::getLiquidStack).collect(Collectors.toList());
        Recipe backingRecipe = findRecipe(maxVoltage, mcItemInputs, mcFluidInputs, outputFluidTankCapacity, true);
        return backingRecipe == null ? null : new CTRecipe(this, backingRecipe);
    }

    @ZenGetter("recipes")
    @Method(modid = GTValues.MODID_CT)
    public List<CTRecipe> ccGetRecipeList() {
        return getRecipeList().stream().map(recipe -> new CTRecipe(this, recipe)).collect(Collectors.toList());
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

    /**
     * Removes a recipe from the map. (recursive part)
     *
     * @param recipeToRemove the recipe to add.
     * @param ingredients    list of input ingredients.
     * @param branchMap      the current branch in the recursion.
     */
    private Recipe recurseIngredientTreeRemove(@Nonnull Recipe recipeToRemove, @Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchMap, int depth) {
        for (List<AbstractMapIngredient> current : ingredients) {
            for (AbstractMapIngredient obj : current) {
                Map<AbstractMapIngredient, Either<Recipe, Branch>> targetMap;
                if (obj.isSpecialIngredient()) {
                    targetMap = branchMap.getSpecialNodes();
                } else {
                    targetMap = branchMap.getNodes();
                }
                if (ingredients.size() == 0) return null;
                Recipe r = removeDive(recipeToRemove, ingredients.subList(1, ingredients.size()), targetMap, obj, depth);
                if (r != null) {
                    if (ingredients.size() == 1) {
                        targetMap.remove(obj);
                    } else {
                        if (targetMap.get(obj).right().isPresent()) {
                            Branch branch = targetMap.get(obj).right().get();
                            if (branch.isEmptyBranch()) {
                                targetMap.remove(obj);
                            }
                        }
                    }
                    return r;
                }
            }
        }
        return null;
    }

    private Recipe removeDive(Recipe recipeToRemove, @Nonnull List<List<AbstractMapIngredient>> ingredients, Map<AbstractMapIngredient, Either<Recipe, Branch>> targetMap, AbstractMapIngredient obj, int depth) {
        Either<Recipe, Branch> result = targetMap.get(obj);
        if (result != null) {
            // Either return recipe or continue branch.
            Recipe r = result.map(recipe -> recipe, right -> recurseIngredientTreeRemove(recipeToRemove, ingredients, right, depth + 1));
            if (r == recipeToRemove) {
                return r;
            }
        }
        return null;
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
        return "RecipeMap{" + "unlocalizedName='" + unlocalizedName + '\'' + '}';
    }

    @FunctionalInterface
    @ZenClass("mods.gregtech.recipe.IChanceFunction")
    @ZenRegister
    public interface IChanceFunction {

        /**
         *
         * @param baseChance the base chance of the recipe
         * @param boostPerTier the amount the chance is changed per tier over the base
         * @param baseTier the lowest tier used to obtain un-boosted chances
         * @param boostTier the tier the chance should be calculated at
         * @return the chance
         */
        int chanceFor(int baseChance, int boostPerTier, int baseTier, int boostTier);
    }

}
