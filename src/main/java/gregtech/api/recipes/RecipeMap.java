package gregtech.api.recipes;

import com.google.common.collect.ImmutableList;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.crafttweaker.CTRecipe;
import gregtech.api.recipes.crafttweaker.CTRecipeBuilder;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.ValidationResult;
import gregtech.integration.groovy.GroovyScriptCompat;
import gregtech.integration.groovy.VirtualizedRecipeMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.items.IItemHandlerModifiable;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.*;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

@ZenClass("mods.gregtech.recipe.RecipeMap")
@ZenRegister
public final class RecipeMap<R extends RecipeBuilder<R>> {

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

    private final RecipeMapFrontend frontend;
    private final RecipeMapBackend<R> backend;

    public IChanceFunction chanceFunction = DEFAULT_CHANCE_FUNCTION;

    public final String unlocalizedName;

    private final RecipeBuilder<R> defaultRecipeBuilder;
    private int maxInputs;
    private int maxOutputs;
    private int maxFluidInputs;
    private int maxFluidOutputs;
    private final boolean modifyItemInputs;
    private final boolean modifyItemOutputs;
    private final boolean modifyFluidInputs;
    private final boolean modifyFluidOutputs;

    private final Collection<Consumer<RecipeBuilder<R>>> buildActions;
    private RecipeMap<?> smallRecipeMap;

    /**
     * @param unlocalizedName      the unlocalized name for the RecipeMap
     * @param defaultRecipeBuilder the default RecipeBuilder for the RecipeMap
     * @param maxItemInputs        the maximum item inputs
     * @param modifyItemInputs     If the Maximum Item Inputs should be able to be modified
     * @param maxItemOutputs       the maximum item outputs
     * @param modifyItemOutputs    If the Maximum Item Outputs should be able to be modified
     * @param maxFluidInputs       the maximum fluid inputs
     * @param modifyFluidInputs    If the Maximum Fluid Inputs should be able to be modified
     * @param maxFluidOutputs      the maximum fluid outputs
     * @param modifyFluidOutputs   If the Maximum Fluid Outputs should be able to be modified
     * @param buildActions         actions to be performed upon recipe build
     * @param frontend                   the GUI for the recipemap
     * @param backend              the backend for the recipemap
     */
    public RecipeMap(@Nonnull String unlocalizedName, @Nonnull RecipeBuilder<R> defaultRecipeBuilder,
                     @Nonnegative int maxItemInputs, boolean modifyItemInputs,
                     @Nonnegative int maxItemOutputs, boolean modifyItemOutputs,
                     @Nonnegative int maxFluidInputs, boolean modifyFluidInputs,
                     @Nonnegative int maxFluidOutputs, boolean modifyFluidOutputs,
                     @Nonnull Collection<Consumer<RecipeBuilder<R>>> buildActions,
                     @Nonnull RecipeMapFrontend frontend, @Nonnull RecipeMapBackend<R> backend) {
        this.unlocalizedName = unlocalizedName;
        defaultRecipeBuilder.setRecipeMap(this);
        this.defaultRecipeBuilder = defaultRecipeBuilder;
        this.maxInputs = maxItemInputs;
        this.modifyItemInputs = modifyItemInputs;
        this.maxOutputs = maxItemOutputs;
        this.modifyItemOutputs = modifyItemOutputs;
        this.maxFluidInputs = maxFluidInputs;
        this.modifyFluidInputs = modifyFluidInputs;
        this.maxFluidOutputs = maxFluidOutputs;
        this.modifyFluidOutputs = modifyFluidOutputs;
        this.buildActions = buildActions;
        this.frontend = frontend;
        this.backend = backend;
        if (GroovyScriptCompat.isLoaded()) {
            this.backend.setVirtualizedRecipeMap(new VirtualizedRecipeMap(this));
        }

        RECIPE_MAP_REGISTRY.put(unlocalizedName, this);
    }

    /**
     * Performs additional validation of recipes before adding to the ingredient tree.
     *
     * @param validationResult the current validation result
     * @return the new result based on validation
     */
    @Nonnull
    private ValidationResult<Recipe> postValidateRecipe(@Nonnull ValidationResult<Recipe> validationResult) {
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
        boolean emptyOutputs = recipe.getOutputs().isEmpty() && recipe.getFluidOutputs().isEmpty() && recipe.getChancedOutputs().isEmpty();
        if (recipe.getEUt() > 0 && emptyOutputs) {
            GTLog.logger.error("Invalid amount of recipe outputs. Recipe outputs are empty.");
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError("Invalid amount of outputs inputs. Recipe outputs are empty.");
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (emptyInputs && emptyOutputs) {
            GTLog.logger.error("Invalid recipe. Inputs and Outputs are empty.");
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Recipe is empty"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError("Invalid recipe. Inputs and Outputs are empty.");
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Recipe is empty"));
            }
        }

        int amount = recipe.getInputs().size();
        if (amount > getMaxInputs()) {
            GTLog.logger.error("Invalid amount of recipe inputs. Actual: {}. Should be at most {}.", amount, getMaxInputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe inputs. Actual: %s. Should be at most %s.", amount, getMaxInputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }

        amount = recipe.getOutputs().size() + recipe.getChancedOutputs().size();
        if (amount > getMaxOutputs()) {
            GTLog.logger.error("Invalid amount of recipe outputs. Actual: {}. Should be at most {}.", amount, getMaxOutputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe outputs. Actual: %s. Should be at most %s.", amount, getMaxOutputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }

        amount = recipe.getFluidInputs().size();
        if (amount > getMaxFluidInputs()) {
            GTLog.logger.error("Invalid amount of recipe fluid inputs. Actual: {}. Should be at most {}.", amount, getMaxFluidInputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe fluid inputs. Actual: %s. Should be at most %s.", amount, getMaxFluidInputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }

        amount = recipe.getFluidOutputs().size();
        if (amount > getMaxFluidOutputs()) {
            GTLog.logger.error("Invalid amount of recipe fluid outputs. Actual: {}. Should be at most {}.", amount, getMaxFluidOutputs());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Outputs"));
            if (recipe.getIsCTRecipe()) {
                CraftTweakerAPI.logError(String.format("Invalid amount of recipe fluid outputs. Actual: %s. Should be at most %s.", amount, getMaxFluidOutputs()));
                CraftTweakerAPI.logError("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Outputs"));
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        return ValidationResult.newResult(recipeStatus, recipe);
    }

    /**
     * Internal usage <strong>only</strong>, only call from {@link RecipeBuilder#buildAndRegister()}.
     *
     * @param validationResult the validation result from building the recipe
     */
    public void addRecipe(@Nonnull ValidationResult<Recipe> validationResult) {
        validationResult = this.postValidateRecipe(validationResult);
        switch (validationResult.getType()) {
            case SKIP:
                return;
            case INVALID:
                RecipeMapBackend.setFoundInvalidRecipe(true);
                return;
        }
        this.backend.addRecipe(validationResult.getResult());
    }

    /**
     * Internal usage <strong>only</strong>, only call from {@link VirtualizedRecipeMap#onReload()}.
     * Compiles a recipe and adds it to the ingredient tree
     *
     * @param recipe the recipe to compile
     */
    public void compileRecipe(@Nullable Recipe recipe) {
        this.backend.compileRecipe(recipe);
    }

    /**
     * @param recipe the recipe to remove
     * @return if removal was successful
     */
    public boolean removeRecipe(@Nonnull Recipe recipe) {
        return this.backend.removeRecipe(recipe);
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
        return backend.findRecipe(voltage, inputs, fluidInputs);
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
        return backend.findRecipe(voltage, inputs, fluidInputs);
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
    public Recipe findRecipe(long voltage, final List<ItemStack> inputs, final List<FluidStack> fluidInputs, boolean exactVoltage) {
        return backend.findRecipe(voltage, inputs, fluidInputs);
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
        return backend.findRecipeCollisions(items, fluids);
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

    @ZenMethod("setChanceFunction")
    public void setChanceFunction(IChanceFunction function) {
        chanceFunction = function;
    }

    public void setSmallRecipeMap(@Nullable RecipeMap<?> recipeMap) {
        this.smallRecipeMap = recipeMap;
    }

    @Nullable
    public RecipeMap<?> getSmallRecipeMap() {
        return smallRecipeMap;
    }

    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        return frontend.createJeiUITemplate(importItems, exportItems, importFluids, exportFluids, yOffset);
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        return frontend.createUITemplate(progressSupplier, importItems, exportItems, importFluids, exportFluids, yOffset);
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplateNoOutputs(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        return frontend.createUITemplateNoOutputs(progressSupplier, importItems, exportItems, importFluids, exportFluids, yOffset);
    }

    public void setProgressBar(@Nonnull TextureArea progressBar, @Nonnull ProgressWidget.MoveType moveType) {
        frontend.setProgressBar(progressBar, moveType);
    }

    public void setSpecialTexture(int x, int y, int width, int height, @Nonnull TextureArea area) {
        frontend.setSpecialTexture(x, y, width, height, area);
    }

    public void setSound(@Nullable SoundEvent sound) {
        frontend.setSound(sound);
    }

    @Nullable
    public SoundEvent getSound() {
        return frontend.getSound();
    }

    public boolean isVisible() {
        return frontend.isVisible();
    }

    @Nonnull
    public Collection<Recipe> getRecipeList() {
        ObjectOpenHashSet<Recipe> recipes = new ObjectOpenHashSet<>();
        return this.backend.getRecipes(true)
                .filter(recipes::add)
                .sorted(RECIPE_DURATION_THEN_EU)
                .collect(Collectors.toList());
    }

    @ZenMethod("findRecipe")
    @Method(modid = GTValues.MODID_CT)
    @Nullable
    public CTRecipe ctFindRecipe(long maxVoltage, IItemStack[] itemInputs, ILiquidStack[] fluidInputs, @Optional(valueLong = Integer.MAX_VALUE) int outputFluidTankCapacity) {
        List<ItemStack> mcItemInputs = itemInputs == null ? Collections.emptyList() : Arrays.stream(itemInputs).map(CraftTweakerMC::getItemStack).collect(Collectors.toList());
        List<FluidStack> mcFluidInputs = fluidInputs == null ? Collections.emptyList() : Arrays.stream(fluidInputs).map(CraftTweakerMC::getLiquidStack).collect(Collectors.toList());
        Recipe backingRecipe = this.backend.findRecipe(maxVoltage, mcItemInputs, mcFluidInputs, true);
        return backingRecipe == null ? null : new CTRecipe(this, backingRecipe);
    }

    @ZenGetter("recipes")
    @Method(modid = GTValues.MODID_CT)
    public List<CTRecipe> ctGetRecipeList() {
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
        return defaultRecipeBuilder.copy().onBuild(this.buildActions);
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
    public void setMaxInputs(@Nonnegative int maxInputs) {
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
    public void setMaxOutputs(@Nonnegative int maxOutputs) {
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
    public void setMaxFluidInputs(@Nonnegative int maxFluidInputs) {
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
    public void setMaxFluidOutputs(@Nonnegative int maxFluidOutputs) {
        if (modifyFluidOutputs) {
            this.maxFluidOutputs = Math.max(this.maxFluidOutputs, maxFluidOutputs);
        } else {
            throw new UnsupportedOperationException("Cannot change max fluid output amount for " + getUnlocalizedName());
        }
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
         * @param baseChance   the base chance of the recipe
         * @param boostPerTier the amount the chance is changed per tier over the base
         * @param baseTier     the lowest tier used to obtain un-boosted chances
         * @param boostTier    the tier the chance should be calculated at
         * @return the chance
         */
        int chanceFor(int baseChance, int boostPerTier, int baseTier, int boostTier);
    }
}
