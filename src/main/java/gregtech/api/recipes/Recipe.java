package gregtech.api.recipes;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.chance.BaseChanceEntry;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTFluidIngredient;
import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.api.recipes.output.FluidOutputProvider;
import gregtech.api.recipes.output.ItemOutputProvider;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.recipes.properties.RecipePropertyStorageImpl;
import gregtech.api.recipes.properties.impl.CircuitProperty;
import gregtech.api.recipes.properties.impl.PowerGenerationProperty;
import gregtech.api.recipes.properties.impl.PowerPropertyData;
import gregtech.api.recipes.properties.impl.PowerUsageProperty;
import gregtech.integration.groovy.GroovyScriptModule;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Class that represent machine recipe.
 * <p>
 * <p>
 * Recipes are created using {@link RecipeBuilder} or its subclasses in builder-alike pattern. To get RecipeBuilder use
 * {@link RecipeMap#recipeBuilder()}.
 * <p>
 * <p>
 * Example:
 * RecipeMap.POLARIZER_RECIPES.recipeBuilder().inputs(new ItemStack(Items.APPLE)).outputs(new
 * ItemStack(Items.GOLDEN_APPLE)).duration(256).EUt(480).buildAndRegister();
 * <p>
 * This will create and register Polarizer recipe with Apple as input and Golden apple as output, duration - 256 ticks
 * and energy consumption of 480 EU/t.
 * <p>
 * To get example for particular RecipeMap see {@link RecipeMap}
 * <p>
 * <p>
 * Recipes are immutable.
 */
public class Recipe {

    private final List<GTItemIngredient> itemIngredients;
    private final Object2ByteOpenHashMap<GTItemIngredient> equalityItemFrequencyMap;
    private final List<GTFluidIngredient> fluidIngredients;
    private final Object2ByteOpenHashMap<GTFluidIngredient> equalityFluidFrequencyMap;
    public final long ingredientFlags;
    private final ItemOutputProvider itemOutputProvider;
    private final FluidOutputProvider fluidOutputProvider;

    private final int duration;

    /**
     * If this Recipe is hidden from JEI
     */
    private final boolean hidden;
    private final GTRecipeCategory recipeCategory;

    /**
     * If this Recipe is a Crafttweaker recipe. Used for logging purposes
     */
    // TODO YEET
    private final boolean isCTRecipe;
    private final boolean groovyRecipe;
    private final RecipePropertyStorage recipePropertyStorage;

    private final int hashCode;

    public Recipe(@NotNull List<GTItemIngredient> itemIngredients,
                  @NotNull List<GTFluidIngredient> fluidIngredients,
                  @NotNull ItemOutputProvider itemOutputProvider,
                  @NotNull FluidOutputProvider fluidOutputProvider,
                  int duration,
                  boolean hidden,
                  boolean isCTRecipe,
                  @NotNull RecipePropertyStorage recipePropertyStorage,
                  @NotNull GTRecipeCategory recipeCategory) {
        int ingredients = itemIngredients.size() + fluidIngredients.size();
        if (ingredients > 64) throw new IllegalArgumentException(
                "Recipe Search cannot support more than 64 item and fluid inputs to a recipe!");
        else this.ingredientFlags = 1L << ingredients;
        this.recipePropertyStorage = recipePropertyStorage;
        this.itemIngredients = itemIngredients;
        this.fluidIngredients = fluidIngredients;
        this.itemOutputProvider = itemOutputProvider;
        this.fluidOutputProvider = fluidOutputProvider;
        this.duration = duration;
        this.hidden = hidden;
        this.recipeCategory = recipeCategory;
        this.isCTRecipe = isCTRecipe;
        this.hashCode = makeHashCode();
        this.groovyRecipe = GroovyScriptModule.isCurrentlyRunning();
        this.equalityItemFrequencyMap = new Object2ByteOpenHashMap<>();
        for (GTItemIngredient ingredient : this.itemIngredients) {
            this.equalityItemFrequencyMap.merge(ingredient, (byte) 1, (a, b) -> (byte) (a + b));
        }
        this.equalityFluidFrequencyMap = new Object2ByteOpenHashMap<>();
        for (GTFluidIngredient ingredient : this.fluidIngredients) {
            this.equalityFluidFrequencyMap.merge(ingredient, (byte) 1, (a, b) -> (byte) (a + b));
        }
    }

    @NotNull
    public Recipe copy() {
        return new Recipe(this.itemIngredients, this.fluidIngredients, this.itemOutputProvider,
                this.fluidOutputProvider, this.duration, this.hidden, this.isCTRecipe, this.recipePropertyStorage,
                this.recipeCategory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return this.equalityItemFrequencyMap.equals(recipe.equalityItemFrequencyMap) &&
                this.equalityFluidFrequencyMap.equals(recipe.equalityFluidFrequencyMap) &&
                this.getCircuit() == recipe.getCircuit();
    }

    private int makeHashCode() {
        int hash = getCircuit();
        for (GTItemIngredient ingredient : itemIngredients) {
            hash = 31 * hash + ingredient.hashCode();
        }
        for (GTFluidIngredient ingredient : fluidIngredients) {
            hash = 31 * hash + ingredient.hashCode();
        }
        return hash;
    }

    /**
     * @return {@link Integer#MIN_VALUE} if no circuit was found.
     */
    public int getCircuit() {
        Byte fetch = propertyStorage().get(CircuitProperty.getInstance(), null);
        if (fetch == null) return Integer.MIN_VALUE;
        else return fetch;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("inputs", itemIngredients)
                .append("fluidInputs", fluidIngredients)
                .append("outputs", itemOutputProvider)
                .append("fluidOutputs", fluidOutputProvider)
                .append("duration", duration)
                .append("hidden", hidden)
                .append("CTRecipe", isCTRecipe)
                .append("GSRecipe", groovyRecipe)
                .toString();
    }

    ///////////////////
    // Getters //
    ///////////////////

    public List<GTItemIngredient> getItemIngredients() {
        return itemIngredients;
    }

    public List<GTFluidIngredient> getFluidIngredients() {
        return fluidIngredients;
    }

    public ItemOutputProvider getItemOutputProvider() {
        return itemOutputProvider;
    }

    public FluidOutputProvider getFluidOutputProvider() {
        return fluidOutputProvider;
    }

    /**
     * Returns a list of every possible ItemStack output from a recipe, including all possible chanced outputs.
     *
     * @return A List of ItemStack outputs from the recipe, including all chanced outputs
     */
    public List<ItemStack> getMaximumItemOutputs(List<ItemStack> inputItems, List<FluidStack> inputFluids) {
        var complete = getItemOutputProvider().getCompleteOutputs(inputItems, inputFluids);
        List<ItemStack> list = new ObjectArrayList<>(complete.getLeft().size() + complete.getRight().size());
        list.addAll(complete.getLeft());
        complete.getRight().stream().map(BaseChanceEntry::getIngredient).forEach(list::add);
        return list;
    }

    /**
     * Returns a list of every possible FluidStack output from a recipe, including all possible chanced outputs.
     *
     * @return A List of FluidStack outputs from the recipe, including all chanced outputs
     */
    public List<FluidStack> getMaximumFluidOutputs(List<ItemStack> inputItems, List<FluidStack> inputFluids) {
        var complete = getFluidOutputProvider().getCompleteOutputs(inputItems, inputFluids);
        List<FluidStack> list = new ObjectArrayList<>(complete.getLeft().size() + complete.getRight().size());
        list.addAll(complete.getLeft());
        complete.getRight().stream().map(BaseChanceEntry::getIngredient).forEach(list::add);
        return list;
    }

    public int getDuration() {
        return duration;
    }

    /**
     * @deprecated use {@link #getVoltage()} instead.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public long getEUt() {
        return getVoltage();
    }

    public long getVoltage() {
        PowerPropertyData generation = recipePropertyStorage.get(PowerGenerationProperty.getInstance(), null);
        if (generation != null) return generation.getVoltage();
        return recipePropertyStorage.get(PowerUsageProperty.getInstance(), PowerPropertyData.EMPTY).getVoltage();
    }

    public long getAmperage() {
        PowerPropertyData generation = recipePropertyStorage.get(PowerGenerationProperty.getInstance(), null);
        if (generation != null) return generation.getAmperage();
        return recipePropertyStorage.get(PowerUsageProperty.getInstance(), PowerPropertyData.EMPTY).getAmperage();
    }

    public boolean isGenerating() {
        return recipePropertyStorage.contains(PowerGenerationProperty.getInstance());
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean getIsCTRecipe() {
        return isCTRecipe;
    }

    public boolean isGroovyRecipe() {
        return groovyRecipe;
    }

    /**
     * @return whether this recipe is valid or has bad inputs/outputs
     */
    public boolean isValid() {
        for (GTItemIngredient ingredient : itemIngredients) {
            if (!ingredient.isValid()) return false;
        }
        for (GTFluidIngredient ingredient : fluidIngredients) {
            if (!ingredient.isValid()) return false;
        }
        return itemOutputProvider.isValid() && fluidOutputProvider.isValid();
    }

    @NotNull
    public GTRecipeCategory getRecipeCategory() {
        return this.recipeCategory;
    }

    ///////////////////////////////////////////////////////////
    // Property Helper Methods //
    ///////////////////////////////////////////////////////////

    /**
     * @see RecipePropertyStorageImpl#get(RecipeProperty, Object)
     */
    @Contract("_, !null -> !null")
    public <T> @Nullable T getProperty(@NotNull RecipeProperty<T> property, @Nullable T defaultValue) {
        return recipePropertyStorage.get(property, defaultValue);
    }

    public RecipePropertyStorage getRecipePropertyStorage() {
        return recipePropertyStorage;
    }

    /**
     * @see RecipePropertyStorageImpl#contains(RecipeProperty)
     */
    public boolean hasProperty(@NotNull RecipeProperty<?> property) {
        return recipePropertyStorage.contains(property);
    }

    /**
     * @return the property storage
     */
    public @NotNull RecipePropertyStorage propertyStorage() {
        return recipePropertyStorage;
    }

    // TODO

    /**
     * Returns a list of every possible ItemStack output from a recipe, including all possible chanced outputs.
     *
     * @return A List of ItemStack outputs from the recipe, including all chanced outputs
     */
    public List<ItemStack> getAllItemOutputs() {
        return null;
    }

    /**
     * Returns a list of every possible FluidStack output from a recipe, including all possible chanced outputs.
     *
     * @return A List of FluidStack outputs from the recipe, including all chanced outputs
     */
    public List<FluidStack> getAllFluidOutputs() {
        return null;
    }

    /**
     * Trims the recipe outputs, chanced outputs, and fluid outputs based on the performing MetaTileEntity's trim limit.
     *
     * @param currentRecipe  The recipe to perform the output trimming upon
     * @param recipeMap      The RecipeMap that the recipe is from
     * @param itemTrimLimit  The Limit to which item outputs should be trimmed to, -1 for no trimming
     * @param fluidTrimLimit The Limit to which fluid outputs should be trimmed to, -1 for no trimming
     * @return A new Recipe whose outputs have been trimmed.
     */
    public static Recipe trimRecipeOutputs(Recipe currentRecipe, RecipeMap<?> recipeMap, int itemTrimLimit,
                                           int fluidTrimLimit) {
        return null;
    }

    public final boolean matches(boolean consumeIfSuccessful, IItemHandlerModifiable inputs,
                                 IMultipleTankHandler fluidInputs) {
        return false;
    }

    /**
     * This methods aim to verify if the current recipe matches the given inputs according to matchingMode mode.
     *
     * @param consumeIfSuccessful if true will consume the inputs of the recipe.
     * @param inputs              Items input or Collections.emptyList() if none.
     * @param fluidInputs         Fluids input or Collections.emptyList() if none.
     * @return true if the recipe matches the given inputs false otherwise.
     */
    public boolean matches(boolean consumeIfSuccessful, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
        return false;
    }

    public List<ItemStack> getOutputs() {
        return null;
    }

    /**
     * Returns all outputs from the recipe.
     * This is where Chanced Outputs for the recipe are calculated.
     * The Recipe should be trimmed by calling {@link Recipe#getItemAndChanceOutputs(int)} before calling this method,
     * if trimming is required.
     *
     * @param recipeTier  The Voltage Tier of the Recipe, used for chanced output calculation
     * @param machineTier The Voltage Tier of the Machine, used for chanced output calculation
     * @param recipeMap   The RecipeMap that the recipe is being performed upon, used for chanced output calculation
     * @return A list of all resulting ItemStacks from the recipe, after chance has been applied to any chanced outputs
     */
    public List<ItemStack> getResultItemOutputs(int recipeTier, int machineTier, RecipeMap<?> recipeMap) {
        return null;
    }

    /**
     * Returns the maximum possible recipe outputs from a recipe, divided into regular and chanced outputs
     * Takes into account any specific output limiters, ie macerator slots, to trim down the output list
     * Trims from chanced outputs first, then regular outputs
     *
     * @param outputLimit The limit on the number of outputs, -1 for disabled.
     * @return A Pair of recipe outputs and chanced outputs, limited by some factor
     */
    public Pair<List<ItemStack>, List<ChancedItemOutput>> getItemAndChanceOutputs(int outputLimit) {
        return null;
    }

    public ChancedOutputList<ItemStack, ChancedItemOutput> getChancedOutputs() {
        return null;
    }

    public ChancedOutputList<FluidStack, ChancedFluidOutput> getChancedFluidOutputs() {
        return null;
    }

    public List<FluidStack> getFluidOutputs() {
        return null;
    }

    /**
     * Returns the maximum possible recipe outputs from a recipe, divided into regular and chanced outputs
     * Takes into account any specific output limiters, ie macerator slots, to trim down the output list
     * Trims from chanced outputs first, then regular outputs
     *
     * @param outputLimit The limit on the number of outputs, -1 for disabled.
     * @return A Pair of recipe outputs and chanced outputs, limited by some factor
     */
    public Pair<List<FluidStack>, List<ChancedFluidOutput>> getFluidAndChanceOutputs(int outputLimit) {
        return null;
    }

    /**
     * Returns all outputs from the recipe.
     * This is where Chanced Outputs for the recipe are calculated.
     * The Recipe should be trimmed by calling {@link Recipe#getFluidAndChanceOutputs(int)} before calling this method,
     * if trimming is required.
     *
     * @param recipeTier  The Voltage Tier of the Recipe, used for chanced output calculation
     * @param machineTier The Voltage Tier of the Machine, used for chanced output calculation
     * @param recipeMap   The RecipeMap that the recipe is being performed upon, used for chanced output calculation
     * @return A list of all resulting ItemStacks from the recipe, after chance has been applied to any chanced outputs
     */
    public List<FluidStack> getResultFluidOutputs(int recipeTier, int machineTier, RecipeMap<?> recipeMap) {
        return null;
    }
}
