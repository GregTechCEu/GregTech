package gregtech.api.recipes;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.recipeproperties.EmptyRecipePropertyStorage;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.integration.GroovyScriptCompat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Class that represent machine recipe.<p>
 * <p>
 * Recipes are created using {@link RecipeBuilder} or its subclasses in builder-alike pattern. To get RecipeBuilder use {@link RecipeMap#recipeBuilder()}.<p>
 * <p>
 * Example:
 * RecipeMap.POLARIZER_RECIPES.recipeBuilder().inputs(new ItemStack(Items.APPLE)).outputs(new ItemStack(Items.GOLDEN_APPLE)).duration(256).EUt(480).buildAndRegister();<p>
 * This will create and register Polarizer recipe with Apple as input and Golden apple as output, duration - 256 ticks and energy consumption of 480 EU/t.<p>
 * To get example for particular RecipeMap see {@link RecipeMap}<p>
 * <p>
 * Recipes are immutable.
 */
public class Recipe {

    private static final NonNullList<ItemStack> EMPTY = NonNullList.create();

    public static int getMaxChancedValue() {
        return 10000;
    }

    public static String formatChanceValue(int outputChance) {
        return String.format("%.2f", outputChance / (getMaxChancedValue() * 1.0) * 100);
    }

    private final List<GTRecipeInput> inputs;
    private final NonNullList<ItemStack> outputs;

    /**
     * A chance of 10000 equals 100%
     */
    private final List<ChanceEntry> chancedOutputs;
    private final List<GTRecipeInput> fluidInputs;
    private final List<FluidStack> fluidOutputs;

    private final int duration;

    /**
     * if > 0 means EU/t consumed, if < 0 - produced
     */
    private final int EUt;

    /**
     * If this Recipe is hidden from JEI
     */
    private final boolean hidden;

    /**
     * If this Recipe is a Crafttweaker recipe. Used for logging purposes
     */
    // TODO YEET
    private final boolean isCTRecipe;
    private final boolean groovyRecipe;
    private final IRecipePropertyStorage recipePropertyStorage;

    private final int hashCode;

    public Recipe(List<GTRecipeInput> inputs, List<ItemStack> outputs, List<ChanceEntry> chancedOutputs,
                  List<GTRecipeInput> fluidInputs, List<FluidStack> fluidOutputs,
                  int duration, int EUt, boolean hidden, boolean isCTRecipe,
                  IRecipePropertyStorage recipePropertyStorage) {
        this.recipePropertyStorage =
                recipePropertyStorage == null ? EmptyRecipePropertyStorage.INSTANCE : recipePropertyStorage;
        if (inputs.isEmpty()) {
            this.inputs = Collections.emptyList();
        } else {
            this.inputs = NonNullList.create();
            this.inputs.addAll(inputs);
            this.inputs.sort((ing1, ing2) -> Boolean.compare(ing1.isNonConsumable(), ing2.isNonConsumable()));
        }
        if (outputs.isEmpty()) {
            this.outputs = EMPTY;
        } else {
            this.outputs = NonNullList.create();
            this.outputs.addAll(outputs);
        }
        this.chancedOutputs = chancedOutputs.isEmpty() ? Collections.emptyList() : new ArrayList<>(chancedOutputs);
        this.fluidInputs = fluidInputs.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(fluidInputs);
        this.fluidOutputs = fluidOutputs.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(fluidOutputs);
        this.duration = duration;
        this.EUt = EUt;
        this.hidden = hidden;
        this.isCTRecipe = isCTRecipe;
        this.hashCode = makeHashCode();
        if (Loader.isModLoaded(GTValues.MODID_GROOVYSCRIPT)) {
            this.groovyRecipe = GroovyScriptCompat.isCurrentlyRunning();
        } else {
            this.groovyRecipe = false;
        }
    }

    public Recipe copy() {
        return new Recipe(this.inputs, this.outputs, this.chancedOutputs, this.fluidInputs,
                this.fluidOutputs, this.duration, this.EUt, this.hidden, this.isCTRecipe, this.recipePropertyStorage);
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
    public Recipe trimRecipeOutputs(Recipe currentRecipe, RecipeMap<?> recipeMap, int itemTrimLimit, int fluidTrimLimit) {

        // Fast return early if no trimming desired
        if (itemTrimLimit == -1 && fluidTrimLimit == -1) {
            return currentRecipe;
        }


        currentRecipe = currentRecipe.copy();
        RecipeBuilder<?> builder = new RecipeBuilder<>(currentRecipe, recipeMap);

        builder.clearOutputs();
        builder.clearChancedOutput();
        builder.clearFluidOutputs();

        // Chanced outputs are removed in this if they cannot fit the limit
        Pair<List<ItemStack>, List<Recipe.ChanceEntry>> recipeOutputs = currentRecipe.getItemAndChanceOutputs(itemTrimLimit);

        // Add the trimmed chanced outputs and outputs
        builder.chancedOutputs(recipeOutputs.getRight());
        builder.outputs(recipeOutputs.getLeft());

        List<FluidStack> recipeFluidOutputs = currentRecipe.getAllFluidOutputs(fluidTrimLimit);

        // Add the trimmed fluid outputs
        builder.fluidOutputs(recipeFluidOutputs);


        return builder.build().getResult();
    }

    public final boolean matches(boolean consumeIfSuccessful, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
        return matches(consumeIfSuccessful, GTUtility.itemHandlerToList(inputs), GTUtility.fluidHandlerToList(fluidInputs));
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
        Pair<Boolean, int[]> fluids = null;
        Pair<Boolean, int[]> items = null;

        if (fluidInputs.size() >0 ) {
            fluids = matchesFluid(fluidInputs);
            if (!fluids.getKey()) {
                return false;
            }
        }

        if (inputs.size() > 0) {
            items = matchesItems(inputs);
            if (!items.getKey()) {
                return false;
            }
        }

        if (consumeIfSuccessful) {
            if (fluids != null) {
                int[] fluidAmountInTank = fluids.getValue();

                for (int i = 0; i < fluidAmountInTank.length; i++) {
                    FluidStack fluidStack = fluidInputs.get(i);
                    int fluidAmount = fluidAmountInTank[i];
                    if (fluidStack == null || fluidStack.amount == fluidAmount)
                        continue;
                    fluidStack.amount = fluidAmount;
                    if (fluidStack.amount == 0)
                        fluidInputs.set(i, null);
                }
            }
            if(items != null) {
                int[] itemAmountInSlot = items.getValue();
                for (int i = 0; i < itemAmountInSlot.length; i++) {
                    ItemStack itemInSlot = inputs.get(i);
                    int itemAmount = itemAmountInSlot[i];
                    if (itemInSlot.isEmpty() || itemInSlot.getCount() == itemAmount)
                        continue;
                    itemInSlot.setCount(itemAmountInSlot[i]);
                }
            }
        }

        return true;
    }

    private Pair<Boolean, int[]> matchesItems(List<ItemStack> inputs) {
        int[] itemAmountInSlot = new int[inputs.size()];
        int indexed = 0;

        List<GTRecipeInput> gtRecipeInputs = this.inputs;
        for (int i = 0; i < gtRecipeInputs.size(); i++) {
            GTRecipeInput ingredient = gtRecipeInputs.get(i);
            int ingredientAmount = ingredient.getAmount();
            for (int j = 0; j < inputs.size(); j++) {
                ItemStack inputStack = inputs.get(j);

                if (j == indexed) {
                    itemAmountInSlot[j] = inputStack.isEmpty() ? 0 : inputStack.getCount();
                    indexed++;
                }

                if (inputStack.isEmpty() || !ingredient.acceptsStack(inputStack))
                    continue;
                int itemAmountToConsume = Math.min(itemAmountInSlot[j], ingredientAmount);
                ingredientAmount -= itemAmountToConsume;
                if (!ingredient.isNonConsumable()) itemAmountInSlot[j] -= itemAmountToConsume;
                if (ingredientAmount == 0) break;
            }
            if (ingredientAmount > 0)
                return Pair.of(false, itemAmountInSlot);
        }
        int[] retItemAmountInSlot = new int[indexed];
        System.arraycopy(itemAmountInSlot, 0, retItemAmountInSlot, 0, indexed);

        return Pair.of(true, retItemAmountInSlot);
    }

    private Pair<Boolean, int[]> matchesFluid(List<FluidStack> fluidInputs) {
        int[] fluidAmountInTank = new int[fluidInputs.size()];
        int indexed = 0;

        List<GTRecipeInput> gtRecipeInputs = this.fluidInputs;
        for (int i = 0; i < gtRecipeInputs.size(); i++) {
            GTRecipeInput fluid = gtRecipeInputs.get(i);
            int fluidAmount = fluid.getAmount();
            for (int j = 0; j < fluidInputs.size(); j++) {
                FluidStack tankFluid = fluidInputs.get(j);

                if (j == indexed) {
                    indexed++;
                    fluidAmountInTank[j] = tankFluid == null ? 0 : tankFluid.amount;
                }

                if (tankFluid == null || !fluid.acceptsFluid(tankFluid))
                    continue;
                int fluidAmountToConsume = Math.min(fluidAmountInTank[j], fluidAmount);
                fluidAmount -= fluidAmountToConsume;
                if (!fluid.isNonConsumable()) fluidAmountInTank[j] -= fluidAmountToConsume;
                if (fluidAmount == 0) break;
            }
            if (fluidAmount > 0)
                return Pair.of(false, fluidAmountInTank);
        }
        int[] retfluidAmountInTank = new int[indexed];
        System.arraycopy(fluidAmountInTank, 0, retfluidAmountInTank, 0, indexed);

        return Pair.of(true, retfluidAmountInTank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return hasSameInputs(recipe) && hasSameFluidInputs(recipe);
    }

    private int makeHashCode() {
        int hash = 31 * hashInputs();
        hash = 31 * hash + hashFluidList(this.fluidInputs);
        return hash;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    private int hashInputs() {
        int hash = 0;
        for (GTRecipeInput recipeIngredient : this.inputs) {
            if (!recipeIngredient.isOreDict()) {
                for (ItemStack is : recipeIngredient.getInputStacks()) {
                    hash = 31 * hash + ItemStackHashStrategy.comparingAll().hashCode(is);
                }
            } else {
                hash = 31 * hash + recipeIngredient.getOreDict();
            }
        }
        return hash;
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

    public int hashFluidList(List<GTRecipeInput> fluids) {
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
                .append("EUt", EUt)
                .append("hidden", hidden)
                .append("CTRecipe", isCTRecipe)
                .toString();
    }

    ///////////////////
    //    Getters    //
    ///////////////////

    public List<GTRecipeInput> getInputs() {
        return inputs;
    }

    public NonNullList<ItemStack> getOutputs() {
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
        ArrayList<ItemStack> outputs = new ArrayList<>(GTUtility.copyStackList(getOutputs()));
        List<ChanceEntry> chancedOutputsList = getChancedOutputs();
        List<ItemStack> resultChanced = new ArrayList<>();
        for (ChanceEntry chancedOutput : chancedOutputsList) {
            int outputChance = recipeMap.getChanceFunction().chanceFor(
                    chancedOutput.getChance(), chancedOutput.getBoostPerTier(),
                    recipeTier, machineTier);
            if (GTValues.RNG.nextInt(Recipe.getMaxChancedValue()) <= outputChance) {
                ItemStack stackToAdd = chancedOutput.getItemStack();
                GTUtility.addStackToItemStackList(stackToAdd, resultChanced);
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
    public Pair<List<ItemStack>, List<ChanceEntry>> getItemAndChanceOutputs(int outputLimit) {
        List<ItemStack> outputs = new ArrayList<>();


        // Create an entry for the chanced outputs, and initially populate it
        List<ChanceEntry> chancedOutputs = new ArrayList<>(getChancedOutputs());


        // No limiting
        if (outputLimit == -1) {
            outputs.addAll(GTUtility.copyStackList(getOutputs()));
        }
        // If just the regular outputs would satisfy the outputLimit
        else if (getOutputs().size() >= outputLimit) {
            outputs.addAll(GTUtility.copyStackList(getOutputs()).subList(0, Math.min(outputLimit, getOutputs().size())));
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

        for (ChanceEntry entry : this.chancedOutputs) {
            recipeOutputs.add(entry.getItemStack());
        }

        return recipeOutputs;
    }


    public List<ChanceEntry> getChancedOutputs() {
        return chancedOutputs;
    }

    public List<GTRecipeInput> getFluidInputs() {
        return fluidInputs;
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
     * Trims the list of fluid outputs based on some passed factor.
     * Similar to {@link Recipe#getItemAndChanceOutputs(int)} but does not handle chanced fluid outputs
     *
     * @param outputLimit The limiting factor to trim the fluid outputs to, -1 for disabled.
     * @return A trimmed List of fluid outputs.
     */
    // TODO, implement future chanced fluid outputs here
    public List<FluidStack> getAllFluidOutputs(int outputLimit) {
        return outputLimit == -1 ? fluidOutputs : fluidOutputs.subList(0, Math.min(fluidOutputs.size(), outputLimit));
    }

    public int getDuration() {
        return duration;
    }

    public int getEUt() {
        return EUt;
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
            }
            return Arrays.stream(ingredient.getInputStacks()).anyMatch(s -> !s.isEmpty());
        }
        for (GTRecipeInput fluidInput : fluidInputs) {
            FluidStack fluidIngredient = fluidInput.getInputFluidStack();
            if (fluidIngredient != null && fluidIngredient.amount > 0) {
                return true;
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////
    //               Property Helper Methods                 //
    ///////////////////////////////////////////////////////////
    public <T> T getProperty(RecipeProperty<T> property, T defaultValue) {
        return recipePropertyStorage.getRecipePropertyValue(property, defaultValue);
    }

    public Object getPropertyRaw(String key) {
        return recipePropertyStorage.getRawRecipePropertyValue(key);
    }

    public Set<Map.Entry<RecipeProperty<?>, Object>> getPropertyValues() {
        return recipePropertyStorage.getRecipeProperties();
    }

    public Set<String> getPropertyKeys() {
        return recipePropertyStorage.getRecipePropertyKeys();
    }

    public boolean hasProperty(RecipeProperty<?> property) {
        return recipePropertyStorage.hasRecipeProperty(property);
    }

    public int getPropertyCount() {
        return recipePropertyStorage.getSize();
    }

    public int getUnhiddenPropertyCount() {
        return (int) recipePropertyStorage.getRecipeProperties().stream().filter((property) -> !property.getKey().isHidden()).count();
    }

    public IRecipePropertyStorage getRecipePropertyStorage() {
        return recipePropertyStorage;
    }

    ///////////////////////////////////////////////////////////
    //                   Chanced Output                      //
    ///////////////////////////////////////////////////////////
    public static class ChanceEntry {
        private final ItemStack itemStack;
        private final int chance;
        private final int boostPerTier;

        public ChanceEntry(ItemStack itemStack, int chance, int boostPerTier) {
            this.itemStack = itemStack.copy();
            this.chance = chance;
            this.boostPerTier = boostPerTier;
        }

        public ItemStack getItemStack() {
            return itemStack.copy();
        }

        public ItemStack getItemStackRaw() {
            return itemStack;
        }

        public int getChance() {
            return chance;
        }

        public int getBoostPerTier() {
            return boostPerTier;
        }

        public ChanceEntry copy() {
            return new ChanceEntry(itemStack, chance, boostPerTier);
        }
    }
}
