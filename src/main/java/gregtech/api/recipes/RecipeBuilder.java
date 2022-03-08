package gregtech.api.recipes;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe.ChanceEntry;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @see Recipe
 */

@SuppressWarnings("unchecked")
public class RecipeBuilder<R extends RecipeBuilder<R>> {

    protected RecipeMap<R> recipeMap;

    protected final List<CountableIngredient> inputs;
    protected final NonNullList<ItemStack> outputs;
    protected final List<ChanceEntry> chancedOutputs;

    protected final List<FluidStack> fluidInputs;
    protected final List<FluidStack> fluidOutputs;

    protected int duration, EUt;
    protected boolean hidden = false;

    protected int parallel = 0;

    protected Consumer<RecipeBuilder<?>> onBuildAction = null;

    protected EnumValidationResult recipeStatus = EnumValidationResult.VALID;

    protected RecipeBuilder() {
        this.inputs = NonNullList.create();
        this.outputs = NonNullList.create();
        this.chancedOutputs = new ArrayList<>();

        this.fluidInputs = new ArrayList<>(0);
        this.fluidOutputs = new ArrayList<>(0);
    }

    public RecipeBuilder(Recipe recipe, RecipeMap<R> recipeMap) {
        this.recipeMap = recipeMap;
        this.inputs = NonNullList.create();
        this.inputs.addAll(recipe.getInputs());
        this.outputs = NonNullList.create();
        this.outputs.addAll(GTUtility.copyStackList(recipe.getOutputs()));
        this.chancedOutputs = new ArrayList<>(recipe.getChancedOutputs());

        this.fluidInputs = GTUtility.copyFluidList(recipe.getFluidInputs());
        this.fluidOutputs = GTUtility.copyFluidList(recipe.getFluidOutputs());

        this.duration = recipe.getDuration();
        this.EUt = recipe.getEUt();
        this.hidden = recipe.isHidden();
    }

    @SuppressWarnings("CopyConstructorMissesField")
    protected RecipeBuilder(RecipeBuilder<R> recipeBuilder) {
        this.recipeMap = recipeBuilder.recipeMap;
        this.inputs = NonNullList.create();
        this.inputs.addAll(recipeBuilder.getInputs());
        this.outputs = NonNullList.create();
        this.outputs.addAll(GTUtility.copyStackList(recipeBuilder.getOutputs()));
        this.chancedOutputs = new ArrayList<>(recipeBuilder.chancedOutputs);

        this.fluidInputs = GTUtility.copyFluidList(recipeBuilder.getFluidInputs());
        this.fluidOutputs = GTUtility.copyFluidList(recipeBuilder.getFluidOutputs());
        this.duration = recipeBuilder.duration;
        this.EUt = recipeBuilder.EUt;
        this.hidden = recipeBuilder.hidden;
        this.onBuildAction = recipeBuilder.onBuildAction;
    }

    public boolean applyProperty(String key, Object value) {
        return false;
    }

    public boolean applyProperty(String key, ItemStack item) {
        return false;
    }

    public R inputs(ItemStack... inputs) {
        return inputs(Arrays.asList(inputs));
    }

    public R inputs(Collection<ItemStack> inputs) {
        if (GTUtility.iterableContains(inputs, stack -> stack == null || stack.isEmpty())) {
            GTLog.logger.error("Input cannot contain null or empty ItemStacks. Inputs: {}", inputs);
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        inputs.forEach(stack -> {
            if (!(stack == null || stack.isEmpty())) {
                this.inputs.add(CountableIngredient.from(stack));
            }
        });
        return (R) this;
    }

    public R input(String oredict, int count) {
        return inputs(CountableIngredient.from(oredict, count));
    }

    public R input(Enum<?> oredict, int count) {
        return inputs(CountableIngredient.from(oredict.name(), count));
    }


    public R input(OrePrefix orePrefix, Material material) {
        return inputs(CountableIngredient.from(orePrefix, material, 1));
    }

    public R input(OrePrefix orePrefix, Material material, int count) {
        return inputs(CountableIngredient.from(orePrefix, material, count));
    }

    public R input(Item item) {
        return input(item, 1);
    }

    public R input(Item item, int count) {
        return inputs(new ItemStack(item, count));
    }

    public R input(Item item, int count, int meta) {
        return inputs(new ItemStack(item, count, meta));
    }

    @SuppressWarnings("unused")
    public R input(Item item, int count, boolean wild) {
        return inputs(new ItemStack(item, count, GTValues.W));
    }

    public R input(Block item) {
        return input(item, 1);
    }

    public R input(Block item, int count) {
        return inputs(new ItemStack(item, count));
    }

    @SuppressWarnings("unused")
    public R input(Block item, int count, boolean wild) {
        return inputs(new ItemStack(item, count, GTValues.W));
    }

    public R input(MetaItem<?>.MetaValueItem item, int count) {
        return inputs(item.getStackForm(count));
    }

    public R input(MetaItem<?>.MetaValueItem item) {
        return input(item, 1);
    }

    public R input(MetaTileEntity mte) {
        return input(mte, 1);
    }

    public R input(MetaTileEntity mte, int amount) {
        return inputs(mte.getStackForm(amount));
    }

    public R inputs(CountableIngredient... inputs) {
        List<CountableIngredient> ingredients = new ArrayList<>();
        for (CountableIngredient input : inputs) {
            if (input.getCount() < 0) {
                GTLog.logger.error("Count cannot be less than 0. Actual: {}.", input.getCount());
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            } else {
                ingredients.add(input);
            }
        }

        return inputsIngredients(ingredients);
    }

    public R inputsIngredients(Collection<CountableIngredient> ingredients) {
        this.inputs.addAll(ingredients);
        return (R) this;
    }

    public R clearInputs() {
        this.inputs.clear();
        return (R) this;
    }

    public R notConsumable(ItemStack itemStack) {
        return inputs(CountableIngredient.from(itemStack, itemStack.getCount())
                .setNonConsumable());
    }

    public R notConsumable(OrePrefix prefix, Material material, int amount) {
        return inputs(CountableIngredient.from(prefix, material, amount)
                .setNonConsumable());
    }

    public R notConsumable(OrePrefix prefix, Material material) {
        return notConsumable(prefix, material, 1);
    }

    public R notConsumable(Ingredient ingredient) {
        return inputs(new CountableIngredient(ingredient, 1)
                .setNonConsumable());
    }

    public R notConsumable(MetaItem<?>.MetaValueItem item) {
        return inputs(CountableIngredient.from(item.getStackForm(), 1)
                .setNonConsumable());
    }

    public R notConsumable(Fluid fluid, int amount) {
        FluidStack ncf = new FluidStack(fluid, amount, new NBTTagCompound());
        ncf.tag.setBoolean("nonConsumable", true);
        return fluidInputs(ncf);
    }

    public R notConsumable(Fluid fluid) {
        return notConsumable(fluid, 1);
    }

    public R notConsumable(FluidStack fluidStack) {
        FluidStack ncf = fluidStack.copy();
        if (ncf.tag == null) {
            ncf.tag = new NBTTagCompound();
        }
        ncf.tag.setBoolean("nonConsumable", true);
        return fluidInputs(ncf);
    }

    public R output(OrePrefix orePrefix, Material material) {
        return outputs(OreDictUnifier.get(orePrefix, material, 1));
    }

    public R output(OrePrefix orePrefix, Material material, int count) {
        return outputs(OreDictUnifier.get(orePrefix, material, count));
    }

    public R output(Item item) {
        return output(item, 1);
    }

    public R output(Item item, int count) {
        return outputs(new ItemStack(item, count));
    }

    public R output(Item item, int count, int meta) {
        return outputs(new ItemStack(item, count, meta));
    }

    public R output(Block item) {
        return output(item, 1);
    }

    public R output(Block item, int count) {
        return outputs(new ItemStack(item, count));
    }

    public R output(MetaItem<?>.MetaValueItem item, int count) {
        return outputs(item.getStackForm(count));
    }

    public R output(MetaItem<?>.MetaValueItem item) {
        return output(item, 1);
    }

    public R output(MetaTileEntity mte) {
        return output(mte, 1);
    }

    public R output(MetaTileEntity mte, int amount) {
        return outputs(mte.getStackForm(amount));
    }

    public R outputs(ItemStack... outputs) {
        return outputs(Arrays.asList(outputs));
    }

    public R outputs(Collection<ItemStack> outputs) {
        outputs = new ArrayList<>(outputs);
        outputs.removeIf(stack -> stack == null || stack.isEmpty());
        this.outputs.addAll(outputs);
        return (R) this;
    }

    public R clearOutputs() {
        this.outputs.clear();
        return (R) this;
    }

    public R fluidInputs(FluidStack... inputs) {
        return fluidInputs(Arrays.asList(inputs));
    }

    public R fluidInputs(Collection<FluidStack> inputs) {
        if (inputs.contains(null)) {
            GTLog.logger.error("Fluid input cannot contain null FluidStacks. Inputs: {}", inputs);
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.fluidInputs.addAll(inputs);
        this.fluidInputs.removeIf(Objects::isNull);
        return (R) this;
    }

    public R clearFluidInputs() {
        this.fluidInputs.clear();
        return (R) this;
    }

    public R fluidOutputs(FluidStack... outputs) {
        return fluidOutputs(Arrays.asList(outputs));
    }

    public R fluidOutputs(Collection<FluidStack> outputs) {
        outputs = new ArrayList<>(outputs);
        outputs.removeIf(Objects::isNull);
        this.fluidOutputs.addAll(outputs);
        return (R) this;
    }

    public R clearFluidOutputs() {
        this.fluidOutputs.clear();
        return (R) this;
    }

    public R chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
        if (stack == null || stack.isEmpty()) {
            return (R) this;
        }
        if (0 >= chance || chance > Recipe.getMaxChancedValue()) {
            GTLog.logger.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.", Recipe.getMaxChancedValue(), chance);
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }
        this.chancedOutputs.add(new ChanceEntry(stack.copy(), chance, tierChanceBoost));
        return (R) this;
    }

    public R chancedOutput(OrePrefix prefix, Material material, int count, int chance, int tierChanceBoost) {
        return chancedOutput(OreDictUnifier.get(prefix, material, count), chance, tierChanceBoost);
    }

    public R chancedOutput(OrePrefix prefix, Material material, int chance, int tierChanceBoost) {
        return chancedOutput(prefix, material, 1, chance, tierChanceBoost);
    }

    public R chancedOutput(MetaItem<?>.MetaValueItem item, int count, int chance, int tierChanceBoost) {
        return chancedOutput(item.getStackForm(count), chance, tierChanceBoost);
    }

    public R chancedOutput(MetaItem<?>.MetaValueItem item, int chance, int tierChanceBoost) {
        return chancedOutput(item, 1, chance, tierChanceBoost);
    }

    public R chancedOutputs(List<ChanceEntry> chancedOutputs) {
        chancedOutputs.stream().map(ChanceEntry::copy).forEach(this.chancedOutputs::add);
        return (R) this;
    }

    public R clearChancedOutput() {
        this.chancedOutputs.clear();
        return (R) this;
    }

    /**
     * Copies the chanced outputs of a Recipe numberOfOperations times, so every chanced output
     * gets an individual roll, instead of an all or nothing situation
     *
     * @param chancedOutputsFrom The original recipe before any parallel multiplication
     * @param numberOfOperations The number of parallel operations that have been performed
     */

    public void chancedOutputsMultiply(Recipe chancedOutputsFrom, int numberOfOperations) {
        for (Recipe.ChanceEntry entry : chancedOutputsFrom.getChancedOutputs()) {
            int chance = entry.getChance();
            int boost = entry.getBoostPerTier();

            // Add individual chanced outputs per number of parallel operations performed, to mimic regular recipes.
            // This is done instead of simply batching the chanced outputs by the number of parallel operations performed
            IntStream.range(0, numberOfOperations).forEach(value -> {
                this.chancedOutput(entry.getItemStack(), chance, boost);
            });
        }
    }

    /**
     * Appends the passed {@link Recipe} onto the inputs and outputs, multiplied by the amount specified by multiplier
     * The duration of the multiplied {@link Recipe} is also added to the current duration
     *
     * @param recipe           The Recipe to be multiplied
     * @param multiplier       Amount to multiply the recipe by
     * @param multiplyDuration Whether duration should be multiplied instead of EUt
     * @return the builder holding the multiplied recipe
     */

    public R append(Recipe recipe, int multiplier, boolean multiplyDuration) {
        for (Map.Entry<RecipeProperty<?>, Object> property : recipe.getPropertyValues()) {
            this.applyProperty(property.getKey().getKey(), property.getValue());
        }

        // Create holders for the various parts of the new multiplied Recipe
        List<CountableIngredient> newRecipeInputs = new ArrayList<>();
        List<FluidStack> newFluidInputs = new ArrayList<>();
        List<ItemStack> outputItems = new ArrayList<>();
        List<FluidStack> outputFluids = new ArrayList<>();

        // Populate the various holders of the multiplied Recipe
        multiplyInputsAndOutputs(newRecipeInputs, newFluidInputs, outputItems, outputFluids, recipe, multiplier);

        // Build the new Recipe with multiplied components
        this.inputsIngredients(newRecipeInputs);
        this.fluidInputs(newFluidInputs);

        this.outputs(outputItems);
        chancedOutputsMultiply(recipe, multiplier);

        this.fluidOutputs(outputFluids);

        this.EUt(multiplyDuration ? recipe.getEUt() : this.EUt + recipe.getEUt() * multiplier);
        this.duration(multiplyDuration ? this.duration + recipe.getDuration() * multiplier : recipe.getDuration());
        this.parallel += multiplier;

        return (R) this;
    }

    protected static void multiplyInputsAndOutputs(List<CountableIngredient> newRecipeInputs,
                                                   List<FluidStack> newFluidInputs,
                                                   List<ItemStack> outputItems,
                                                   List<FluidStack> outputFluids,
                                                   Recipe recipe,
                                                   int numberOfOperations) {
        recipe.getInputs().forEach(ci -> {
            if (ci.isNonConsumable()) {
                newRecipeInputs.add(new CountableIngredient(ci.getIngredient(),
                        ci.getCount()).setNonConsumable());
            } else {
                newRecipeInputs.add(new CountableIngredient(ci.getIngredient(),
                        ci.getCount() * numberOfOperations));
            }
        });

        recipe.getFluidInputs().forEach(fluidStack -> {
            if (fluidStack.tag != null && fluidStack.tag.hasKey("nonConsumable")) {
                FluidStack fs = new FluidStack(fluidStack.getFluid(), fluidStack.amount, new NBTTagCompound());
                fs.tag.setBoolean("nonConsumable", true);
                newFluidInputs.add(fs);
            } else {
                newFluidInputs.add(new FluidStack(fluidStack.getFluid(),
                        fluidStack.amount * (fluidStack.tag != null && fluidStack.tag.hasKey("nonConsumable") ? 1 : numberOfOperations)));
            }
        });

        recipe.getOutputs().forEach(itemStack ->
                outputItems.add(copyItemStackWithCount(itemStack,
                        itemStack.getCount() * numberOfOperations)));

        recipe.getFluidOutputs().forEach(fluidStack ->
                outputFluids.add(copyFluidStackWithAmount(fluidStack,
                        fluidStack.amount * numberOfOperations)));
    }

    public int getParallel() {
        return parallel;
    }

    protected static ItemStack copyItemStackWithCount(ItemStack itemStack, int count) {
        ItemStack itemCopy = itemStack.copy();
        itemCopy.setCount(count);
        return itemCopy;
    }

    protected static FluidStack copyFluidStackWithAmount(FluidStack fluidStack, int count) {
        FluidStack fluidCopy = fluidStack.copy();
        fluidCopy.amount = count;
        return fluidCopy;
    }

    public R duration(int duration) {
        this.duration = duration;
        return (R) this;
    }

    public R EUt(int EUt) {
        this.EUt = EUt;
        return (R) this;
    }

    public R hidden() {
        this.hidden = true;
        return (R) this;
    }

    public R setRecipeMap(RecipeMap<R> recipeMap) {
        this.recipeMap = recipeMap;
        return (R) this;
    }

    public R copy() {
        return (R) new RecipeBuilder<>(this);
    }

    protected EnumValidationResult finalizeAndValidate() {
        return validate();
    }

    public ValidationResult<Recipe> build() {
        return ValidationResult.newResult(finalizeAndValidate(),
                new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, duration, EUt, hidden));
    }

    protected EnumValidationResult validate() {
        if (EUt == 0) {
            GTLog.logger.error("EU/t cannot be equal to 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (duration <= 0) {
            GTLog.logger.error("Duration cannot be less or equal to 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (recipeStatus == EnumValidationResult.INVALID) {
            GTLog.logger.error("Invalid recipe, read the errors above: {}", this);
        }
        return recipeStatus;
    }

    protected R onBuild(Consumer<RecipeBuilder<?>> consumer) {
        this.onBuildAction = consumer;
        return (R) this;
    }

    protected R invalidateOnBuildAction() {
        this.onBuildAction = null;
        return (R) this;
    }

    public void buildAndRegister() {
        if (onBuildAction != null)
            onBuildAction.accept(this);
        ValidationResult<Recipe> validationResult = build();
        recipeMap.addRecipe(validationResult);
    }

    ///////////////////
    //    Getters    //
    ///////////////////

    public List<CountableIngredient> getInputs() {
        return inputs;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public List<ChanceEntry> getChancedOutputs() {
        return chancedOutputs;
    }

    /**
     * Similar to {@link Recipe#getAllItemOutputs()}, returns the recipe outputs and all chanced outputs
     *
     * @return A List of ItemStacks composed of the recipe outputs and chanced outputs
     */
    public List<ItemStack> getAllItemOutputs() {
        List<ItemStack> stacks = new ArrayList<>(getOutputs());

        stacks.addAll(getChancedOutputs().stream().map(ChanceEntry::getItemStack).collect(Collectors.toList()));

        return stacks;
    }

    public List<FluidStack> getFluidInputs() {
        return fluidInputs;
    }

    public List<FluidStack> getFluidOutputs() {
        return fluidOutputs;
    }

    public int getEUt() {
        return EUt;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("recipeMap", recipeMap)
                .append("inputs", inputs)
                .append("outputs", outputs)
                .append("chancedOutputs", chancedOutputs)
                .append("fluidInputs", fluidInputs)
                .append("fluidOutputs", fluidOutputs)
                .append("duration", duration)
                .append("EUt", EUt)
                .append("hidden", hidden)
                .append("recipeStatus", recipeStatus)
                .toString();
    }
}
