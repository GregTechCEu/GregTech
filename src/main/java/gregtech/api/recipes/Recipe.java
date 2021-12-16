package gregtech.api.recipes;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.recipes.recipeproperties.RecipePropertyStorage;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

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

    public static int getMaxChancedValue() {
        return 10000;
    }

    public static String formatChanceValue(int outputChance) {
        return String.format("%.2f", outputChance / (getMaxChancedValue() * 1.0) * 100);
    }

    private final List<CountableIngredient> inputs;
    private final NonNullList<ItemStack> outputs;

    /**
     * A chance of 10000 equals 100%
     */
    private final List<ChanceEntry> chancedOutputs;
    private final List<FluidStack> fluidInputs;
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

    private final RecipePropertyStorage recipePropertyStorage;

    private static final ItemStackHashStrategy hashStrategy = ItemStackHashStrategy.comparingAll();

    private final int hashCode;

    public Recipe(List<CountableIngredient> inputs, List<ItemStack> outputs, List<ChanceEntry> chancedOutputs,
                  List<FluidStack> fluidInputs, List<FluidStack> fluidOutputs,
                  int duration, int EUt, boolean hidden) {
        this.recipePropertyStorage = new RecipePropertyStorage();
        this.inputs = NonNullList.create();
        this.inputs.addAll(inputs);
        this.outputs = NonNullList.create();
        this.outputs.addAll(outputs);
        this.chancedOutputs = new ArrayList<>(chancedOutputs);
        this.fluidInputs = ImmutableList.copyOf(fluidInputs);
        this.fluidOutputs = ImmutableList.copyOf(fluidOutputs);
        this.duration = duration;
        this.EUt = EUt;
        this.hidden = hidden;

        //sort not consumables inputs to the end
        this.inputs.sort((ing1, ing2) -> ing1.getCount() == 0 ? 1 : 0);
        this.hashCode = makeHashCode();
    }

    public final boolean matches(boolean consumeIfSuccessful, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode matchingMode) {
        return matches(consumeIfSuccessful, GTUtility.itemHandlerToList(inputs), GTUtility.fluidHandlerToList(fluidInputs), matchingMode);
    }

    public final boolean matches(boolean consumeIfSuccessful, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
        return matches(consumeIfSuccessful, GTUtility.itemHandlerToList(inputs), GTUtility.fluidHandlerToList(fluidInputs), MatchingMode.DEFAULT);
    }

    public boolean matches(boolean consumeIfSuccessful, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
        return matches(consumeIfSuccessful, inputs, fluidInputs, MatchingMode.DEFAULT);
    }

    /**
     * This methods aim to verify if the current recipe matches the given inputs according to matchingMode mode.
     *
     * @param consumeIfSuccessful if true and matchingMode is equal to {@link MatchingMode#DEFAULT} will consume the inputs of the recipe.
     * @param inputs              Items input or Collections.emptyList() if none.
     * @param fluidInputs         Fluids input or Collections.emptyList() if none.
     * @param matchingMode        How this method should check if inputs matches according to {@link MatchingMode} description.
     * @return true if the recipe matches the given inputs false otherwise.
     */
    public boolean matches(boolean consumeIfSuccessful, List<ItemStack> inputs, List<FluidStack> fluidInputs, MatchingMode matchingMode) {
        Pair<Boolean, Integer[]> fluids = null;
        Pair<Boolean, Integer[]> items = null;

        if (matchingMode == MatchingMode.IGNORE_FLUIDS) {
            if (getInputs().isEmpty()) {
                return false;
            }
        } else {
            fluids = matchesFluid(fluidInputs);
            if (!fluids.getKey()) {
                return false;
            }
        }

        if (matchingMode == MatchingMode.IGNORE_ITEMS) {
            if (getFluidInputs().isEmpty()) {
                return false;
            }
        } else {
            items = matchesItems(inputs);
            if (!items.getKey()) {
                return false;
            }
        }

        if (consumeIfSuccessful && matchingMode == MatchingMode.DEFAULT) {
            Integer[] fluidAmountInTank = fluids.getValue();
            Integer[] itemAmountInSlot = items.getValue();
            for (int i = 0; i < fluidAmountInTank.length; i++) {
                FluidStack fluidStack = fluidInputs.get(i);
                int fluidAmount = fluidAmountInTank[i];
                if (fluidStack == null || fluidStack.amount == fluidAmount)
                    continue;
                fluidStack.amount = fluidAmount;
                if (fluidStack.amount == 0)
                    fluidInputs.set(i, null);
            }
            for (int i = 0; i < itemAmountInSlot.length; i++) {
                ItemStack itemInSlot = inputs.get(i);
                int itemAmount = itemAmountInSlot[i];
                if (itemInSlot.isEmpty() || itemInSlot.getCount() == itemAmount)
                    continue;
                itemInSlot.setCount(itemAmountInSlot[i]);
            }
        }

        return true;
    }

    private Pair<Boolean, Integer[]> matchesItems(List<ItemStack> inputs) {
        Integer[] itemAmountInSlot = new Integer[inputs.size()];

        for (int i = 0; i < itemAmountInSlot.length; i++) {
            ItemStack itemInSlot = inputs.get(i);
            itemAmountInSlot[i] = itemInSlot.isEmpty() ? 0 : itemInSlot.getCount();
        }

        for (CountableIngredient ingredient : this.inputs) {
            int ingredientAmount = ingredient.getCount();
            boolean isNotConsumed = false;
            if (ingredientAmount == 0) {
                ingredientAmount = 1;
                isNotConsumed = true;
            }
            for (int i = 0; i < inputs.size(); i++) {
                ItemStack inputStack = inputs.get(i);
                if (inputStack.isEmpty() || !ingredient.getIngredient().apply(inputStack))
                    continue;
                int itemAmountToConsume = Math.min(itemAmountInSlot[i], ingredientAmount);
                ingredientAmount -= itemAmountToConsume;
                if (!isNotConsumed) itemAmountInSlot[i] -= itemAmountToConsume;
                if (ingredientAmount == 0) break;
            }
            if (ingredientAmount > 0)
                return Pair.of(false, itemAmountInSlot);
        }

        return Pair.of(true, itemAmountInSlot);
    }

    private Pair<Boolean, Integer[]> matchesFluid(List<FluidStack> fluidInputs) {
        Integer[] fluidAmountInTank = new Integer[fluidInputs.size()];

        for (int i = 0; i < fluidAmountInTank.length; i++) {
            FluidStack fluidInTank = fluidInputs.get(i);
            fluidAmountInTank[i] = fluidInTank == null ? 0 : fluidInTank.amount;
        }

        for (FluidStack fluid : this.fluidInputs) {
            int fluidAmount = fluid.amount;
            boolean isNotConsumed = false;
            if (fluidAmount == 0) {
                fluidAmount = 1;
                isNotConsumed = true;
            }
            for (int i = 0; i < fluidInputs.size(); i++) {
                FluidStack tankFluid = fluidInputs.get(i);
                if (tankFluid == null || !tankFluid.isFluidEqual(fluid))
                    continue;
                int fluidAmountToConsume = Math.min(fluidAmountInTank[i], fluidAmount);
                fluidAmount -= fluidAmountToConsume;
                if (!isNotConsumed) fluidAmountInTank[i] -= fluidAmountToConsume;
                if (fluidAmount == 0) break;
            }
            if (fluidAmount > 0)
                return Pair.of(false, fluidAmountInTank);
        }
        return Pair.of(true, fluidAmountInTank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return this.EUt == recipe.EUt &&
                this.duration == recipe.duration &&
                hasSameInputs(recipe) &&
                hasSameOutputs(recipe) &&
                hasSameChancedOutputs(recipe) &&
                hasSameFluidInputs(recipe) &&
                hasSameFluidOutputs(recipe) &&
                hasSameRecipeProperties(recipe);
    }

    private int makeHashCode() {
        int hash = Objects.hash(EUt, duration);
        hash += hashInputs() * 7;
        hash += hashOutputs() * 11;
        hash += hashChancedOutputs() * 13;
        hash += hashFluidList(this.fluidInputs) * 17;
        hash += hashFluidList(this.fluidOutputs) * 19;
        hash += hashRecipeProperties() * 23;
        return hash;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    private int hashInputs() {
        int hash = 0;
        for (CountableIngredient countableIngredient : this.inputs) {
            for (ItemStack is : countableIngredient.getIngredient().getMatchingStacks()) {
                hash += ItemStackHashStrategy.comparingAllButCount().hashCode(is);
                hash += countableIngredient.getCount();
            }
        }
        return hash;
    }

    private boolean hasSameInputs(Recipe otherRecipe) {
        if (this.inputs.size() != otherRecipe.inputs.size()) return false;
        for (int i = 0; i < inputs.size(); i++) {
            for (int j = 0; j < this.inputs.get(i).getIngredient().getMatchingStacks().length; j++) {
                if (!hashStrategy.equals(this.inputs.get(i).getIngredient().getMatchingStacks()[j],
                        otherRecipe.inputs.get(i).getIngredient().getMatchingStacks()[j])) {
                    return false;
                }
            }
        }
        return true;
    }

    private int hashOutputs() {
        int hash = 0;
        for (ItemStack is : this.outputs) {
            hash += hashStrategy.hashCode(is);
        }
        return hash;
    }

    private boolean hasSameOutputs(Recipe otherRecipe) {
        if (this.outputs.size() != otherRecipe.outputs.size()) return false;
        for (int i = 0; i < outputs.size(); i++) {
            if (!hashStrategy.equals(this.outputs.get(i), otherRecipe.outputs.get(i))) {
                return false;
            }
        }
        return true;
    }

    private int hashChancedOutputs() {
        int hash = 0;
        for (ChanceEntry chanceEntry : this.chancedOutputs) {
            hash += hashStrategy.hashCode(chanceEntry.itemStack);
            hash += chanceEntry.chance;
            hash += chanceEntry.boostPerTier;
        }
        return hash;
    }

    private boolean hasSameChancedOutputs(Recipe otherRecipe) {
        if (this.chancedOutputs.size() != otherRecipe.chancedOutputs.size()) return false;
        for (int i = 0; i < chancedOutputs.size(); i++) {
            if (!hashStrategy.equals(this.chancedOutputs.get(i).itemStack, otherRecipe.chancedOutputs.get(i).itemStack)) {
                return false;
            }
        }
        return true;
    }

    public int hashFluidList(List<FluidStack> fluids) {
        int hash = 0;
        for (FluidStack fluidStack : fluids) {
            hash += new FluidKey(fluidStack).hashCode();
        }
        return hash;
    }

    private boolean hasSameFluidInputs(Recipe otherRecipe) {
        if (this.fluidInputs.size() != otherRecipe.fluidInputs.size()) return false;
        for (int i = 0; i < fluidInputs.size(); i++) {
            if (!fluidInputs.get(i).isFluidStackIdentical(otherRecipe.fluidInputs.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean hasSameFluidOutputs(Recipe otherRecipe) {
        if (this.fluidOutputs.size() != otherRecipe.fluidOutputs.size()) return false;
        for (int i = 0; i < fluidOutputs.size(); i++) {
            if (!fluidOutputs.get(i).isFluidStackIdentical(otherRecipe.fluidOutputs.get(i))) {
                return false;
            }
        }
        return true;
    }

    private int hashRecipeProperties() {
        int hash = 0;
        for (Map.Entry<RecipeProperty<?>, Object> propertyObjectEntry : this.recipePropertyStorage.getRecipeProperties()) {
            hash += propertyObjectEntry.getKey().hashCode();
        }
        return hash;
    }

    private boolean hasSameRecipeProperties(Recipe otherRecipe) {
        if (this.getPropertyCount() != otherRecipe.getPropertyCount()) return false;
        return this.recipePropertyStorage.getRecipeProperties().containsAll(otherRecipe.recipePropertyStorage.getRecipeProperties());
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
                .toString();
    }

    ///////////////////
    //    Getters    //
    ///////////////////

    public List<CountableIngredient> getInputs() {
        return inputs;
    }

    public NonNullList<ItemStack> getOutputs() {
        return outputs;
    }

    public List<ItemStack> getResultItemOutputs(int maxOutputSlots, int tier) {
        ArrayList<ItemStack> outputs = new ArrayList<>(GTUtility.copyStackList(getOutputs()));
        List<ChanceEntry> chancedOutputsList = getChancedOutputs();
        List<ItemStack> resultChanced = new ArrayList<>();
        int maxChancedSlots = maxOutputSlots - outputs.size();
        for (ChanceEntry chancedOutput : chancedOutputsList) {
            int outputChance = RecipeMap.getChanceFunction().chanceFor(chancedOutput.getChance(), chancedOutput.getBoostPerTier(), tier);
            if (GTValues.RNG.nextInt(Recipe.getMaxChancedValue()) <= outputChance) {
                ItemStack stackToAdd = chancedOutput.getItemStack();
                GTUtility.addStackToItemStackList(stackToAdd, resultChanced);
            }
        }
        if (resultChanced.size() > maxChancedSlots) {
            outputs.addAll(resultChanced.subList(0, Math.max(0, maxChancedSlots)));
        } else {
            outputs.addAll(resultChanced);
        }
        return outputs;
    }

    public List<ItemStack> getAllItemOutputs(int maxOutputSlots) {
        List<ItemStack> outputs = new ArrayList<>();
        outputs.addAll(GTUtility.copyStackList(getOutputs()));
        outputs.addAll(getChancedOutputs().stream().map(ChanceEntry::getItemStack).collect(Collectors.toList()));
        if (outputs.size() > maxOutputSlots) {
            outputs = outputs.subList(0, maxOutputSlots);
        }
        return outputs;
    }

    public List<ChanceEntry> getChancedOutputs() {
        return chancedOutputs;
    }

    public List<FluidStack> getFluidInputs() {
        return fluidInputs;
    }

    public boolean hasInputFluid(FluidStack fluid) {
        for (FluidStack fluidStack : fluidInputs) {
            if (fluidStack.isFluidEqual(fluid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNotConsumedInput(Object stack) {
        if (stack instanceof FluidStack) {
            if (fluidInputs.contains(stack)) {
                return fluidInputs.get(fluidInputs.indexOf(stack)).amount == 0;
            } else return false;
        } else if (stack instanceof ItemStack) {
            for (CountableIngredient ing : inputs) {
                if (ing.getCount() != 0) continue;
                for (ItemStack inputStack : ing.getIngredient().getMatchingStacks()) {
                    if (inputStack.getItem() == ((ItemStack) stack).getItem()
                            && inputStack.getItemDamage() == ((ItemStack) stack).getItemDamage()
                            && Objects.equals(inputStack.getTagCompound(), ((ItemStack) stack).getTagCompound())) {
                        return true;
                    }
                }
            }
            return false;
        } else return false;
    }

    public List<FluidStack> getFluidOutputs() {
        return fluidOutputs;
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

    public boolean hasValidInputsForDisplay() {
        boolean hasValidInputs = true;
        for (CountableIngredient ingredient : inputs) {
            ItemStack[] matchingItems = ingredient.getIngredient().getMatchingStacks();
            hasValidInputs &= Arrays.stream(matchingItems).anyMatch(s -> !s.isEmpty());
        }
        return hasValidInputs;
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

    public boolean setProperty(RecipeProperty<?> property, Object value) {
        return recipePropertyStorage.store(property, value);
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
