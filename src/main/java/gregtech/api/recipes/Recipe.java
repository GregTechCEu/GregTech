package gregtech.api.recipes;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.IFluidIngredient;
import gregtech.api.recipes.ingredients.IItemIngredient;
import gregtech.api.recipes.ingredients.old.GTRecipeInput;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.recipes.properties.RecipePropertyStorageImpl;
import gregtech.api.recipes.properties.impl.PowerUsageProperty;
import gregtech.api.recipes.properties.impl.PowerGenerationProperty;
import gregtech.api.recipes.properties.impl.PowerPropertyData;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.integration.groovy.GroovyScriptModule;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

    private final List<IItemIngredient> itemIngredients;
    private final Object2ByteOpenHashMap<IItemIngredient> equalityItemFrequencyMap;
    private final List<IFluidIngredient> fluidIngredients;
    private final Object2ByteOpenHashMap<IFluidIngredient> equalityFluidFrequencyMap;
    public final long ingredientFlags;
    private final List<ItemStack> outputs;

    /**
     * A chance of 10000 equals 100%
     */
    private final ChancedOutputList<ItemStack, ChancedItemOutput> chancedOutputs;
    private final List<FluidStack> fluidOutputs;
    private final ChancedOutputList<FluidStack, ChancedFluidOutput> chancedFluidOutputs;

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

    public Recipe(@NotNull List<IItemIngredient> itemIngredients,
                  @NotNull List<IFluidIngredient> fluidIngredients,
                  List<ItemStack> outputs,
                  @NotNull ChancedOutputList<ItemStack, ChancedItemOutput> chancedOutputs,
                  List<FluidStack> fluidOutputs,
                  @NotNull ChancedOutputList<FluidStack, ChancedFluidOutput> chancedFluidOutputs,
                  int duration,
                  boolean hidden,
                  boolean isCTRecipe,
                  @NotNull RecipePropertyStorage recipePropertyStorage,
                  @NotNull GTRecipeCategory recipeCategory) {
        int ingredients = itemIngredients.size() + fluidIngredients.size();
        if (ingredients > 64) throw new IllegalArgumentException("Recipe Search cannot support more than 64 item and fluid inputs to a recipe!");
        else this.ingredientFlags = 1L << ingredients;
        this.recipePropertyStorage = recipePropertyStorage;
        this.itemIngredients = itemIngredients;
        if (outputs.isEmpty()) {
            this.outputs = Collections.emptyList();
        } else {
            this.outputs = new ArrayList<>(outputs);
        }
        this.chancedOutputs = chancedOutputs;
        this.chancedFluidOutputs = chancedFluidOutputs;
        this.fluidIngredients = fluidIngredients;
        this.fluidOutputs = fluidOutputs.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(fluidOutputs);
        this.duration = duration;
        this.hidden = hidden;
        this.recipeCategory = recipeCategory;
        this.isCTRecipe = isCTRecipe;
        this.hashCode = makeHashCode();
        this.groovyRecipe = GroovyScriptModule.isCurrentlyRunning();
        this.equalityItemFrequencyMap = new Object2ByteOpenHashMap<>();
        for (IItemIngredient ingredient : this.itemIngredients) {
            this.equalityItemFrequencyMap.merge(ingredient, (byte) 1, (a, b) -> (byte) (a + b));
        }
        this.equalityFluidFrequencyMap = new Object2ByteOpenHashMap<>();
        for (IFluidIngredient ingredient : this.fluidIngredients) {
            this.equalityFluidFrequencyMap.merge(ingredient, (byte) 1, (a, b) -> (byte) (a + b));
        }
    }

    @NotNull
    public Recipe copy() {
        return new Recipe(this.itemIngredients, this.fluidIngredients, this.outputs, this.chancedOutputs,
                this.fluidOutputs, this.chancedFluidOutputs, this.duration,
                this.hidden, this.isCTRecipe, this.recipePropertyStorage, this.recipeCategory);
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
        // Fast return early if no trimming desired
        if (itemTrimLimit == -1 && fluidTrimLimit == -1) {
            return currentRecipe;
        }

        currentRecipe = currentRecipe.copy();
        RecipeBuilder<?> builder = new RecipeBuilder<>(currentRecipe, recipeMap);

        builder.clearOutputs();
        builder.clearChancedOutput();
        builder.clearFluidOutputs();
        builder.clearChancedFluidOutputs();

        // Chanced outputs are removed in this if they cannot fit the limit
        Pair<List<ItemStack>, List<ChancedItemOutput>> recipeOutputs = currentRecipe
                .getItemAndChanceOutputs(itemTrimLimit);

        // Add the trimmed chanced outputs and outputs
        builder.chancedOutputs(recipeOutputs.getRight());
        builder.outputs(recipeOutputs.getLeft());

        Pair<List<FluidStack>, List<ChancedFluidOutput>> recipeFluidOutputs = currentRecipe
                .getFluidAndChanceOutputs(fluidTrimLimit);

        // Add the trimmed fluid outputs
        builder.chancedFluidOutputs(recipeFluidOutputs.getRight());
        builder.fluidOutputs(recipeFluidOutputs.getLeft());

        return builder.build().getResult();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return this.equalityItemFrequencyMap.equals(recipe.equalityItemFrequencyMap) && this.equalityFluidFrequencyMap.equals(recipe.equalityFluidFrequencyMap);
    }

    private int makeHashCode() {
        int hash = 0;
        for (IItemIngredient ingredient : itemIngredients) {
            hash = 31 * hash + ingredient.hashCode();
        }
        for (IFluidIngredient ingredient : fluidIngredients) {
            hash = 31 * hash + ingredient.hashCode();
        }
        return hash;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    private boolean hasSameInputs(Recipe otherRecipe) {
        List<ItemStack> otherStackList = new ObjectArrayList<>(otherRecipe.inputs.size());
        for (GTRecipeInput otherInputs : otherRecipe.inputs) {
            otherStackList.addAll(Arrays.asList(otherInputs.getInputStacks()));
        }
        if (!this.matchesItems(otherStackList).getLeft()) {
            return false;
        }

        List<ItemStack> thisStackList = new ObjectArrayList<>(this.inputs.size());
        for (GTRecipeInput thisInputs : this.inputs) {
            thisStackList.addAll(Arrays.asList(thisInputs.getInputStacks()));
        }
        return otherRecipe.matchesItems(thisStackList).getLeft();
    }

    public static int hashFluidList(@NotNull List<GTRecipeInput> fluids) {
        int hash = 0;
        for (GTRecipeInput fluidInput : fluids) {
            hash = 31 * hash + fluidInput.hashCode();
        }
        return hash;
    }

    private boolean hasSameFluidInputs(Recipe otherRecipe) {
        List<FluidStack> otherFluidList = new ObjectArrayList<>(otherRecipe.fluidInputs.size());
        for (GTRecipeInput otherInputs : otherRecipe.fluidInputs) {
            FluidStack fluidStack = otherInputs.getInputFluidStack();
            otherFluidList.add(fluidStack);
        }
        if (!this.matchesFluid(otherFluidList).getLeft()) {
            return false;
        }

        List<FluidStack> thisFluidsList = new ObjectArrayList<>(this.fluidInputs.size());
        for (GTRecipeInput thisFluidInputs : this.fluidInputs) {
            FluidStack fluidStack = thisFluidInputs.getInputFluidStack();
            thisFluidsList.add(fluidStack);
        }
        return otherRecipe.matchesFluid(thisFluidsList).getLeft();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("inputs", inputs)
                .append("outputs", outputs)
                .append("chancedOutputs", chancedOutputs)
                .append("fluidInputs", fluidInputs)
                .append("fluidOutputs", fluidOutputs)
                .append("duration", duration)
                .append("hidden", hidden)
                .append("CTRecipe", isCTRecipe)
                .append("GSRecipe", groovyRecipe)
                .toString();
    }

    ///////////////////
    // Getters //
    ///////////////////

    public List<GTRecipeInput> getInputs() {
        return inputs;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    // All Recipes this method is called for should be already trimmed, if required

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
        List<ItemStack> outputs = new ArrayList<>(getOutputs());
        ChanceBoostFunction function = recipeMap.getChanceFunction();
        List<ChancedItemOutput> chancedOutputsList = getChancedOutputs().roll(function, recipeTier, machineTier);

        if (chancedOutputsList == null) return outputs;

        Collection<ItemStack> resultChanced = new ArrayList<>();
        for (ChancedItemOutput chancedOutput : chancedOutputsList) {
            ItemStack stackToAdd = chancedOutput.getIngredient().copy();
            for (ItemStack stackInList : resultChanced) {
                int insertable = stackInList.getMaxStackSize() - stackInList.getCount();
                if (insertable > 0 && ItemHandlerHelper.canItemStacksStack(stackInList, stackToAdd)) {
                    if (insertable >= stackToAdd.getCount()) {
                        stackInList.grow(stackToAdd.getCount());
                        stackToAdd = ItemStack.EMPTY;
                        break;
                    } else {
                        stackInList.grow(insertable);
                        stackToAdd.shrink(insertable);
                    }
                }
            }
            if (!stackToAdd.isEmpty()) {
                resultChanced.add(stackToAdd);
            }
        }

        outputs.addAll(resultChanced);

        return outputs;
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
        List<ItemStack> outputs = new ArrayList<>();

        // Create an entry for the chanced outputs, and initially populate it
        List<ChancedItemOutput> chancedOutputs = new ArrayList<>(getChancedOutputs().getChancedEntries());

        // No limiting
        if (outputLimit == -1) {
            outputs.addAll(GTUtility.copyStackList(getOutputs()));
        }
        // If just the regular outputs would satisfy the outputLimit
        else if (getOutputs().size() >= outputLimit) {
            outputs.addAll(
                    GTUtility.copyStackList(getOutputs()).subList(0, Math.min(outputLimit, getOutputs().size())));
            // clear the chanced outputs, as we are only getting regular outputs
            chancedOutputs.clear();
        }
        // If the regular outputs and chanced outputs are required to satisfy the outputLimit
        else if (!getOutputs().isEmpty() && (getOutputs().size() + chancedOutputs.size()) >= outputLimit) {
            outputs.addAll(GTUtility.copyStackList(getOutputs()));

            // Calculate the number of chanced outputs after adding all the regular outputs
            int numChanced = outputLimit - getOutputs().size();

            chancedOutputs = chancedOutputs.subList(0, Math.min(numChanced, chancedOutputs.size()));
        }
        // There are only chanced outputs to satisfy the outputLimit
        else if (getOutputs().isEmpty()) {
            chancedOutputs = chancedOutputs.subList(0, Math.min(outputLimit, chancedOutputs.size()));
        }
        // The number of outputs + chanced outputs is lower than the trim number, so just add everything
        else {
            outputs.addAll(GTUtility.copyStackList(getOutputs()));
            // Chanced outputs are taken care of in the original copy
        }

        return Pair.of(outputs, chancedOutputs);
    }

    /**
     * Returns a list of every possible ItemStack output from a recipe, including all possible chanced outputs.
     *
     * @return A List of ItemStack outputs from the recipe, including all chanced outputs
     */
    public List<ItemStack> getAllItemOutputs() {
        List<ItemStack> recipeOutputs = new ArrayList<>(this.outputs);

        for (ChancedItemOutput entry : this.chancedOutputs.getChancedEntries()) {
            recipeOutputs.add(entry.getIngredient().copy());
        }

        return recipeOutputs;
    }

    public ChancedOutputList<ItemStack, ChancedItemOutput> getChancedOutputs() {
        return chancedOutputs;
    }

    public List<GTRecipeInput> getFluidInputs() {
        return fluidInputs;
    }

    public ChancedOutputList<FluidStack, ChancedFluidOutput> getChancedFluidOutputs() {
        return chancedFluidOutputs;
    }

    public boolean hasInputFluid(FluidStack fluid) {
        for (GTRecipeInput fluidInput : fluidInputs) {
            FluidStack fluidStack = fluidInput.getInputFluidStack();
            if (fluid.getFluid() == fluidStack.getFluid()) {
                return fluidStack.isFluidEqual(fluid);
            }
        }
        return false;
    }

    public List<FluidStack> getFluidOutputs() {
        return fluidOutputs;
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
        List<FluidStack> outputs = new ArrayList<>();

        // Create an entry for the chanced outputs, and initially populate it
        List<ChancedFluidOutput> chancedOutputs = new ArrayList<>(getChancedFluidOutputs().getChancedEntries());

        // No limiting
        if (outputLimit == -1) {
            outputs.addAll(GTUtility.copyFluidList(getFluidOutputs()));
        }
        // If just the regular outputs would satisfy the outputLimit
        else if (getFluidOutputs().size() >= outputLimit) {
            outputs.addAll(
                    GTUtility.copyFluidList(getFluidOutputs()).subList(0,
                            Math.min(outputLimit, getFluidOutputs().size())));
            // clear the chanced outputs, as we are only getting regular outputs
            chancedOutputs.clear();
        }
        // If the regular outputs and chanced outputs are required to satisfy the outputLimit
        else if (!getFluidOutputs().isEmpty() && (getFluidOutputs().size() + chancedOutputs.size()) >= outputLimit) {
            outputs.addAll(GTUtility.copyFluidList(getFluidOutputs()));

            // Calculate the number of chanced outputs after adding all the regular outputs
            int numChanced = outputLimit - getFluidOutputs().size();

            chancedOutputs = chancedOutputs.subList(0, Math.min(numChanced, chancedOutputs.size()));
        }
        // There are only chanced outputs to satisfy the outputLimit
        else if (getFluidOutputs().isEmpty()) {
            chancedOutputs = chancedOutputs.subList(0, Math.min(outputLimit, chancedOutputs.size()));
        }
        // The number of outputs + chanced outputs is lower than the trim number, so just add everything
        else {
            outputs.addAll(GTUtility.copyFluidList(getFluidOutputs()));
            // Chanced outputs are taken care of in the original copy
        }

        return Pair.of(outputs, chancedOutputs);
    }

    /**
     * Returns a list of every possible FluidStack output from a recipe, including all possible chanced outputs.
     *
     * @return A List of FluidStack outputs from the recipe, including all chanced outputs
     */
    public List<FluidStack> getAllFluidOutputs() {
        List<FluidStack> recipeOutputs = new ArrayList<>(this.fluidOutputs);

        for (ChancedFluidOutput entry : this.chancedFluidOutputs.getChancedEntries()) {
            recipeOutputs.add(entry.getIngredient().copy());
        }

        return recipeOutputs;
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
        List<FluidStack> outputs = new ArrayList<>(GTUtility.copyFluidList(getFluidOutputs()));

        ChanceBoostFunction function = recipeMap.getChanceFunction();
        List<ChancedFluidOutput> chancedOutputsList = getChancedFluidOutputs().roll(function, recipeTier, machineTier);

        if (chancedOutputsList == null) return outputs;

        Collection<FluidStack> resultChanced = new ArrayList<>();
        for (ChancedFluidOutput chancedOutput : chancedOutputsList) {
            FluidStack stackToAdd = chancedOutput.getIngredient().copy();
            for (FluidStack stackInList : resultChanced) {
                int insertable = stackInList.amount;
                if (insertable > 0 && stackInList.getFluid() == stackToAdd.getFluid()) {
                    stackInList.amount += stackToAdd.amount;
                    stackToAdd = null;
                    break;
                }
            }
            if (stackToAdd != null) {
                resultChanced.add(stackToAdd);
            }
        }

        outputs.addAll(resultChanced);

        return outputs;
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

    public boolean hasValidInputsForDisplay() {
        for (GTRecipeInput ingredient : inputs) {
            if (ingredient.isOreDict()) {
                if (OreDictionary.getOres(OreDictionary.getOreName(ingredient.getOreDict())).stream()
                        .anyMatch(s -> !s.isEmpty())) {
                    return true;
                }
            } else if (Arrays.stream(ingredient.getInputStacks()).anyMatch(s -> !s.isEmpty())) {
                return true;
            }
        }
        for (GTRecipeInput fluidInput : fluidInputs) {
            FluidStack fluidIngredient = fluidInput.getInputFluidStack();
            if (fluidIngredient != null && fluidIngredient.amount > 0) {
                return true;
            }
        }
        return false;
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
}
