package gregtech.api.recipes;

import gregtech.api.recipes.category.GTRecipeCategory;
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
import gregtech.api.recipes.roll.ListWithRollInformation;
import gregtech.integration.groovy.GroovyScriptModule;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
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

    private final @NotNull ListWithRollInformation<GTItemIngredient> itemIngredients;
    private final @NotNull ListWithRollInformation<GTFluidIngredient> fluidIngredients;
    public final long ingredientFlags;
    private final @NotNull ItemOutputProvider itemOutputProvider;
    private final @NotNull FluidOutputProvider fluidOutputProvider;

    private final int duration;

    /**
     * If this Recipe is hidden from JEI
     */
    private final boolean hidden;
    private final GTRecipeCategory recipeCategory;

    private final boolean groovyRecipe;
    private final RecipePropertyStorage recipePropertyStorage;

    // equals() and hashCode() helpers //
    private final Object2ByteOpenHashMap<GTItemIngredient> equalityItemFrequencyMap;
    private final Object2ByteOpenHashMap<GTFluidIngredient> equalityFluidFrequencyMap;
    private final int hashCode;

    public Recipe(@NotNull ListWithRollInformation<GTItemIngredient> itemIngredients,
                  @NotNull ListWithRollInformation<GTFluidIngredient> fluidIngredients,
                  @NotNull ItemOutputProvider itemOutputProvider,
                  @NotNull FluidOutputProvider fluidOutputProvider,
                  @NotNull RecipePropertyStorage recipePropertyStorage,
                  int duration, boolean hidden,
                  @NotNull GTRecipeCategory recipeCategory) {
        int ingredients = itemIngredients.size() + fluidIngredients.size();
        if (ingredients > 64) throw new IllegalArgumentException(
                "Recipe Search cannot support more than 64 item and fluid inputs to a recipe!");
        else this.ingredientFlags = (1L << ingredients) - 1;
        this.recipePropertyStorage = recipePropertyStorage;
        this.itemIngredients = itemIngredients;
        this.fluidIngredients = fluidIngredients;
        this.itemOutputProvider = itemOutputProvider;
        this.fluidOutputProvider = fluidOutputProvider;
        this.duration = duration;
        this.hidden = hidden;
        this.recipeCategory = recipeCategory;
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
                this.fluidOutputProvider, this.recipePropertyStorage, this.duration, this.hidden,
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
                .append("GSRecipe", groovyRecipe)
                .toString();
    }

    ///////////////////
    // Getters //
    ///////////////////

    public @NotNull ListWithRollInformation<GTItemIngredient> getItemIngredients() {
        return itemIngredients;
    }

    public @NotNull ListWithRollInformation<GTFluidIngredient> getFluidIngredients() {
        return fluidIngredients;
    }

    public @NotNull ItemOutputProvider getItemOutputProvider() {
        return itemOutputProvider;
    }

    public @NotNull FluidOutputProvider getFluidOutputProvider() {
        return fluidOutputProvider;
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

    public ChancedOutputList<ItemStack, ChancedItemOutput> getChancedOutputs() {
        return null;
    }

    public ChancedOutputList<FluidStack, ChancedFluidOutput> getChancedFluidOutputs() {
        return null;
    }

    public List<FluidStack> getFluidOutputs() {
        return null;
    }
}
