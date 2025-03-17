package gregtech.api.recipes;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.recipes.buildaction.RecipeBuildAction;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.ingredients.match.IngredientMatchHelper;
import gregtech.api.recipes.lookup.AbstractRecipeLookup;
import gregtech.api.recipes.lookup.CompactibleIterator;
import gregtech.api.recipes.lookup.RecipeLookup;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.ui.RecipeMapUI;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.Mods;
import gregtech.api.util.ValidationResult;
import gregtech.integration.groovy.GroovyScriptModule;
import gregtech.integration.groovy.VirtualizedRecipeMap;
import gregtech.modules.GregTechModules;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ZenClass("mods.gregtech.recipe.RecipeMap")
@ZenRegister
public class RecipeMap<R extends RecipeBuilder<R>> {

    private static final Map<String, RecipeMap<?>> RECIPE_MAP_REGISTRY = new Object2ReferenceOpenHashMap<>();

    private static boolean foundInvalidRecipe = false;

    protected RecipeMapUI<?> recipeMapUI;

    public final String unlocalizedName;

    private final R recipeBuilderSample;
    private int maxInputs;
    private int maxOutputs;
    private int maxFluidInputs;
    private int maxFluidOutputs;

    private boolean allowEmptyInput;
    private boolean allowEmptyOutput;

    private final Object grsVirtualizedRecipeMap;
    protected final @NotNull AbstractRecipeLookup lookup;

    private final Map<GTRecipeCategory, List<Recipe>> recipeByCategory = new Object2ObjectOpenHashMap<>();

    private final Map<ResourceLocation, RecipeBuildAction<R>> recipeBuildActions = new Object2ObjectOpenHashMap<>();
    protected @Nullable SoundEvent sound;
    private @Nullable RecipeMap<?> smallRecipeMap;

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
     *
     * @deprecated {@link RecipeMap#RecipeMap(String, R, RecipeMapUIFunction, int, int, int, int)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
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
     *
     * @deprecated {@link RecipeMap#RecipeMap(String, R, RecipeMapUIFunction, int, int, int, int)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    public RecipeMap(@NotNull String unlocalizedName,
                     int maxInputs, boolean modifyItemInputs,
                     int maxOutputs, boolean modifyItemOutputs,
                     int maxFluidInputs, boolean modifyFluidInputs,
                     int maxFluidOutputs, boolean modifyFluidOutputs,
                     @NotNull R defaultRecipeBuilder,
                     boolean isHidden) {
        this.unlocalizedName = unlocalizedName;

        this.maxInputs = maxInputs;
        this.maxFluidInputs = maxFluidInputs;
        this.maxOutputs = maxOutputs;
        this.maxFluidOutputs = maxFluidOutputs;

        defaultRecipeBuilder.setRecipeMap(this);
        defaultRecipeBuilder
                .category(GTRecipeCategory.create(GTValues.MODID, unlocalizedName, getTranslationKey(), this));
        this.recipeBuilderSample = defaultRecipeBuilder;

        this.recipeMapUI = new RecipeMapUI<>(this, modifyItemInputs, modifyItemOutputs, modifyFluidInputs,
                modifyFluidOutputs, false);
        this.recipeMapUI.setJEIVisible(!isHidden);

        RECIPE_MAP_REGISTRY.put(unlocalizedName, this);

        this.grsVirtualizedRecipeMap = GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_GRS) ?
                new VirtualizedRecipeMap(this) : null;
        this.lookup = new RecipeLookup();
    }

    /**
     * Create and register new instance of RecipeMap with specified properties.
     *
     * @param unlocalizedName      the unlocalized name for the RecipeMap
     * @param defaultRecipeBuilder the default RecipeBuilder for the RecipeMap
     * @param recipeMapUI          the ui to represent this recipemap
     * @param maxInputs            the maximum item inputs
     * @param maxOutputs           the maximum item outputs
     * @param maxFluidInputs       the maximum fluid inputs
     * @param maxFluidOutputs      the maximum fluid outputs
     */
    public RecipeMap(@NotNull String unlocalizedName, @NotNull R defaultRecipeBuilder,
                     @NotNull RecipeMapUIFunction recipeMapUI, int maxInputs, int maxOutputs, int maxFluidInputs,
                     int maxFluidOutputs) {
        this(unlocalizedName, defaultRecipeBuilder, recipeMapUI, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs,
                null);
    }

    /**
     * Create and register new instance of RecipeMap with specified properties.
     *
     * @param unlocalizedName      the unlocalized name for the RecipeMap
     * @param defaultRecipeBuilder the default RecipeBuilder for the RecipeMap
     * @param recipeMapUI          the ui to represent this recipemap
     * @param maxInputs            the maximum item inputs
     * @param maxOutputs           the maximum item outputs
     * @param maxFluidInputs       the maximum fluid inputs
     * @param maxFluidOutputs      the maximum fluid outputs
     * @param lookup               the recipe lookup to use
     */
    public RecipeMap(@NotNull String unlocalizedName, @NotNull R defaultRecipeBuilder,
                     @NotNull RecipeMapUIFunction recipeMapUI, int maxInputs, int maxOutputs, int maxFluidInputs,
                     int maxFluidOutputs, @Nullable AbstractRecipeLookup lookup) {
        this.lookup = lookup == null ? new RecipeLookup() : lookup;
        this.unlocalizedName = unlocalizedName;
        this.recipeMapUI = recipeMapUI.apply(this);

        this.maxInputs = maxInputs;
        this.maxFluidInputs = maxFluidInputs;
        this.maxOutputs = maxOutputs;
        this.maxFluidOutputs = maxFluidOutputs;

        defaultRecipeBuilder.setRecipeMap(this);
        defaultRecipeBuilder
                .category(GTRecipeCategory.create(GTValues.MODID, unlocalizedName, getTranslationKey(), this));
        this.recipeBuilderSample = defaultRecipeBuilder;
        RECIPE_MAP_REGISTRY.put(unlocalizedName, this);

        this.grsVirtualizedRecipeMap = GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_GRS) ?
                new VirtualizedRecipeMap(this) : null;
    }

    @ZenMethod
    public static List<RecipeMap<? extends RecipeBuilder<?>>> getRecipeMaps() {
        return ImmutableList.copyOf(RECIPE_MAP_REGISTRY.values());
    }

    @ZenMethod
    public static RecipeMap<? extends RecipeBuilder<?>> getByName(String unlocalizedName) {
        return RECIPE_MAP_REGISTRY.get(unlocalizedName);
    }

    public AbstractRecipeLookup getLookup() {
        return lookup;
    }

    public static boolean isFoundInvalidRecipe() {
        return foundInvalidRecipe;
    }

    public static void setFoundInvalidRecipe(boolean foundInvalidRecipe) {
        RecipeMap.foundInvalidRecipe = RecipeMap.foundInvalidRecipe || foundInvalidRecipe;
        OrePrefix currentOrePrefix = OrePrefix.getCurrentProcessingPrefix();
        if (currentOrePrefix != null) {
            Material currentMaterial = OrePrefix.getCurrentMaterial();
            GTLog.logger.error(
                    "Error happened during processing ore registration of prefix {} and material {}. " +
                            "Seems like cross-mod compatibility issue. Report to GTCEu github.",
                    currentOrePrefix, currentMaterial);
        }
    }

    /**
     * @deprecated {@link RecipeMapUI#setProgressBar(TextureArea, MoveType)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    public RecipeMap<R> setProgressBar(TextureArea progressBar, MoveType moveType) {
        this.recipeMapUI.setProgressBar(progressBar, moveType);
        return this;
    }

    /**
     * @deprecated {@link RecipeMapUI#setItemSlotOverlay(TextureArea, boolean, boolean)}
     *             {@link RecipeMapUI#setFluidSlotOverlay(TextureArea, boolean, boolean)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    public RecipeMap<R> setSlotOverlay(boolean isOutput, boolean isFluid, TextureArea slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true,
                slotOverlay);
    }

    /**
     * @deprecated {@link RecipeMapUI#setItemSlotOverlay(TextureArea, boolean, boolean)}
     *             {@link RecipeMapUI#setFluidSlotOverlay(TextureArea, boolean, boolean)}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    public RecipeMap<R> setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, TextureArea slotOverlay) {
        if (isFluid) {
            this.recipeMapUI.setFluidSlotOverlay(slotOverlay, isOutput, isLast);
        } else {
            this.recipeMapUI.setItemSlotOverlay(slotOverlay, isOutput, isLast);
        }
        return this;
    }

    public RecipeMap<R> setSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Add a recipe build action to be performed upon this RecipeMap's builder's recipe registration.
     *
     * @param name   the unique name of the action
     * @param action the action to perform
     * @return this
     */
    public RecipeMap<R> onRecipeBuild(@NotNull ResourceLocation name, @NotNull RecipeBuildAction<R> action) {
        if (recipeBuildActions.containsKey(name)) {
            throw new IllegalArgumentException("Cannot register RecipeBuildAction with duplicate name: " + name);
        }
        recipeBuildActions.put(name, action);
        return this;
    }

    /**
     * @param name the name of the build action to remove
     */
    public void removeBuildAction(@NotNull ResourceLocation name) {
        recipeBuildActions.remove(name);
    }

    /**
     * Add a recipe build action to be performed upon this RecipeMap's builder's recipe registration.
     *
     * @param actions the actions to perform
     */
    @ApiStatus.Internal
    protected void onRecipeBuild(@NotNull Map<ResourceLocation, RecipeBuildAction<R>> actions) {
        recipeBuildActions.putAll(actions);
    }

    /**
     * @return the build actions for this RecipeMap's default RecipeBuilder
     */
    @ApiStatus.Internal
    protected @UnmodifiableView @NotNull Map<ResourceLocation, RecipeBuildAction<R>> getBuildActions() {
        return this.recipeBuildActions;
    }

    public RecipeMap<R> allowEmptyInput() {
        this.allowEmptyInput = true;
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

    public RecipeMap<? extends RecipeBuilder<?>> getSmallRecipeMap() {
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
        recipeByCategory.computeIfAbsent(recipe.getRecipeCategory(), c -> new ObjectArrayList<>()).add(recipe);
        return lookup.addRecipe(recipe);
    }

    /**
     * @param recipe the recipe to remove
     * @return if removal was successful
     */
    public boolean removeRecipe(@NotNull Recipe recipe) {
        return lookup.removeRecipe(recipe);
    }

    /**
     * Removes all recipes.
     *
     * @see GTRecipeHandler#removeAllRecipes(RecipeMap)
     */
    @ApiStatus.Internal
    protected void removeAllRecipes() {
        if (GroovyScriptModule.isCurrentlyRunning()) {
            this.lookup.getPendingRecipes().forEach(this.getGroovyScriptRecipeMap()::addBackup);
        }
        this.lookup.clear();
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

        boolean emptyInputs = !this.allowEmptyInput && recipe.getItemIngredients().isEmpty() &&
                recipe.getFluidIngredients().isEmpty();
        if (emptyInputs) {
            GTLog.logger.error("Invalid amount of recipe inputs. Recipe inputs are empty.", new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }
        boolean emptyOutputs = !this.allowEmptyOutput && recipe.getItemOutputProvider().getMaximumOutputs(1) == 0 &&
                recipe.getFluidOutputProvider().getMaximumOutputs(1) == 0;
        if (emptyOutputs) {
            GTLog.logger.error("Invalid amount of recipe outputs. Recipe outputs are empty.", new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }

        int amount = recipe.getItemIngredients().size();
        if (amount > getMaxInputs()) {
            GTLog.logger.error("Invalid amount of recipe inputs. Actual: {}. Should be at most {}.", amount,
                    getMaxInputs(), new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }

        amount = recipe.getItemOutputProvider().getMaximumOutputs(1);
        if (amount > getMaxOutputs()) {
            GTLog.logger.error("Invalid amount of recipe outputs. Actual: {}. Should be at most {}.", amount,
                    getMaxOutputs(), new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }

        amount = recipe.getFluidIngredients().size();
        if (amount > getMaxFluidInputs()) {
            GTLog.logger.error("Invalid amount of recipe fluid inputs. Actual: {}. Should be at most {}.", amount,
                    getMaxFluidInputs(), new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }

        amount = recipe.getFluidOutputProvider().getMaximumOutputs(1);
        if (amount > getMaxFluidOutputs()) {
            GTLog.logger.error("Invalid amount of recipe fluid outputs. Actual: {}. Should be at most {}.", amount,
                    getMaxFluidOutputs(), new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }
        return ValidationResult.newResult(recipeStatus, recipe);
    }

    public CompactibleIterator<Recipe> findRecipes(IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs,
                                                   PropertySet propertySet) {
        return lookup.findRecipes(GTUtility.itemHandlerToList(inputs), GTUtility.fluidHandlerToList(fluidInputs),
                propertySet);
    }

    @NotNull
    public CompactibleIterator<Recipe> findRecipes(List<ItemStack> itemInputs, List<FluidStack> fluidInputs,
                                                   PropertySet propertySet) {
        return lookup.findRecipes(itemInputs, fluidInputs, propertySet);
    }

    @Nullable
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public Recipe findRecipe(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
        CompactibleIterator<Recipe> iter = findRecipes(inputs, fluidInputs, PropertySet.empty().supply(voltage, 1));
        if (!iter.hasNext()) return null;
        else return iter.next();
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
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
        CompactibleIterator<Recipe> iter = findRecipes(inputs, fluidInputs, PropertySet.empty().supply(voltage, 1));
        if (!iter.hasNext()) return null;
        else return iter.next();
    }

    /**
     * Finds a Recipe matching the Fluid and/or ItemStack Inputs.
     *
     * @param voltage            Voltage of the Machine or Long.MAX_VALUE if it has no Voltage
     * @param inputs             the Item Inputs
     * @param fluidInputs        the Fluid Inputs
     * @param propertylessSearch whether the recipe search should only care about item and fluid matching,
     *                           and do full count match testing.
     *                           If voltage is greater than zero, exact voltage matching is also applied.
     * @return the Recipe it has found or null for no matching Recipe
     */
    @Nullable
    @ApiStatus.Obsolete
    public Recipe findRecipe(long voltage, final List<ItemStack> inputs, final List<FluidStack> fluidInputs,
                             boolean propertylessSearch) {
        final List<ItemStack> items = inputs.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
        final List<FluidStack> fluids = fluidInputs.stream().filter(f -> f != null && f.amount != 0)
                .collect(Collectors.toList());

        CompactibleIterator<Recipe> iter = findRecipes(items, fluids,
                propertylessSearch ? null : PropertySet.empty().supply(voltage, 1));

        if (!propertylessSearch) {
            if (!iter.hasNext()) return null;
            else return iter.next();
        } else {
            while (iter.hasNext()) {
                Recipe recipe = iter.next();
                if ((voltage < 0 || recipe.getVoltage() == voltage) && checkSatisfaction(items, fluids, recipe))
                    return recipe;
            }
            return null;
        }
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
    public Recipe find(@NotNull List<ItemStack> items, @NotNull List<FluidStack> fluids,
                       @NotNull Predicate<Recipe> canHandle) {
        CompactibleIterator<Recipe> iter = findRecipes(items, fluids, null);

        while (iter.hasNext()) {
            Recipe next = iter.next();
            if (canHandle.test(next) && checkSatisfaction(items, fluids, next)) return next;
        }
        return null;
    }

    protected static boolean checkSatisfaction(@NotNull List<ItemStack> items, @NotNull List<FluidStack> fluids,
                                               @NotNull Recipe recipe) {
        return IngredientMatchHelper.matchItems(recipe.getItemIngredients(), items).attemptScale(1) &&
                IngredientMatchHelper.matchFluids(recipe.getFluidIngredients(), fluids).attemptScale(1);
    }

    @Method(modid = Mods.Names.GROOVY_SCRIPT)
    private VirtualizedRecipeMap getGroovyScriptRecipeMap() {
        return ((VirtualizedRecipeMap) grsVirtualizedRecipeMap);
    }

    @NotNull
    @UnmodifiableView
    public Collection<Recipe> getRecipeList() {
        return lookup.getPendingRecipes();
    }

    public @Nullable SoundEvent getSound() {
        return sound;
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
        return recipeBuilderSample.copy();
    }

    @ZenGetter("maxInputs")
    public int getMaxInputs() {
        return maxInputs;
    }

    @ZenSetter("maxInputs")
    public void setMaxInputs(int maxInputs) {
        this.maxInputs = Math.max(this.maxInputs, maxInputs);
        if (!recipeMapUI.canModifyItemInputs()) {
            GTLog.logger.warn(
                    "RecipeMap {} ui does not support changing max item inputs. Replace with a supporting UI for proper behavior.",
                    getUnlocalizedName(), new Throwable());
        }
    }

    @ZenGetter("maxOutputs")
    public int getMaxOutputs() {
        return maxOutputs;
    }

    @ZenSetter("maxOutputs")
    public void setMaxOutputs(int maxOutputs) {
        this.maxOutputs = Math.max(this.maxOutputs, maxOutputs);
        if (!recipeMapUI.canModifyItemOutputs()) {
            GTLog.logger.warn(
                    "RecipeMap {} ui does not support changing max item outputs. Replace with a supporting UI for proper behavior.",
                    getUnlocalizedName(), new Throwable());
        }
    }

    @ZenGetter("maxFluidInputs")
    public int getMaxFluidInputs() {
        return maxFluidInputs;
    }

    @ZenSetter("maxFluidInputs")
    public void setMaxFluidInputs(int maxFluidInputs) {
        this.maxFluidInputs = Math.max(this.maxFluidInputs, maxFluidInputs);
        if (!recipeMapUI.canModifyFluidInputs()) {
            GTLog.logger.warn(
                    "RecipeMap {} ui does not support changing max fluid inputs. Replace with a supporting UI for proper behavior.",
                    getUnlocalizedName(), new Throwable());
        }
    }

    @ZenGetter("maxFluidOutputs")
    public int getMaxFluidOutputs() {
        return maxFluidOutputs;
    }

    @ZenSetter("maxFluidOutputs")
    public void setMaxFluidOutputs(int maxFluidOutputs) {
        this.maxFluidOutputs = Math.max(this.maxFluidOutputs, maxFluidOutputs);
        if (!recipeMapUI.canModifyFluidOutputs()) {
            GTLog.logger.warn(
                    "RecipeMap {} ui does not support changing max fluid outputs. Replace with a supporting UI for proper behavior.",
                    getUnlocalizedName(), new Throwable());
        }
    }

    /**
     * <strong>This is not suitable for Recipe Lookup.</strong>
     * Use {@link #findRecipe(long, List, List)} instead.
     *
     * @return the recipes stored by category.
     */
    @NotNull
    @UnmodifiableView
    public Map<GTRecipeCategory, List<Recipe>> getRecipesByCategory() {
        return recipeByCategory;
    }

    /**
     * @return the current ui for the recipemap
     */
    public @NotNull RecipeMapUI<?> getRecipeMapUI() {
        return recipeMapUI;
    }

    /**
     * @param recipeMapUI the recipemap ui to set
     */
    public void setRecipeMapUI(@NotNull RecipeMapUI<?> recipeMapUI) {
        if (this.recipeMapUI.recipeMap() != recipeMapUI.recipeMap()) {
            throw new IllegalArgumentException("RecipeMap UI RecipeMap '" + recipeMapUI.recipeMap().unlocalizedName +
                    "' does not match this RecipeMap '" + this.unlocalizedName + "'");
        }
        this.recipeMapUI = recipeMapUI;
    }

    @Override
    @ZenMethod
    public String toString() {
        return "RecipeMap{" + unlocalizedName + '}';
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
